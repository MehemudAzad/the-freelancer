'use client';

import React, { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowLeft, 
  Send, 
  FileText, 
  DollarSign, 
  Clock, 
  Link as LinkIcon, 
  Plus, 
  X,
  Briefcase,
  User,
  Star,
  MapPin,
  Calendar,
  CheckCircle,
  AlertCircle,
  Sparkles,
  Zap,
  Award,
  Wand2,
  Loader2
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { useAppSelector } from '@/store/hooks';
import proposalService, { ProposalData } from '@/services/proposal';
import jobService from '@/services/job';
import { Job } from '@/types/api';
import { toast } from 'react-hot-toast';

function ProposalPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const jobId = searchParams?.get('jobId');
  
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  
  const [job, setJob] = useState<Job | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [portfolioLinks, setPortfolioLinks] = useState<string[]>(['']);
  const [generatingCoverLetter, setGeneratingCoverLetter] = useState(false);
  
  const [formData, setFormData] = useState<Omit<ProposalData, 'jobId'>>({
    coverLetter: '',
    proposedRate: 0,
    deliveryDays: 1,
    portfolioLinks: '', // Changed from array to string
    additionalNotes: ''
  });

  // Redirect if not authenticated or not a freelancer
  useEffect(() => {
    // if (!isAuthenticated) {
    //   router.push('/auth/login');
    //   return;
    // }
    
    if (user?.role?.toLowerCase() !== 'freelancer') {
      router.push('/jobs');
      toast.error('Only freelancers can submit proposals');
      return;
    }
  }, [isAuthenticated, user, router]);

  // Fetch job details
  useEffect(() => {
    const fetchJob = async () => {
      if (!jobId) {
        router.push('/jobs');
        return;
      }

      try {
        setLoading(true);
        const jobData = await jobService.jobAPI.getJobById(parseInt(jobId));
        setJob(jobData);
      } catch (error) {
        console.error('Error fetching job:', error);
        toast.error('Failed to load job details');
        router.push('/jobs');
      } finally {
        setLoading(false);
      }
    };

    fetchJob();
  }, [jobId, router]);

  const handlePortfolioLinkChange = (index: number, value: string) => {
    const newLinks = [...portfolioLinks];
    newLinks[index] = value;
    setPortfolioLinks(newLinks);
  };

  const addPortfolioLink = () => {
    if (portfolioLinks.length < 5) { // Limit to 5 links
      setPortfolioLinks([...portfolioLinks, '']);
    }
  };

  const removePortfolioLink = (index: number) => {
    if (portfolioLinks.length > 1) {
      const newLinks = portfolioLinks.filter((_, i) => i !== index);
      setPortfolioLinks(newLinks);
    }
  };

  const generateAICoverLetter = async () => {
    if (!job) {
      toast.error('Job details not available');
      return;
    }

    setGeneratingCoverLetter(true);

    try {
      const response = await fetch('/api/ai/generate-cover-letter', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          jobTitle: job.title || job.projectName || 'Job Title',
          jobDescription: job.description || 'Job description',
          freelancerExperience: "7+ years React development, built 3 healthcare platforms, HIPAA compliance experience, worked with major hospitals and clinics",
          keySkills: "React, TypeScript, Redux, HIPAA compliance, Healthcare APIs, Node.js, PostgreSQL, AWS"
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to generate cover letter');
      }

      const data = await response.json();
      console.log('ai response:', data);
      
      if (data.coverLetter) {
        setFormData({ ...formData, coverLetter: data.coverLetter });
        toast.success('AI cover letter generated successfully!');
      } else {
        throw new Error('No cover letter received from AI');
      }
    } catch (error) {
      console.error('Error generating cover letter:', error);
      toast.error('Failed to generate cover letter. Please try again.');
    } finally {
      setGeneratingCoverLetter(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!job || !jobId) return;
    
    // Validation
    if (!formData.coverLetter.trim()) {
      toast.error('Please write a cover letter');
      return;
    }
    
    if (formData.proposedRate <= 0) {
      toast.error('Please enter a valid rate');
      return;
    }
    
    if (formData.deliveryDays <= 0) {
      toast.error('Please enter valid delivery days');
      return;
    }

    try {
      setSubmitting(true);
      
      // Filter out empty portfolio links
      const validPortfolioLinks = portfolioLinks.filter(link => link.trim() !== '');
      
      const proposalData: ProposalData = {
        jobId: parseInt(jobId),
        coverLetter: formData.coverLetter,
        proposedRate: formData.proposedRate,
        deliveryDays: formData.deliveryDays,
        portfolioLinks: validPortfolioLinks.join(', '), // Convert array to comma-separated string
        additionalNotes: formData.additionalNotes
      };

      await proposalService.submitProposal(proposalData);
      
      toast.success('Proposal submitted successfully!');
      router.push('/jobs');
      
    } catch (error: any) {
      console.error('Error submitting proposal:', error);
      toast.error(error.response?.data?.message || 'Failed to submit proposal');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 flex items-center justify-center">
        <div className="text-center">
          <motion.div
            className="w-16 h-16 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
          />
          <p className="text-gray-600">Loading job details...</p>
        </div>
      </div>
    );
  }

  if (!job) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Job Not Found</h2>
          <p className="text-gray-600 mb-6">The job you're looking for doesn't exist or has been removed.</p>
          <Button onClick={() => router.push('/jobs')} className="bg-blue-600 hover:bg-blue-700">
            Back to Jobs
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 relative overflow-hidden">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-400/20 to-purple-600/20 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-tr from-indigo-400/20 to-pink-600/20 rounded-full blur-3xl"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-gradient-to-r from-cyan-400/10 to-blue-600/10 rounded-full blur-3xl"></div>
      </div>
      
      <div className="relative z-10 py-8">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: -30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: "easeOut" }}
            className="mb-12"
          >
            <div className="flex items-center space-x-4 mb-8">
              <motion.div
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <Button 
                  variant="ghost" 
                  onClick={() => router.back()}
                  className="p-3 hover:bg-white/30 rounded-xl backdrop-blur-sm border border-white/20 shadow-lg transition-all duration-300"
                >
                  <ArrowLeft className="h-5 w-5 text-gray-700" />
                </Button>
              </motion.div>
              <div>
                <motion.h1 
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.2, duration: 0.6 }}
                  className="text-4xl font-extrabold bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 bg-clip-text text-transparent mb-2"
                >
                  Submit Your Proposal ✨
                </motion.h1>
                <motion.p 
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.3, duration: 0.6 }}
                  className="text-gray-600 text-lg font-medium mb-6"
                >
                  Craft a winning proposal for this amazing opportunity 🎯
                </motion.p>
                <motion.div
                  initial={{ scaleX: 0 }}
                  animate={{ scaleX: 1 }}
                  transition={{ delay: 0.5, duration: 0.8 }}
                  className="h-1 w-24 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full mt-2"
                />
              </div>
            </div>
          </motion.div>

          <div className="grid lg:grid-cols-5 gap-10">
            {/* Job Details Card */}
            <motion.div
              initial={{ opacity: 0, x: -40 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.7, ease: "easeOut" }}
              className="lg:col-span-2"
            >
              <div className="bg-white/80 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-8 sticky top-8 overflow-hidden">
                {/* Decorative gradient overlay */}
                <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-blue-500/10 to-transparent rounded-full"></div>
                <div className="absolute bottom-0 left-0 w-24 h-24 bg-gradient-to-tr from-purple-500/10 to-transparent rounded-full"></div>
                
                <div className="relative z-10">
                  <motion.div 
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.3, duration: 0.6 }}
                    className="flex items-start space-x-5 mb-8"
                  >
                    <motion.div 
                      whileHover={{ scale: 1.1, rotate: 5 }}
                      className="p-4 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl text-white shadow-lg"
                    >
                      <Briefcase className="h-7 w-7" />
                    </motion.div>
                    <div className="flex-1">
                      <h2 className="font-bold text-gray-900 text-xl leading-tight mb-3">
                        {job.title || job.projectName}
                      </h2>
                      <div className="flex items-center space-x-3 text-sm text-gray-600 bg-gray-50 rounded-full px-4 py-2 w-fit">
                        <User className="h-4 w-4" />
                        <span className="font-medium">Posted by Client</span>
                      </div>
                    </div>
                  </motion.div>

                  <div className="space-y-6">
                    <motion.div 
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.4, duration: 0.6 }}
                      className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-2xl p-6 border border-green-100"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <div className="p-2 bg-green-500 rounded-xl">
                            <DollarSign className="h-5 w-5 text-white" />
                          </div>
                          <div>
                            <span className="text-sm font-medium text-green-700">Project Budget</span>
                            <p className="text-xs text-green-600">Client's allocated budget</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="font-bold text-2xl text-green-700">
                            ${job.budget || Math.floor((job.minBudgetCents || 0) / 100)}
                          </div>
                          <div className="text-sm text-green-600">
                            {job.maxBudgetCents && `- $${Math.floor(job.maxBudgetCents / 100)}`}
                          </div>
                        </div>
                      </div>
                    </motion.div>

                    <motion.div 
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.5, duration: 0.6 }}
                      className="grid grid-cols-1 gap-4"
                    >
                      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-4 border border-blue-100">
                        <div className="flex items-center space-x-3">
                          <div className="p-2 bg-blue-500 rounded-lg">
                            <MapPin className="h-4 w-4 text-white" />
                          </div>
                          <div className="flex-1">
                            <span className="text-sm font-medium text-blue-700">Category</span>
                            <p className="text-gray-700 font-semibold">{job.category || 'Not specified'}</p>
                          </div>
                        </div>
                      </div>

                      <div className="bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl p-4 border border-purple-100">
                        <div className="flex items-center space-x-3">
                          <div className="p-2 bg-purple-500 rounded-lg">
                            <Calendar className="h-4 w-4 text-white" />
                          </div>
                          <div className="flex-1">
                            <span className="text-sm font-medium text-purple-700">Posted</span>
                            <p className="text-gray-700 font-semibold">
                              {new Date(job.createdAt).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  </div>
                </div>
              </div>
            </motion.div>

            {/* Proposal Form */}
            <motion.div
              initial={{ opacity: 0, x: 40 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.7, ease: "easeOut" }}
              className="lg:col-span-3"
            >
              <form onSubmit={handleSubmit} className="space-y-8">
                <motion.div 
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.2, duration: 0.6 }}
                  className="bg-white/85 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-10 overflow-hidden relative"
                >
                  {/* Decorative elements */}
                  <div className="absolute top-0 right-0 w-40 h-40 bg-gradient-to-bl from-blue-500/5 to-transparent rounded-full"></div>
                  <div className="absolute bottom-0 left-0 w-32 h-32 bg-gradient-to-tr from-purple-500/5 to-transparent rounded-full"></div>
                  
                  <div className="relative z-10">
                    <div className="flex items-center justify-between mb-8">
                      <motion.div 
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: 0.3, duration: 0.6 }}
                        className="flex items-center space-x-4"
                      >
                        <motion.div 
                          whileHover={{ scale: 1.1, rotate: 5 }}
                          className="p-3 bg-gradient-to-br from-green-500 to-blue-600 rounded-2xl text-white shadow-lg"
                        >
                          <FileText className="h-6 w-6" />
                        </motion.div>
                        <div>
                          <h3 className="text-2xl font-bold text-gray-900">Cover Letter</h3>
                          <p className="text-gray-600 text-sm">Tell them why you're perfect for this job</p>
                        </div>
                      </motion.div>
                      
                      <motion.div
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                      >
                        <Button
                          type="button"
                          onClick={generateAICoverLetter}
                          disabled={generatingCoverLetter}
                          className="flex items-center space-x-3 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white px-6 py-3 rounded-2xl font-semibold shadow-lg transition-all duration-300"
                        >
                          {generatingCoverLetter ? (
                            <>
                              <Loader2 className="h-5 w-5 animate-spin" />
                              <span>Generating...</span>
                            </>
                          ) : (
                            <>
                              <Wand2 className="h-5 w-5" />
                              <span>Generate with AI</span>
                              <Sparkles className="h-4 w-4" />
                            </>
                          )}
                        </Button>
                      </motion.div>
                    </div>
                    
                    <motion.div
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: 0.4, duration: 0.6 }}
                    >
                      <Textarea
                        placeholder="🚀 Start with a strong opening that grabs attention... 

