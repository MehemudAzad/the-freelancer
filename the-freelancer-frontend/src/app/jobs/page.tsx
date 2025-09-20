

'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Search, 
  Filter, 
  MapPin, 
  DollarSign, 
  Calendar, 
  Plus, 
  Briefcase, 
  TrendingUp, 
  Zap, 
  Star, 
  Award, 
  Users, 
  Sparkles,
  ChevronDown,
  Code,
  Palette,
  PenTool,
  Globe,
  Database,
  Camera,
  Target,
  Wand2,
  Loader2
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import JobList from '@/components/JobList';
import jobService from '@/services/job';
import { Job } from '@/types/api';
import { useAppSelector } from '@/store/hooks';

// Button component for clients to post a job
const ClientPostJobButton = () => {
  const router = useRouter();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  
  // Only show the button if user is authenticated and is a client
  if (!isAuthenticated || user?.role !== 'client') {
    return null;
  }
  
  return (
    <Button 
      className="bg-blue-600 hover:bg-blue-700 text-white"
      onClick={() => router.push('/jobs/post')}
    >
      <Plus className="mr-2 h-4 w-4" />
      Post a Job
    </Button>
  );
};

export default function JobsPage() {
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [budgetRange, setBudgetRange] = useState('');
  const [showMobileFilters, setShowMobileFilters] = useState(false);
  
  // AI Recommendations State
  const [recommendations, setRecommendations] = useState<Job[]>([]);
  const [loadingRecommendations, setLoadingRecommendations] = useState(false);
  const [showRecommendations, setShowRecommendations] = useState(false);
  
  // Tab state
  const [activeTab, setActiveTab] = useState<'available' | 'recommended'>('available');

  useEffect(() => {
    fetchJobs();
  }, [selectedCategory, budgetRange]);

  const fetchJobs = async () => {
    try {
      setLoading(true);
      const response = await jobService.jobAPI.getAllJobs();
      console.log('Jobs API Response:', response);
      // Handle both paginated and direct array responses
      const jobsData = Array.isArray(response) ? response : response.content || [];
      console.log('Jobs data structure:', jobsData.length > 0 ? jobsData[0] : 'No jobs');
      setJobs(jobsData);
    } catch (error) {
      console.error('Error fetching jobs:', error);
      setJobs([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    // For now, just filter locally since there's no search endpoint
    // In a real app, you'd call a search API
    fetchJobs();
  };

  const fetchRecommendations = async () => {
    if (!isAuthenticated || !user || user.role?.toLowerCase() !== 'freelancer') {
      return;
    }

    setLoadingRecommendations(true);

    try {
      const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : '';
      
      const response = await fetch(`/api/recommendations/${user.id}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch recommendations');
      }

      const data = await response.json();
      console.log('AI recommendations response:', data);
      
      if (data.recommendations && Array.isArray(data.recommendations)) {
        // Extract job IDs from AI recommendations
        const recommendedJobIds = data.recommendations.map((rec: any) => {
          // Handle different possible structures of the AI response
          const jobId = rec.jobId || rec.id || rec.job_id || rec;
          // Convert to number if it's a string, as job.id is number
          return typeof jobId === 'string' ? parseInt(jobId, 10) : jobId;
        }).filter((id: any) => id !== null && id !== undefined && !isNaN(id)); // Remove invalid values

        console.log('Recommended job IDs:', recommendedJobIds);
        console.log('Available jobs count:', jobs.length);
        console.log('Available job IDs:', jobs.map(job => job.id));

        // Filter existing jobs to match the recommended job IDs
        const matchedRecommendedJobs = jobs.filter(job => {
          // Use the job.id field as defined in the Job interface
          const isMatch = recommendedJobIds.includes(job.id);
          if (isMatch) {
            console.log(`Match found: Job ID ${job.id} - "${job.projectName || job.title}"`);
          }
          return isMatch;
        });

        console.log(`Matched ${matchedRecommendedJobs.length} jobs out of ${recommendedJobIds.length} recommendations`);

        // Set the filtered jobs with complete details
        setRecommendations(matchedRecommendedJobs);
        setShowRecommendations(true);

        // Show message if no matches found
        if (matchedRecommendedJobs.length === 0) {
          console.warn('AI returned recommendations but no matching jobs found in current job list');
          console.warn('This could happen if:');
          console.warn('1. The recommended jobs are not in the current page/filter');
          console.warn('2. The job IDs from AI don\'t match the database IDs');
          console.warn('3. The recommended jobs have been removed or are no longer active');
        }
      } else {
        throw new Error('No recommendations received from AI');
      }
    } catch (error) {
      console.error('Error fetching recommendations:', error);
      // You could add a toast notification here
    } finally {
      setLoadingRecommendations(false);
    }
  };

  const categories = [
    { name: 'All Categories', icon: Sparkles, gradient: 'from-purple-400 to-pink-400' },
    { name: 'Web Development', icon: Code, gradient: 'from-blue-400 to-cyan-400' },
    { name: 'Mobile Development', icon: Zap, gradient: 'from-green-400 to-blue-400' },
    { name: 'AI & Machine Learning', icon: TrendingUp, gradient: 'from-violet-400 to-purple-400' },
    { name: 'Graphic Design', icon: Palette, gradient: 'from-pink-400 to-rose-400' },
    { name: 'Digital Marketing', icon: Target, gradient: 'from-orange-400 to-red-400' },
    { name: 'Writing & Translation', icon: PenTool, gradient: 'from-indigo-400 to-purple-400' },
    { name: 'Video & Animation', icon: Camera, gradient: 'from-yellow-400 to-orange-400' },
    { name: 'Music & Audio', icon: Star, gradient: 'from-teal-400 to-cyan-400' },
    { name: 'Data Science', icon: Database, gradient: 'from-violet-400 to-purple-400' },
    { name: 'Business', icon: Award, gradient: 'from-emerald-400 to-green-400' },
    { name: 'Photography', icon: Camera, gradient: 'from-rose-400 to-pink-400' }
  ];

  const budgetRanges = [
    'All Budgets',
    'Under $100',
    '$100 - $500',
    '$500 - $1,000',
    '$1,000 - $5,000',
    '$5,000+'
  ];

  const filteredJobs = jobs.filter(job => {
    const matchesSearch = !searchQuery || 
      (job.title || job.projectName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (job.description || '').toLowerCase().includes(searchQuery.toLowerCase());
    
    // Fix category filtering to match actual job categories
    const matchesCategory = !selectedCategory || selectedCategory === 'All Categories' || 
      job.category?.toLowerCase() === selectedCategory.toLowerCase();
    
    let matchesBudget = !budgetRange || budgetRange === 'All Budgets';
    if (budgetRange && budgetRange !== 'All Budgets') {
      // Use the actual budget fields from your database (minBudgetCents and maxBudgetCents)
      const minBudget = job.minBudgetCents ? job.minBudgetCents / 100 : 0; // Convert cents to dollars
      const maxBudget = job.maxBudgetCents ? job.maxBudgetCents / 100 : 0; // Convert cents to dollars
      const avgBudget = (minBudget + maxBudget) / 2; // Use average for filtering
      
      // Also check legacy budget field as fallback
      const budgetToCheck = avgBudget > 0 ? avgBudget : (job.budget || 0);
      
      switch (budgetRange) {
        case 'Under $100':
          matchesBudget = budgetToCheck < 100;
          break;
        case '$100 - $500':
          matchesBudget = budgetToCheck >= 100 && budgetToCheck <= 500;
          break;
        case '$500 - $1,000':
          matchesBudget = budgetToCheck >= 500 && budgetToCheck <= 1000;
          break;
        case '$1,000 - $5,000':
          matchesBudget = budgetToCheck >= 1000 && budgetToCheck <= 5000;
          break;
        case '$5,000+':
          matchesBudget = budgetToCheck >= 5000;
          break;
      }
      
      // Debug logging for budget filtering
      if (job.id === 1) { // Log first job for debugging
        console.log('Budget filtering debug:', {
          jobId: job.id,
          minBudgetCents: job.minBudgetCents,
          maxBudgetCents: job.maxBudgetCents,
          minBudget,
          maxBudget,
          avgBudget,
          budgetToCheck,
          budgetRange,
          matchesBudget
        });
      }
    }
    
    // Debug logging for category filtering
    if (selectedCategory && selectedCategory !== 'All Categories' && job.id === 1) {
      console.log('Category filtering debug:', {
        jobId: job.id,
        jobCategory: job.category,
        selectedCategory,
        matchesCategory
      });
    }
    
    return matchesSearch && matchesCategory && matchesBudget;
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50">
      {/* Animated Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-r from-emerald-600 via-blue-600 to-indigo-700">
        {/* Animated background elements */}
        <div className="absolute inset-0">
          <div className="absolute top-0 left-0 w-full h-full">
            <motion.div
              animate={{
                scale: [1, 1.2, 1],
                opacity: [0.3, 0.5, 0.3],
              }}
              transition={{
                duration: 8,
                repeat: Infinity,
                ease: "easeInOut"
              }}
              className="absolute top-20 left-20 w-64 h-64 bg-white/10 rounded-full blur-xl"
            />
            <motion.div
              animate={{
                scale: [1.2, 1, 1.2],
                opacity: [0.2, 0.4, 0.2],
              }}
              transition={{
                duration: 6,
                repeat: Infinity,
                ease: "easeInOut"
              }}
              className="absolute bottom-20 right-20 w-96 h-96 bg-cyan-300/20 rounded-full blur-2xl"
            />
          </div>
        </div>
        
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-center"
          >
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="flex items-center justify-center mb-6"
            >
              <Briefcase className="h-8 w-8 text-yellow-300 mr-3" />
              <span className="text-yellow-300 font-semibold text-lg">Premium Opportunities</span>
              <Briefcase className="h-8 w-8 text-yellow-300 ml-3" />
            </motion.div>
            
            <motion.h1 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.3 }}
              className="text-5xl md:text-7xl font-bold mb-8 text-white leading-tight"
            >
              Find Your Dream
              <span className="bg-gradient-to-r from-yellow-300 via-emerald-300 to-cyan-300 bg-clip-text text-transparent block">
                Project Today
              </span>
            </motion.h1>
            
            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.5 }}
              className="text-xl md:text-2xl mb-12 text-blue-100 max-w-3xl mx-auto leading-relaxed"
            >
              Discover amazing job opportunities from clients worldwide and take your freelance career to the next level
            </motion.p>

            {/* Enhanced Search Form */}
            <motion.form 
              initial={{ opacity: 0, y: 30, scale: 0.9 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{ duration: 0.8, delay: 0.7 }}
              onSubmit={handleSearch} 
              className="max-w-4xl mx-auto"
            >
              <div className="relative bg-white/10 backdrop-blur-lg rounded-3xl p-3 shadow-2xl border border-white/20">
                <div className="flex flex-col md:flex-row gap-4">
                  <div className="flex-1 relative">
                    <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 h-6 w-6 text-gray-400" />
                    <Input
                      type="text"
                      placeholder="What kind of project are you looking for?"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-12 pr-4 py-4 bg-white/90 backdrop-blur-sm text-gray-800 placeholder-gray-500 text-lg rounded-2xl border-0 focus:ring-4 focus:ring-white/30 shadow-lg transition-all duration-300"
                    />
                  </div>
                  <Button
                    type="submit"
                    size="lg"
                    className="bg-gradient-to-r from-emerald-500 to-blue-500 hover:from-emerald-600 hover:to-blue-600 text-white px-8 py-4 rounded-2xl font-bold text-lg shadow-lg hover:shadow-2xl transition-all duration-300 hover:scale-105"
                  >
                    <Search className="mr-2 h-6 w-6" />
                    Search Jobs
                  </Button>
                </div>
              </div>
            </motion.form>

            {/* Post Job Button for Hero Section */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.9 }}
              className="mt-8"
            >
              <ClientPostJobButton />
            </motion.div>

            {/* Stats */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 1.0 }}
              className="grid grid-cols-2 md:grid-cols-4 gap-8 mt-16 max-w-4xl mx-auto"
            >
              {[
                { number: '15K+', label: 'Active Jobs' },
                { number: '8K+', label: 'Happy Freelancers' },
                { number: '92%', label: 'Success Rate' },
                { number: '24/7', label: 'Support' }
              ].map((stat, index) => (
                <motion.div
                  key={index}
                  whileHover={{ scale: 1.05 }}
                  className="text-center bg-white/10 backdrop-blur-sm rounded-2xl p-6 border border-white/20"
                >
                  <div className="text-3xl font-bold text-white mb-2">{stat.number}</div>
                  <div className="text-blue-200">{stat.label}</div>
                </motion.div>
              ))}
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Enhanced Filters and Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Enhanced Sidebar Filters */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="lg:w-80"
          >
            <div className="bg-white/90 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-8 sticky top-8 overflow-hidden">
              {/* Background decoration */}
              <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-emerald-100/50 to-transparent rounded-full -translate-y-8 translate-x-8" />
              <div className="absolute bottom-0 left-0 w-24 h-24 bg-gradient-to-tr from-blue-100/50 to-transparent rounded-full translate-y-4 -translate-x-4" />
              
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                className="flex items-center justify-between mb-8 relative z-10"
              >
                <h3 className="font-bold text-2xl flex items-center bg-gradient-to-r from-emerald-600 to-blue-600 bg-clip-text text-transparent">
                  <Filter className="mr-3 h-6 w-6 text-emerald-500" />
                  Filters
                </h3>
                <Button
                  variant="ghost"
                  className="lg:hidden"
                  onClick={() => setShowMobileFilters(!showMobileFilters)}
                >
                  <ChevronDown className={`h-5 w-5 transition-transform ${showMobileFilters ? 'rotate-180' : ''}`} />
                </Button>
              </motion.div>
              
              <AnimatePresence>
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: 'auto' }}
                  className={`space-y-6 relative z-10 ${showMobileFilters ? 'block' : 'hidden lg:block'}`}
                >
                  {/* Categories */}
                  <div>
                    <h4 className="font-bold text-lg mb-4 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">Categories</h4>
                    <div className="space-y-3">
                      {[ ...categories.map(cat => cat.name)].map((category, index) => {
                        const categoryData = categories.find(cat => cat.name === category);
                        const IconComponent = categoryData?.icon || Target;
                        const isSelected = (selectedCategory === category) || (selectedCategory === '' && category === 'All Categories');
                        
                        return (
                          <motion.button
                            key={category}
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.4, delay: index * 0.05 + 0.3 }}
                            whileHover={{ 
                              scale: 1.02, 
                              x: 6,
                              transition: { duration: 0.2 }
                            }}
                            whileTap={{ scale: 0.98 }}
                            onClick={() => setSelectedCategory(category === 'All Categories' ? '' : category)}
                            className={`w-full text-left px-5 py-4 rounded-2xl transition-all duration-300 group relative overflow-hidden border ${
                              isSelected
                                ? 'bg-gradient-to-r from-blue-400 to-purple-400 text-white shadow-xl border-transparent transform scale-[1.02]'
                                : 'text-gray-700 hover:text-white bg-white hover:bg-gradient-to-r hover:from-blue-400 hover:to-purple-400 border-gray-200/50 hover:border-transparent shadow-sm hover:shadow-lg'
                            }`}
                          >
                            {/* Animated shimmer effect */}
                            <motion.div
                              animate={{
                                x: isSelected ? [-100, 300] : [-100, -100],
                              }}
                              transition={{
                                duration: 2,
                                repeat: isSelected ? Infinity : 0,
                                ease: "easeInOut"
                              }}
                              className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent skew-x-12"
                            />
                            
                            <div className="flex items-center relative z-10">
                              <motion.div
                                animate={{
                                  rotate: isSelected ? [0, 10, -10, 0] : 0,
                                  scale: isSelected ? [1, 1.1, 1] : 1
                                }}
                                transition={{
                                  duration: 0.5,
                                  ease: "easeInOut"
                                }}
                              >
                                <IconComponent className={`h-5 w-5 mr-4 transition-all duration-300 ${
                                  isSelected ? 'text-white' : 'text-gray-500 group-hover:text-white'
                                }`} />
                              </motion.div>
                              <span className="font-semibold text-base">{category}</span>
                            </div>
                            
                            {/* Gradient background overlay */}
                            <div className="absolute inset-0 bg-gradient-to-r from-blue-400 to-purple-400 opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-2xl" />
                          </motion.button>
                        );
                      })}
                    </div>
                  </div>

                  {/* Budget Range */}
                  <div>
                    <h4 className="font-bold text-lg mb-4 bg-gradient-to-r from-green-600 to-emerald-600 bg-clip-text text-transparent">Budget Range</h4>
                    <div className="space-y-3">
                      {budgetRanges.map((range, index) => (
                        <motion.button
                          key={range}
                          initial={{ opacity: 0, x: -20 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.4, delay: index * 0.06 + 0.4 }}
                          whileHover={{ 
                            scale: 1.02, 
                            x: 6,
                            transition: { duration: 0.2 }
                          }}
                          whileTap={{ scale: 0.98 }}
                          onClick={() => setBudgetRange(range === 'All Budgets' ? '' : range)}
                          className={`w-full text-left px-5 py-4 rounded-2xl transition-all duration-300 group relative overflow-hidden border ${
                            (budgetRange === range) || (budgetRange === '' && range === 'All Budgets')
                              ? 'bg-gradient-to-r from-green-400 to-emerald-400 text-white shadow-xl border-transparent transform scale-[1.02]'
                              : 'text-gray-700 hover:text-white bg-white hover:bg-gradient-to-r hover:from-green-400 hover:to-emerald-400 border-gray-200/50 hover:border-transparent shadow-sm hover:shadow-lg'
                          }`}
                        >
                          {/* Animated shimmer effect */}
                          <motion.div
                            animate={{
                              x: (budgetRange === range) || (budgetRange === '' && range === 'All Budgets') ? [-100, 300] : [-100, -100],
                            }}
                            transition={{
                              duration: 2,
                              repeat: (budgetRange === range) || (budgetRange === '' && range === 'All Budgets') ? Infinity : 0,
                              ease: "easeInOut"
                            }}
                            className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent skew-x-12"
                          />
                          
                          <div className="flex items-center relative z-10">
                            <motion.div
                              animate={{
                                rotate: (budgetRange === range) || (budgetRange === '' && range === 'All Budgets') ? [0, 10, -10, 0] : 0,
                                scale: (budgetRange === range) || (budgetRange === '' && range === 'All Budgets') ? [1, 1.1, 1] : 1
                              }}
                              transition={{
                                duration: 0.5,
                                ease: "easeInOut"
                              }}
                            >
                              <DollarSign className={`h-5 w-5 mr-4 transition-all duration-300 ${
                                (budgetRange === range) || (budgetRange === '' && range === 'All Budgets')
                                  ? 'text-white'
                                  : 'text-gray-500 group-hover:text-white'
                              }`} />
                            </motion.div>
                            <span className="font-semibold text-base">{range}</span>
                          </div>
                          
                          {/* Gradient background overlay */}
                          <div className="absolute inset-0 bg-gradient-to-r from-green-400 to-emerald-400 opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-2xl" />
                        </motion.button>
                      ))}
                    </div>
                  </div>
                </motion.div>
              </AnimatePresence>
            </div>
          </motion.div>

          {/* Enhanced Main Content */}
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="flex-1"
          >
            {/* Enhanced Header */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.5 }}
              className="flex items-center justify-between mb-8"
            >
              <div className="bg-white/70 backdrop-blur-lg rounded-2xl p-6 shadow-lg border border-white/20">
                <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-900 via-blue-900 to-purple-900 bg-clip-text text-transparent mb-2">
                  Available Opportunities
                </h2>
                <p className="text-gray-600 flex items-center">
                  <Briefcase className="h-5 w-5 mr-2 text-emerald-500" />
                  {filteredJobs.length} job{filteredJobs.length !== 1 ? 's' : ''} found
                </p>
              </div>
              
              {/* Enhanced Post Job Button */}
              <motion.div
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                className="hidden md:block"
              >
                <ClientPostJobButton />
              </motion.div>
            </motion.div>

            {/* Tabbed Interface */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.6, delay: 0.6 }}
              className="bg-white/70 backdrop-blur-lg rounded-3xl shadow-xl border border-white/20"
            >
              {/* Tab Navigation */}
              <div className="border-b border-gray-200/50 p-6">
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-gradient-to-r from-blue-500 to-purple-500 rounded-lg text-white">
                      <Briefcase className="h-6 w-6" />
                    </div>
                    <div>
                      <h2 className="text-2xl font-bold text-gray-900">Job Opportunities</h2>
                      <p className="text-gray-600">Discover the perfect job for you</p>
                    </div>
                  </div>
                  <div className="text-sm text-gray-500 bg-blue-50 px-3 py-1 rounded-full">
                    {activeTab === 'available' ? filteredJobs.length : recommendations.length} job{(activeTab === 'available' ? filteredJobs.length : recommendations.length) !== 1 ? 's' : ''} {activeTab === 'available' ? 'available' : 'recommended'}
                  </div>
                </div>

                {/* Tab Buttons */}
                <div className="flex space-x-1 bg-gray-100 rounded-2xl p-1">
                  <button
                    onClick={() => setActiveTab('available')}
                    className={`flex-1 flex items-center justify-center px-6 py-3 rounded-xl font-semibold transition-all duration-300 ${
                      activeTab === 'available'
                        ? 'bg-white text-emerald-600 shadow-lg transform scale-105'
                        : 'text-gray-600 hover:text-gray-800'
                    }`}
                  >
                    <Briefcase className="h-5 w-5 mr-2" />
                    Available Jobs
                    <span className="ml-2 bg-emerald-100 text-emerald-600 px-2 py-1 rounded-full text-xs">
                      {filteredJobs.length}
                    </span>
                  </button>
                  
                  {isAuthenticated && user?.role?.toLowerCase() === 'freelancer' && (
                    <button
                      onClick={() => {
                        setActiveTab('recommended');
                        // Auto-fetch recommendations if not already loaded
                        if (!showRecommendations && !loadingRecommendations) {
                          fetchRecommendations();
                        }
                      }}
                      className={`flex-1 flex items-center justify-center px-6 py-3 rounded-xl font-semibold transition-all duration-300 ${
                        activeTab === 'recommended'
                          ? 'bg-gradient-to-r from-purple-500 to-blue-500 text-white shadow-lg transform scale-105'
                          : 'text-gray-600 hover:text-gray-800'
                      }`}
                    >
                      <Wand2 className="h-5 w-5 mr-2" />
                      AI Recommended
                      <span className={`ml-2 px-2 py-1 rounded-full text-xs ${
                        activeTab === 'recommended' 
                          ? 'bg-white/20 text-white' 
                          : 'bg-purple-100 text-purple-600'
                      }`}>
                        {showRecommendations ? recommendations.length : '✨'}
                      </span>
                    </button>
                  )}
                </div>
              </div>

              {/* Tab Content */}
              <div className="p-6">
                {/* Available Jobs Tab */}
                {activeTab === 'available' && (
                  <motion.div
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3 }}
                  >
                    <div className="mb-4 flex items-center space-x-2 text-gray-600">
                      <Briefcase className="h-4 w-4" />
                      <span className="text-sm">Browse all current job opportunities</span>
                    </div>
                    <JobList 
                      jobs={filteredJobs} 
                      loading={loading}
                      onJobClick={(job) => {
                        console.log('Job clicked:', job.title);
                      }}
                    />
                  </motion.div>
                )}

                {/* AI Recommended Jobs Tab */}
                {activeTab === 'recommended' && isAuthenticated && user?.role?.toLowerCase() === 'freelancer' && (
                  <motion.div
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3 }}
                  >
                    {/* AI Tab Header */}
                    <div className="mb-6 p-4 bg-gradient-to-r from-purple-50 to-blue-50 rounded-2xl border border-purple-200/50">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <div className="p-2 bg-gradient-to-r from-purple-500 to-blue-500 rounded-lg text-white">
                            <Sparkles className="h-5 w-5" />
                          </div>
                          <div>
                            <h3 className="text-lg font-semibold text-gray-800">AI-Powered Job Matching</h3>
                            <p className="text-sm text-gray-600">Personalized recommendations based on your profile</p>
                          </div>
                        </div>
                        
                        {!showRecommendations && (
                          <Button
                            onClick={fetchRecommendations}
                            disabled={loadingRecommendations}
                            className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white px-6 py-2 rounded-lg font-medium transition-all duration-300"
                          >
                            {loadingRecommendations ? (
                              <>
                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                Analyzing...
                              </>
                            ) : (
                              <>
                                <Wand2 className="h-4 w-4 mr-2" />
                                Get Recommendations
                              </>
                            )}
                          </Button>
                        )}
                      </div>
                      
                      {/* AI Features */}
                      <div className="flex items-center space-x-4 mt-3 text-sm text-purple-600">
                        <div className="flex items-center space-x-1">
                          <Target className="h-4 w-4" />
                          <span>Skills matching</span>
                        </div>
                        <div className="flex items-center space-x-1">
                          <Zap className="h-4 w-4" />
                          <span>Smart analysis</span>
                        </div>
                        <div className="flex items-center space-x-1">
                          <Star className="h-4 w-4" />
                          <span>Best fit jobs</span>
                        </div>
                      </div>
                    </div>

                    {/* AI Content */}
                    {showRecommendations && recommendations.length > 0 && (
                      <div>
                        <div className="mb-4 flex items-center justify-between">
                          <div className="flex items-center space-x-2 text-gray-600">
                            <Sparkles className="h-4 w-4 text-purple-500" />
                            <span className="text-sm">
                              Found {recommendations.length} perfect match{recommendations.length > 1 ? 'es' : ''} for you
                            </span>
                          </div>
                          <button
                            onClick={fetchRecommendations}
                            disabled={loadingRecommendations}
                            className="text-purple-600 hover:text-purple-700 text-sm font-medium flex items-center space-x-1"
                          >
                            <Loader2 className={`h-4 w-4 ${loadingRecommendations ? 'animate-spin' : ''}`} />
                            <span>Refresh</span>
                          </button>
                        </div>
                        
                        <div className="bg-gradient-to-r from-purple-50/50 to-blue-50/50 rounded-2xl p-4">
                          <JobList 
                            jobs={recommendations} 
                            loading={false}
                            onJobClick={(job) => {
                              console.log('Recommended job clicked:', job.title);
                            }}
                          />
                        </div>
                      </div>
                    )}

                    {showRecommendations && recommendations.length === 0 && !loadingRecommendations && (
                      <div className="text-center py-12">
                        <div className="p-4 bg-gradient-to-br from-purple-100 to-blue-100 rounded-full w-20 h-20 mx-auto mb-6 flex items-center justify-center">
                          <Sparkles className="h-10 w-10 text-purple-500" />
                        </div>
                        <h3 className="text-xl font-semibold text-gray-800 mb-2">No Recommendations Found</h3>
                        <p className="text-gray-600 mb-4">Our AI couldn't find matching jobs at the moment.</p>
                        <p className="text-sm text-gray-500 mb-6">Try completing your profile or adding more skills to get better recommendations.</p>
                        <Button
                          onClick={fetchRecommendations}
                          className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white"
                        >
                          <Wand2 className="h-4 w-4 mr-2" />
                          Try Again
                        </Button>
                      </div>
                    )}

                    {!showRecommendations && !loadingRecommendations && (
                      <div className="text-center py-12">
                        <div className="p-4 bg-gradient-to-br from-purple-100 to-blue-100 rounded-full w-20 h-20 mx-auto mb-6 flex items-center justify-center">
                          <Wand2 className="h-10 w-10 text-purple-500" />
                        </div>
                        <h3 className="text-xl font-semibold text-gray-800 mb-2">Ready to Find Perfect Jobs?</h3>
                        <p className="text-gray-600 mb-6">Let our AI analyze your profile and discover opportunities tailored just for you!</p>
                        <Button
                          onClick={fetchRecommendations}
                          className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white px-8 py-3"
                        >
                          <Sparkles className="h-5 w-5 mr-2" />
                          Get AI Recommendations
                        </Button>
                      </div>
                    )}

                    {loadingRecommendations && (
                      <div className="text-center py-12">
                        <div className="p-4 bg-gradient-to-br from-purple-100 to-blue-100 rounded-full w-20 h-20 mx-auto mb-6 flex items-center justify-center">
                          <Loader2 className="h-10 w-10 text-purple-500 animate-spin" />
                        </div>
                        <h3 className="text-xl font-semibold text-gray-800 mb-2">Analyzing Your Profile...</h3>
                        <p className="text-gray-600">Our AI is finding the perfect job matches for you!</p>
                      </div>
                    )}
                  </motion.div>
                )}

                {/* Show message for non-freelancers when trying to access AI recommendations */}
                {activeTab === 'recommended' && (!isAuthenticated || user?.role?.toLowerCase() !== 'freelancer') && (
                  <motion.div
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3 }}
                    className="text-center py-12"
                  >
                    <div className="p-4 bg-gray-100 rounded-full w-20 h-20 mx-auto mb-6 flex items-center justify-center">
                      <Users className="h-10 w-10 text-gray-400" />
                    </div>
                    <h3 className="text-xl font-semibold text-gray-800 mb-2">AI Recommendations for Freelancers</h3>
                    <p className="text-gray-600">Please sign in as a freelancer to access personalized job recommendations.</p>
                  </motion.div>
                )}
              </div>
            </motion.div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
