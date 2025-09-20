'use client';

import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { 
  Search, 
  MapPin, 
  Star, 
  DollarSign, 
  User, 
  Send, 
  Filter,
  Clock,
  Award
} from 'lucide-react';
import Link from 'next/link';
import { useAppSelector } from '@/store/hooks';
import InviteModal from '@/components/InviteModal';

interface Freelancer {
  id: number;
  name: string;
  email: string;
  handle?: string;
  profilePictureUrl?: string;
  headline?: string;
  bio?: string;
  locationText?: string;
  hourlyRateCents?: number;
  currency?: string;
  skills?: string[];
  reviewAvg?: number;
  reviewsCount?: number;
  deliveryScore?: number;
}

interface Job {
  id: number;
  title: string;
}

const FreelancersPage = () => {
  const { user } = useAppSelector((state) => state.auth);
  const [freelancers, setFreelancers] = useState<Freelancer[]>([]);
  const [clientJobs, setClientJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSkill, setSelectedSkill] = useState('');
  
  // Invite modal states
  const [inviteModal, setInviteModal] = useState<{
    isOpen: boolean;
    freelancer: Freelancer | null;
    selectedJobId: number | null;
  }>({
    isOpen: false,
    freelancer: null,
    selectedJobId: null
  });

  useEffect(() => {
    fetchFreelancers();
    if (user?.role === 'client') {
      fetchClientJobs();
    }
  }, [user]);

  const fetchFreelancers = async () => {
    try {
      setLoading(true);
      // Mock data for now - replace with actual API call
      const mockFreelancers: Freelancer[] = [
        {
          id: 2,
          name: 'Khalid Hasan Tuhin',
          email: 'tuhin@gmail.com',
          handle: 'tuhin123',
          headline: 'UI/UX Designer',
          bio: 'Creative designer with 5+ years experience in web and mobile design',
          locationText: 'Dhaka, Bangladesh',
          hourlyRateCents: 6000,
          currency: 'USD',
          skills: ['Figma', 'Adobe XD', 'Photoshop'],
          reviewAvg: 5,
          reviewsCount: 1,
          deliveryScore: 0,
          profilePictureUrl: 'https://res.cloudinary.com/dzc2cftmz/image/upload/v1758209464/profile-pictures/file_k2yvey.jpg'
        },
        {
          id: 3,
          name: 'Sarah Johnson',
          email: 'sarah@example.com',
          handle: 'sarahj',
          headline: 'Full Stack Developer',
          bio: 'Experienced developer specializing in React, Node.js, and cloud technologies',
          locationText: 'New York, USA',
          hourlyRateCents: 8000,
          currency: 'USD',
          skills: ['React', 'Node.js', 'AWS', 'TypeScript'],
          reviewAvg: 4.8,
          reviewsCount: 25,
          deliveryScore: 98
        },
        {
          id: 4,
          name: 'Ahmed Hassan',
          email: 'ahmed@example.com',
          handle: 'ahmedh',
          headline: 'Mobile App Developer',
          bio: 'iOS and Android developer with expertise in Flutter and React Native',
          locationText: 'Cairo, Egypt',
          hourlyRateCents: 5500,
          currency: 'USD',
          skills: ['Flutter', 'React Native', 'iOS', 'Android'],
          reviewAvg: 4.9,
          reviewsCount: 18,
          deliveryScore: 95
        }
      ];
      
      setFreelancers(mockFreelancers);
    } catch (error) {
      console.error('Error fetching freelancers:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchClientJobs = async () => {
    try {
      // Mock data for now - replace with actual API call
      const mockJobs: Job[] = [
        { id: 3, title: 'Build a Modern E-commerce Website' },
        { id: 4, title: 'Mobile App Development for Food Delivery' },
        { id: 5, title: 'UI/UX Design for SaaS Platform' }
      ];
      
      setClientJobs(mockJobs);
    } catch (error) {
      console.error('Error fetching client jobs:', error);
    }
  };

  const handleInviteClick = (freelancer: Freelancer) => {
    if (clientJobs.length === 0) {
      alert('You need to post a job first before sending invitations.');
      return;
    }
    
    setInviteModal({
      isOpen: true,
      freelancer,
      selectedJobId: clientJobs[0].id // Default to first job
    });
  };

  const filteredFreelancers = freelancers.filter(freelancer => {
    const matchesSearch = freelancer.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         freelancer.headline?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         freelancer.skills?.some(skill => skill.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesSkill = !selectedSkill || freelancer.skills?.includes(selectedSkill);
    
    return matchesSearch && matchesSkill;
  });

  const allSkills = Array.from(new Set(freelancers.flatMap(f => f.skills || [])));

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-12">
          <motion.h1
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-4xl font-bold text-gray-900 mb-4"
          >
            Find Talented Freelancers
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="text-xl text-gray-600 max-w-2xl mx-auto"
          >
            Connect with skilled professionals ready to bring your projects to life
          </motion.p>
        </div>

        {/* Search and Filters */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="bg-white rounded-2xl shadow-lg p-6 mb-8"
        >
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
              <input
                type="text"
                placeholder="Search freelancers by name, skills, or expertise..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            
            <div className="md:w-64">
              <select
                value={selectedSkill}
                onChange={(e) => setSelectedSkill(e.target.value)}
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">All Skills</option>
                {allSkills.map(skill => (
                  <option key={skill} value={skill}>{skill}</option>
                ))}
              </select>
            </div>
          </div>
        </motion.div>

        {/* Freelancers Grid */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {filteredFreelancers.map((freelancer, index) => (
              <motion.div
                key={freelancer.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 * index }}
                className="bg-white rounded-2xl shadow-lg hover:shadow-xl transition-all duration-300 overflow-hidden"
              >
                {/* Profile Header */}
                <div className="p-6 text-center">
                  <div className="relative inline-block mb-4">
                    <img
                      src={freelancer.profilePictureUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(freelancer.name)}&background=random`}
                      alt={freelancer.name}
                      className="w-20 h-20 rounded-full object-cover border-4 border-white shadow-lg"
                    />
                  </div>
                  
                  <h3 className="text-xl font-bold text-gray-900 mb-1">{freelancer.name}</h3>
                  <p className="text-blue-600 font-medium mb-2">{freelancer.headline}</p>
                  
                  {freelancer.locationText && (
                    <div className="flex items-center justify-center text-gray-500 text-sm mb-4">
                      <MapPin className="h-4 w-4 mr-1" />
                      {freelancer.locationText}
                    </div>
                  )}
                </div>

                {/* Stats */}
                <div className="px-6 pb-4">
                  <div className="grid grid-cols-3 gap-4 text-center">
                    <div>
                      <div className="flex items-center justify-center text-yellow-500 mb-1">
                        <Star className="h-4 w-4 mr-1" />
                        <span className="font-semibold">{freelancer.reviewAvg || 0}</span>
                      </div>
                      <div className="text-xs text-gray-500">{freelancer.reviewsCount || 0} reviews</div>
                    </div>
                    
                    <div>
                      <div className="flex items-center justify-center text-green-600 mb-1">
                        <DollarSign className="h-4 w-4" />
                        <span className="font-semibold">{freelancer.hourlyRateCents ? (freelancer.hourlyRateCents / 100) : 0}</span>
                      </div>
                      <div className="text-xs text-gray-500">per hour</div>
                    </div>
                    
                    <div>
                      <div className="flex items-center justify-center text-blue-600 mb-1">
                        <Award className="h-4 w-4 mr-1" />
                        <span className="font-semibold">{freelancer.deliveryScore || 0}%</span>
                      </div>
                      <div className="text-xs text-gray-500">delivery</div>
                    </div>
                  </div>
                </div>

                {/* Skills */}
                {freelancer.skills && (
                  <div className="px-6 pb-4">
                    <div className="flex flex-wrap gap-2">
                      {freelancer.skills.slice(0, 3).map((skill, skillIndex) => (
                        <span
                          key={skillIndex}
                          className="px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded-full"
                        >
                          {skill}
                        </span>
                      ))}
                      {freelancer.skills.length > 3 && (
                        <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                          +{freelancer.skills.length - 3} more
                        </span>
                      )}
                    </div>
                  </div>
                )}

                {/* Actions */}
                <div className="px-6 pb-6 flex gap-3">
                  <Link href={`/profile/${freelancer.id}`} className="flex-1">
                    <button className="w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors flex items-center justify-center gap-2">
                      <User className="h-4 w-4" />
                      View Profile
                    </button>
                  </Link>
                  
                  {user?.role === 'client' && (
                    <button
                      onClick={() => handleInviteClick(freelancer)}
                      className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
                    >
                      <Send className="h-4 w-4" />
                      Invite
                    </button>
                  )}
                </div>
              </motion.div>
            ))}
          </div>
        )}

        {filteredFreelancers.length === 0 && !loading && (
          <div className="text-center py-20">
            <User className="h-16 w-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No freelancers found</h3>
            <p className="text-gray-600">Try adjusting your search criteria or browse all freelancers.</p>
          </div>
        )}
      </div>

      {/* Invite Modal */}
      {inviteModal.isOpen && inviteModal.freelancer && inviteModal.selectedJobId && (
        <InviteModal
          isOpen={inviteModal.isOpen}
          onClose={() => setInviteModal({ isOpen: false, freelancer: null, selectedJobId: null })}
          jobId={inviteModal.selectedJobId}
          jobTitle={clientJobs.find(job => job.id === inviteModal.selectedJobId)?.title || 'Unknown Job'}
          freelancerId={inviteModal.freelancer.id}
          freelancerName={inviteModal.freelancer.name}
          freelancerHandle={inviteModal.freelancer.handle}
          onInviteSent={() => {
            console.log('Invitation sent successfully!');
            // Optionally refresh data or show notification
          }}
        />
      )}
    </div>
  );
};

export default FreelancersPage;