Explain why you're the perfect fit for this job. Highlight your relevant experience, skills, and what unique value you can deliver. Be specific about how you'll approach their project and what results they can expect."
                        value={formData.coverLetter}
                        onChange={(e) => setFormData({ ...formData, coverLetter: e.target.value })}
                        className="min-h-[250px] resize-none border-2 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20 bg-white/90 text-gray-900 placeholder-gray-500 rounded-2xl text-base leading-relaxed transition-all duration-300"
                        required
                      />
                      <div className="flex items-center justify-between mt-4">
                        <p className="text-sm text-gray-500">
                          {formData.coverLetter.length} characters • Be authentic and specific
                        </p>
                        <div className="flex items-center space-x-2 text-sm text-gray-500">
                          <CheckCircle className="h-4 w-4 text-green-500" />
                          <span>Professional tone</span>
                        </div>
                      </div>
                    </motion.div>
                  </div>  
                </motion.div>

                <div className="grid md:grid-cols-2 gap-8">
                  <motion.div 
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.3, duration: 0.6 }}
                    className="bg-white/85 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-8 relative overflow-hidden"
                  >
                    <div className="absolute top-0 right-0 w-24 h-24 bg-gradient-to-bl from-green-500/10 to-transparent rounded-full"></div>
                    
                    <div className="relative z-10">
                      <div className="flex items-center space-x-4 mb-6">
                        <motion.div 
                          whileHover={{ scale: 1.1, rotate: 5 }}
                          className="p-3 bg-gradient-to-br from-green-500 to-emerald-600 rounded-2xl text-white shadow-lg"
                        >
                          <DollarSign className="h-6 w-6" />
                        </motion.div>
                        <div>
                          <h3 className="text-xl font-bold text-gray-900">Your Rate</h3>
                          <p className="text-gray-600 text-sm">What's your price?</p>
                        </div>
                      </div>
                      
                      <div className="relative">
                        <div className="absolute left-4 top-1/2 transform -translate-y-1/2 text-green-600 font-bold">$</div>
                        <Input
                          type="number"
                          placeholder="0.00"
                          value={formData.proposedRate || ''}
                          onChange={(e) => setFormData({ ...formData, proposedRate: parseFloat(e.target.value) || 0 })}
                          className="pl-8 border-2 border-gray-200 focus:border-green-500 focus:ring-green-500/20 bg-white/90 text-gray-900 placeholder-gray-400 rounded-xl h-14 text-lg font-semibold transition-all duration-300"
                          min="1"
                          required
                        />
                      </div>
                      <div className="mt-4 p-4 bg-green-50 rounded-xl border border-green-100">
                        <p className="text-sm text-green-700 font-medium">
                          💰 Client's budget: <span className="font-bold">
                            ${job.budget || `${Math.floor((job.minBudgetCents || 0) / 100)} - ${Math.floor((job.maxBudgetCents || 0) / 100)}`}
                          </span>
                        </p>
                        <p className="text-xs text-green-600 mt-1">Price competitively to win the project</p>
                      </div>
                    </div>
                  </motion.div>

                  <motion.div 
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.4, duration: 0.6 }}
                    className="bg-white/85 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-8 relative overflow-hidden"
                  >
                    <div className="absolute top-0 right-0 w-24 h-24 bg-gradient-to-bl from-blue-500/10 to-transparent rounded-full"></div>
                    
                    <div className="relative z-10">
                      <div className="flex items-center space-x-4 mb-6">
                        <motion.div 
                          whileHover={{ scale: 1.1, rotate: 5 }}
                          className="p-3 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl text-white shadow-lg"
                        >
                          <Clock className="h-6 w-6" />
                        </motion.div>
                        <div>
                          <h3 className="text-xl font-bold text-gray-900">Delivery Time</h3>
                          <p className="text-gray-600 text-sm">When can you deliver?</p>
                        </div>
                      </div>
                      
                      <div className="relative">
                        <Input
                          type="number"
                          placeholder="7"
                          value={formData.deliveryDays || ''}
                          onChange={(e) => setFormData({ ...formData, deliveryDays: parseInt(e.target.value) || 1 })}
                          className="border-2 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20 bg-white/90 text-gray-900 placeholder-gray-400 rounded-xl h-14 text-lg font-semibold transition-all duration-300"
                          min="1"
                          required
                        />
                        <div className="absolute right-4 top-1/2 transform -translate-y-1/2 text-blue-600 font-medium">days</div>
                      </div>
                      <div className="mt-4 p-4 bg-blue-50 rounded-xl border border-blue-100">
                        <p className="text-sm text-blue-700 font-medium">
                          ⚡ Quick delivery can help you stand out
                        </p>
                        <p className="text-xs text-blue-600 mt-1">Consider buffer time for revisions</p>
                      </div>
                    </div>
                  </motion.div>
                </div>

                {/* Portfolio Links */}
                <motion.div 
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.5, duration: 0.6 }}
                  className="bg-white/85 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-8 relative overflow-hidden"
                >
                  <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-purple-500/10 to-transparent rounded-full"></div>
                  
                  <div className="relative z-10">
                    <div className="flex items-center justify-between mb-6">
                      <div className="flex items-center space-x-4">
                        <motion.div 
                          whileHover={{ scale: 1.1, rotate: 5 }}
                          className="p-3 bg-gradient-to-br from-purple-500 to-pink-600 rounded-2xl text-white shadow-lg"
                        >
                          <LinkIcon className="h-6 w-6" />
                        </motion.div>
                        <div>
                          <h3 className="text-xl font-bold text-gray-900">Portfolio Links</h3>
                          <p className="text-gray-600 text-sm">Show your best work</p>
                        </div>
                      </div>
                      <motion.div
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                      >
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={addPortfolioLink}
                          disabled={portfolioLinks.length >= 5}
                          className="border-2 border-purple-200 text-purple-700 hover:bg-purple-50 hover:border-purple-300 rounded-xl px-4 py-2 font-medium transition-all duration-300"
                        >
                          <Plus className="h-4 w-4 mr-2" />
                          Add Link
                        </Button>
                      </motion.div>
                    </div>

                    <div className="space-y-4">
                      {portfolioLinks.map((link, index) => (
                        <motion.div 
                          key={index}
                          initial={{ opacity: 0, x: -20 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ delay: index * 0.1, duration: 0.4 }}
                          className="flex items-center space-x-3"
                        >
                          <div className="flex-1 relative">
                            <LinkIcon className="absolute left-4 top-1/2 transform -translate-y-1/2 h-4 w-4 text-purple-400" />
                            <Input
                              type="url"
                              placeholder="https://your-portfolio.com"
                              value={link}
                              onChange={(e) => handlePortfolioLinkChange(index, e.target.value)}
                              className="pl-12 border-2 border-gray-200 focus:border-purple-500 focus:ring-purple-500/20 bg-white/90 text-gray-900 placeholder-gray-400 rounded-xl h-12 transition-all duration-300"
                            />
                          </div>
                          {portfolioLinks.length > 1 && (
                            <motion.div
                              whileHover={{ scale: 1.1 }}
                              whileTap={{ scale: 0.9 }}
                            >
                              <Button
                                type="button"
                                variant="ghost"
                                size="sm"
                                onClick={() => removePortfolioLink(index)}
                                className="text-red-500 hover:text-red-700 hover:bg-red-50 p-2 rounded-xl"
                              >
                                <X className="h-4 w-4" />
                              </Button>
                            </motion.div>
                          )}
                        </motion.div>
                      ))}
                    </div>
                    <div className="mt-4 p-4 bg-purple-50 rounded-xl border border-purple-100">
                      <p className="text-sm text-purple-700 font-medium">
                        🎨 Share links to your relevant work (optional, max 5)
                      </p>
                      <p className="text-xs text-purple-600 mt-1">Include projects similar to this job for better chances</p>
                    </div>
                  </div>
                </motion.div>

                {/* Additional Notes */}
                <motion.div 
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.6, duration: 0.6 }}
                  className="bg-white/85 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-8 relative overflow-hidden"
                >
                  <div className="absolute top-0 right-0 w-28 h-28 bg-gradient-to-bl from-indigo-500/10 to-transparent rounded-full"></div>
                  
                  <div className="relative z-10">
                    <div className="flex items-center space-x-4 mb-6">
                      <motion.div 
                        whileHover={{ scale: 1.1, rotate: 5 }}
                        className="p-3 bg-gradient-to-br from-indigo-500 to-blue-600 rounded-2xl text-white shadow-lg"
                      >
                        <Sparkles className="h-6 w-6" />
                      </motion.div>
                      <div>
                        <h3 className="text-xl font-bold text-gray-900">Additional Notes</h3>
                        <p className="text-gray-600 text-sm">Anything else to add?</p>
                      </div>
                    </div>
                    
                    <Textarea
                      placeholder="💡 Any additional information you'd like to share...

