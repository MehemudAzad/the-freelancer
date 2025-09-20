'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Search, 
  Filter, 
  Grid, 
  List, 
  Sparkles, 
  Star, 
  TrendingUp,
  Zap,
  Award,
  Users,
  ChevronDown,
  X
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import GigList from '@/components/GigList';
import gigService from '@/services/gig';
import { Gig } from '@/types/api';

export default function GigsPage() {
  const router = useRouter();
  const [gigs, setGigs] = useState<Gig[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showMobileFilters, setShowMobileFilters] = useState(false);
  const [isSemanticSearch, setIsSemanticSearch] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);

  useEffect(() => {
    fetchGigs();
  }, [selectedCategory]);

  const fetchGigs = async () => {
    try {
      setLoading(true);
      const response = await gigService.gigAPI.getAllGigs();
      console.log('Gigs API Response:', response);
      // Handle both paginated and direct array responses
      const gigsData = Array.isArray(response) ? response : (response?.content || []);
      console.log('Gigs data structure:', gigsData.length > 0 ? gigsData[0] : 'No gigs');
      // Ensure we always set an array
      setGigs(Array.isArray(gigsData) ? gigsData : []);
    } catch (error) {
      console.error('Error fetching gigs:', error);
      // Ensure we always have an array, even on error
      setGigs([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!searchQuery.trim()) {
      // If no search query, just fetch all gigs
      fetchGigs();
      return;
    }

    try {
      setSearchLoading(true);
      
      if (isSemanticSearch) {
        // Use semantic search
        console.log('🔍 Performing semantic search for:', searchQuery);
        const semanticResults = await gigService.gigAPI.semanticSearchGigs(searchQuery.trim());
        console.log('🔍 Semantic search results:', semanticResults);
        // Ensure we always set an array
        setGigs(Array.isArray(semanticResults) ? semanticResults : []);
      } else {
        // Use regular search (fallback to filter local results for now)
        fetchGigs();
      }
    } catch (error) {
      console.error('Search error:', error);
      // Fallback to regular fetch if search fails
      fetchGigs();
    } finally {
      setSearchLoading(false);
    }
  };

  const categories = [
    { name: 'All Categories', icon: Sparkles, gradient: 'from-purple-400 to-pink-400' },
    { name: 'Web Development', icon: TrendingUp, gradient: 'from-blue-400 to-cyan-400' },
    { name: 'Mobile Development', icon: Zap, gradient: 'from-green-400 to-blue-400' },
    { name: 'Graphic Design', icon: Star, gradient: 'from-pink-400 to-rose-400' },
    { name: 'Digital Marketing', icon: Award, gradient: 'from-orange-400 to-red-400' },
    { name: 'Writing & Translation', icon: Users, gradient: 'from-indigo-400 to-purple-400' },
    { name: 'Video & Animation', icon: Sparkles, gradient: 'from-yellow-400 to-orange-400' },
    { name: 'Music & Audio', icon: Star, gradient: 'from-teal-400 to-cyan-400' },
    { name: 'Data Science', icon: TrendingUp, gradient: 'from-violet-400 to-purple-400' },
    { name: 'Business', icon: Award, gradient: 'from-emerald-400 to-green-400' },
    { name: 'Photography', icon: Zap, gradient: 'from-rose-400 to-pink-400' },
    { name: 'Programming & Tech', icon: Users, gradient: 'from-blue-400 to-indigo-400' },
    { name: 'AI & Machine Learning', icon: Sparkles, gradient: 'from-purple-400 to-indigo-400' },
    { name: 'Game Development', icon: Star, gradient: 'from-cyan-400 to-blue-400' }
  ];

  const filteredGigs = (gigs || []).filter(gig => {
    // If we used semantic search, don't filter further by search query as results are already semantic
    const matchesSearch = isSemanticSearch && searchQuery ? true : (
      !searchQuery ||
      gig.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      gig.description?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // Fix category filtering to match actual gig categories
    const matchesCategory = !selectedCategory || selectedCategory === 'All Categories' ||
      gig.category?.toLowerCase() === selectedCategory.toLowerCase();

    return matchesSearch && matchesCategory;
  });

  const clearSearch = () => {
    setSearchQuery('');
    setIsSemanticSearch(false);
    fetchGigs();
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50">
      {/* Animated Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-r from-purple-600 via-blue-600 to-indigo-700">
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
              <Sparkles className="h-8 w-8 text-yellow-300 mr-3" />
              <span className="text-yellow-300 font-semibold text-lg">Premium Services</span>
              <Sparkles className="h-8 w-8 text-yellow-300 ml-3" />
            </motion.div>
            
            <motion.h1 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.3 }}
              className="text-5xl md:text-7xl font-bold mb-8 text-white leading-tight"
            >
              Discover Amazing
              <span className="bg-gradient-to-r from-yellow-300 via-pink-300 to-cyan-300 bg-clip-text text-transparent block">
                Freelance Services
              </span>
            </motion.h1>
            
            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.5 }}
              className="text-xl md:text-2xl mb-12 text-blue-100 max-w-3xl mx-auto leading-relaxed"
            >
              Connect with world-class freelancers and bring your ideas to life with professional services
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
                <div className="flex flex-col gap-4">
                  {/* Search Input Row */}
                  <div className="flex flex-col md:flex-row gap-4">
                    <div className="flex-1 relative">
                      <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 h-6 w-6 text-gray-400" />
                      <Input
                        type="text"
                        placeholder={isSemanticSearch ? "Try: 'mongodb database', 'react frontend', 'machine learning'..." : "What service are you looking for?"}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="pl-12 pr-4 py-4 bg-white/90 backdrop-blur-sm text-gray-800 placeholder-gray-500 text-lg rounded-2xl border-0 focus:ring-4 focus:ring-white/30 shadow-lg transition-all duration-300"
                      />
                    </div>
                    <Button
                      type="submit"
                      size="lg"
                      disabled={searchLoading}
                      className="bg-gradient-to-r from-pink-500 to-violet-500 hover:from-pink-600 hover:to-violet-600 text-white px-8 py-4 rounded-2xl font-bold text-lg shadow-lg hover:shadow-2xl transition-all duration-300 hover:scale-105 disabled:opacity-70 disabled:cursor-not-allowed"
                    >
                      {searchLoading ? (
                        <>
                          <motion.div
                            animate={{ rotate: 360 }}
                            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                            className="mr-2 h-6 w-6 border-2 border-white border-t-transparent rounded-full"
                          />
                          Searching...
                        </>
                      ) : (
                        <>
                          <Search className="mr-2 h-6 w-6" />
                          Search
                        </>
                      )}
                    </Button>
                  </div>
                  
                  {/* Semantic Search Toggle */}
                  <div className="flex flex-col items-center justify-center space-y-3">
                    <motion.button
                      type="button"
                      onClick={() => setIsSemanticSearch(!isSemanticSearch)}
                      whileHover={{ scale: 1.02 }}
                      whileTap={{ scale: 0.98 }}
                      className={`flex items-center space-x-3 px-6 py-3 rounded-2xl font-semibold transition-all duration-300 ${
                        isSemanticSearch
                          ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg'
                          : 'bg-white/80 text-gray-700 hover:bg-white hover:shadow-md'
                      }`}
                    >
                      <motion.div
                        animate={{
                          rotate: isSemanticSearch ? [0, 10, -10, 0] : 0,
                          scale: isSemanticSearch ? [1, 1.1, 1] : 1
                        }}
                        transition={{
                          duration: 0.5,
                          ease: "easeInOut"
                        }}
                      >
                        <Sparkles className={`h-5 w-5 ${isSemanticSearch ? 'text-white' : 'text-purple-500'}`} />
                      </motion.div>
                      <span>
                        {isSemanticSearch ? 'AI Semantic Search ON' : 'Enable AI Semantic Search'}
                      </span>
                      {isSemanticSearch && (
                        <motion.div
                          initial={{ scale: 0 }}
                          animate={{ scale: 1 }}
                          className="bg-white/20 px-2 py-1 rounded-lg text-xs"
                        >
                          BETA
                        </motion.div>
                      )}
                    </motion.button>
                    
                    {/* Help text for semantic search */}
                    {isSemanticSearch && (
                      <motion.div
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="text-center max-w-2xl"
                      >
                        <p className="text-white/80 text-sm">
                          🧠 <strong>AI understands context!</strong> Try searching for concepts like "database optimization" or "frontend frameworks"
                        </p>
                        <div className="flex flex-wrap justify-center gap-2 mt-2">
                          {['mongodb database', 'react frontend', 'machine learning', 'mobile app design'].map((example) => (
                            <button
                              key={example}
                              onClick={() => setSearchQuery(example)}
                              className="px-3 py-1 bg-white/20 hover:bg-white/30 text-white/90 rounded-full text-xs font-medium transition-all duration-200 hover:scale-105"
                            >
                              {example}
                            </button>
                          ))}
                        </div>
                      </motion.div>
                    )}
                  </div>
                </div>
              </div>
            </motion.form>

            {/* Stats */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.9 }}
              className="grid grid-cols-2 md:grid-cols-4 gap-8 mt-16 max-w-4xl mx-auto"
            >
              {[
                { number: '10K+', label: 'Active Services' },
                { number: '5K+', label: 'Happy Clients' },
                { number: '95%', label: 'Success Rate' },
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
              <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-purple-100/50 to-transparent rounded-full -translate-y-8 translate-x-8" />
              <div className="absolute bottom-0 left-0 w-24 h-24 bg-gradient-to-tr from-blue-100/50 to-transparent rounded-full translate-y-4 -translate-x-4" />
              
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                className="flex items-center justify-between mb-8 relative z-10"
              >
                <h3 className="font-bold text-2xl flex items-center bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent">
                  <Filter className="mr-3 h-6 w-6 text-purple-500" />
                  Categories
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
                  className={`space-y-4 relative z-10 ${showMobileFilters ? 'block' : 'hidden lg:block'}`}
                >
                  {categories.map((category, index) => {
                    const IconComponent = category.icon;
                    const isSelected = (selectedCategory === category.name) || (selectedCategory === '' && category.name === 'All Categories');
                    
                    return (
                      <motion.button
                        key={category.name}
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.4, delay: index * 0.06 }}
                        whileHover={{ 
                          scale: 1.02, 
                          x: 6,
                          transition: { duration: 0.2 }
                        }}
                        whileTap={{ scale: 0.98 }}
                        onClick={() => setSelectedCategory(category.name === 'All Categories' ? '' : category.name)}
                        className={`w-full text-left px-5 py-4 rounded-2xl transition-all duration-300 group relative overflow-hidden border ${
                          isSelected
                            ? `bg-gradient-to-r ${category.gradient} text-white shadow-xl border-transparent transform scale-[1.02]`
                            : 'text-gray-700 hover:text-white bg-white hover:bg-gradient-to-r hover:' + category.gradient + ' border-gray-200/50 hover:border-transparent shadow-sm hover:shadow-lg'
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
                        
                        <div className="flex items-center justify-between relative z-10">
                          <div className="flex items-center">
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
                            <span className="font-semibold text-base">{category.name}</span>
                          </div>
                          
                          {isSelected && (
                            <motion.div
                              initial={{ scale: 0, rotate: 180 }}
                              animate={{ scale: 1, rotate: 0 }}
                              transition={{ duration: 0.3, ease: "backOut" }}
                              className="ml-auto"
                            >
                              <Sparkles className="h-5 w-5 text-white animate-pulse" />
                            </motion.div>
                          )}
                        </div>
                        
                        {/* Gradient background overlay */}
                        <div className={`absolute inset-0 bg-gradient-to-r ${category.gradient} opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-2xl`} />
                      </motion.button>
                    );
                  })}
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
              className="bg-white/80 backdrop-blur-lg rounded-3xl shadow-xl border border-white/20 p-8 mb-8"
            >
              <div className="flex flex-col md:flex-row md:items-center justify-between">
                <div className="mb-4 md:mb-0">
                  <div className="flex items-center gap-3 mb-2">
                    <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">
                      {selectedCategory || 'All Services'}
                    </h2>
                    {searchQuery && isSemanticSearch && (
                      <motion.div
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        className="flex items-center bg-gradient-to-r from-emerald-500 to-teal-500 text-white px-3 py-1 rounded-full text-sm font-semibold"
                      >
                        <Sparkles className="h-4 w-4 mr-1" />
                        AI Search
                      </motion.div>
                    )}
                  </div>
                  <motion.p
                    key={filteredGigs.length}
                    initial={{ opacity: 0, scale: 0.8 }}
                    animate={{ opacity: 1, scale: 1 }}
                    className="text-gray-600 font-medium flex items-center"
                  >
                    <span className="inline-flex items-center px-3 py-1 bg-gradient-to-r from-blue-500 to-purple-500 text-white rounded-full text-sm font-bold mr-2">
                      {filteredGigs.length}
                    </span>
                    {searchQuery && isSemanticSearch ? (
                      <>semantic results for "{searchQuery}"</>
                    ) : searchQuery ? (
                      <>results for "{searchQuery}"</>
                    ) : (
                      <>premium service{filteredGigs.length !== 1 ? 's' : ''} available</>
                    )}
                  </motion.p>
                  {searchQuery && (
                    <motion.button
                      initial={{ opacity: 0, scale: 0.8 }}
                      animate={{ opacity: 1, scale: 1 }}
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={clearSearch}
                      className="mt-2 inline-flex items-center px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-600 hover:text-gray-800 rounded-full text-sm font-medium transition-all duration-200"
                    >
                      <X className="h-3 w-3 mr-1" />
                      Clear search
                    </motion.button>
                  )}
                </div>

                <div className="flex items-center space-x-3">
                  <div className="bg-gray-100 rounded-2xl p-1 flex">
                    <Button
                      variant={viewMode === 'grid' ? 'default' : 'ghost'}
                      size="sm"
                      onClick={() => setViewMode('grid')}
                      className={`rounded-xl transition-all duration-300 ${
                        viewMode === 'grid' 
                          ? 'bg-gradient-to-r from-blue-500 to-purple-500 text-white shadow-lg' 
                          : 'text-gray-600 hover:text-gray-800'
                      }`}
                    >
                      <Grid className="h-4 w-4" />
                    </Button>
                    <Button
                      variant={viewMode === 'list' ? 'default' : 'ghost'}
                      size="sm"
                      onClick={() => setViewMode('list')}
                      className={`rounded-xl transition-all duration-300 ${
                        viewMode === 'list' 
                          ? 'bg-gradient-to-r from-blue-500 to-purple-500 text-white shadow-lg' 
                          : 'text-gray-600 hover:text-gray-800'
                      }`}
                    >
                      <List className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </div>
            </motion.div>

            {/* Enhanced Gig List */}
            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.6 }}
            >
              <GigList
                gigs={filteredGigs}
                loading={loading}
                onGigClick={(gig) => {
                  // Navigate to gig detail page using router
                  router.push(`/gigs/${gig.id}`);
                }}
              />
            </motion.div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
