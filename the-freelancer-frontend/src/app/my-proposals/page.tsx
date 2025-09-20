'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import proposalService from '@/services/proposal';
import { Proposal } from '@/services/proposal';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import Link from 'next/link';
import { ArrowRight, Briefcase, Calendar, DollarSign, Loader2 } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

export default function MyProposalsPage() {
  const [proposals, setProposals] = useState<Proposal[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProposals = async () => {
      try {
        setLoading(true);
        const data = await proposalService.getMyProposals();
        console.log('Proposals data:', data); // Debug log to see what we're getting
        setProposals(data);
      } catch (err: any) {
        setError(err.message || 'Failed to load proposals');
      } finally {
        setLoading(false);
      }
    };

    fetchProposals();
  }, []);

  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'SUBMITTED':
        return 'default';
      case 'ACCEPTED':
      case 'CONTRACTED':
        return 'success';
      case 'DECLINED':
        return 'destructive';
      default:
        return 'secondary';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <motion.h1 
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-3xl font-bold leading-tight text-gray-900 flex items-center"
          >
            <Briefcase className="mr-3 h-8 w-8 text-blue-600" />
            My Proposals
          </motion.h1>
        </div>
      </header>
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <Loader2 className="h-12 w-12 animate-spin text-blue-600" />
          </div>
        ) : error ? (
          <div className="text-center text-red-500 bg-red-100 p-4 rounded-lg">{error}</div>
        ) : proposals.length === 0 ? (
          <div className="text-center text-gray-500 py-16">
            <Briefcase className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No proposals found</h3>
            <p className="mt-1 text-sm text-gray-500">You have not submitted any proposals yet.</p>
            <div className="mt-6">
              <Link href="/jobs">
                <Button>
                  Browse Jobs
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            {proposals.map((proposal, index) => (
              <motion.div
                key={proposal.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden"
              >
                <div className="p-6">
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="text-sm text-gray-500">Proposal for:</p>
                      <h2 className="text-xl font-semibold text-gray-800 hover:text-blue-600 transition-colors">
                        <Link href={`/jobs/${proposal.jobId}`}>
                          {proposal.jobTitle || `Job #${proposal.jobId}`}
                        </Link>
                      </h2>
                    </div>
                    <Badge variant={getStatusVariant(proposal.status)}>{proposal.status}</Badge>
                  </div>
                  <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                    <div className="flex items-center text-gray-600">
                      <DollarSign className="h-4 w-4 mr-2 text-green-500" />
                      <span>Proposed Rate: <span className="font-semibold text-gray-800">${proposal.proposedRate}</span></span>
                    </div>
                    <div className="flex items-center text-gray-600">
                      <Calendar className="h-4 w-4 mr-2 text-blue-500" />
                      <span>Submitted: <span className="font-semibold text-gray-800">
                        {(() => {
                          try {
                            const date = new Date(proposal.createdAt);
                            return isNaN(date.getTime()) ? 'Unknown date' : formatDistanceToNow(date, { addSuffix: true });
                          } catch {
                            return 'Unknown date';
                          }
                        })()}
                      </span></span>
                    </div>
                  </div>
                </div>
                {proposal.status === "CONTRACTED" && (
                  <div className="bg-gray-50 px-6 py-3">
                    {proposal.contractId ? (
                      <Link href={`/workspace/${proposal.contractId}`}>
                        <Button variant="link" className="p-0 h-auto text-blue-600">
                          Go to Workspace <ArrowRight className="ml-1 h-4 w-4" />
                        </Button>
                      </Link>
                    ) : (
                      <div className="text-sm text-gray-600">
                        Workspace is being prepared...
                        <Button variant="link" className="p-0 h-auto text-gray-600 ml-2" disabled>
                          Setting up workspace
                        </Button>
                      </div>
                    )}
                  </div>
                )}
              </motion.div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