• Special skills or certifications
• Questions about the project
• Your availability
• Previous similar work experience"
                      value={formData.additionalNotes}
                      onChange={(e) => setFormData({ ...formData, additionalNotes: e.target.value })}
                      className="min-h-[140px] resize-none border-2 border-gray-200 focus:border-indigo-500 focus:ring-indigo-500/20 bg-white/90 text-gray-900 placeholder-gray-500 rounded-2xl leading-relaxed transition-all duration-300"
                    />
                  </div>
                </motion.div>

                {/* Submit Button */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.7, duration: 0.6 }}
                  className="flex justify-end space-x-6 pt-4"
                >
                  <motion.div
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => router.back()}
                      className="px-8 py-4 border-2 border-gray-300 text-gray-700 hover:bg-gray-50 hover:border-gray-400 rounded-2xl font-semibold transition-all duration-300"
                    >
                      Cancel
                    </Button>
                  </motion.div>
                  <motion.div
                    whileHover={{ scale: 1.05, boxShadow: "0 20px 40px rgba(59, 130, 246, 0.3)" }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <Button
                      type="submit"
                      disabled={submitting}
                      className="px-10 py-4 bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 hover:from-blue-700 hover:via-purple-700 hover:to-indigo-700 text-white shadow-2xl rounded-2xl font-bold text-lg transition-all duration-300"
                    >
                      {submitting ? (
                        <>
                          <motion.div
                            className="w-5 h-5 border-2 border-white border-t-transparent rounded-full mr-3"
                            animate={{ rotate: 360 }}
                            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                          />
                          Submitting Proposal...
                        </>
                      ) : (
                        <>
                          <Send className="h-5 w-5 mr-3" />
                          Submit Proposal
                          <Zap className="h-4 w-4 ml-2" />
                        </>
                      )}
                    </Button>
                  </motion.div>
                </motion.div>
              </form>
            </motion.div>
          </div>
        </div>
      </div>
    </div>
  );
}

// Wrap the component with Suspense to handle useSearchParams
function ProposalPageWithSuspense() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <ProposalPage />
    </Suspense>
  );
}

export default ProposalPageWithSuspense;
