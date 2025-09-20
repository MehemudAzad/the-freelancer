'use client';

import React, { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowLeft, 
  Sparkles, 
  Save, 
  Eye, 
  Crown,
  Star,
  Zap,
  Shield,
  DollarSign,
  Clock,
  RefreshCw,
  Tag,
  FileText,
  Package,
  Plus,
  X,
  CheckCircle,
  Palette,
  Wand2,
  Camera,
  Upload,
  Layers,
  Globe,
  Users,
  TrendingUp,
  Award,
  Gem,
  Rocket,
  Heart
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { gigAPI } from '@/services/gig';
import toast from 'react-hot-toast';

interface PackageFormData {
  tier: 'BASIC' | 'STANDARD' | 'PREMIUM';
  title: string;
  description: string;
  price: number;
  deliveryDays: number;
  revisions: number;
  features: string[];
}

const EditGigPage = () => {
  const params = useParams();
  const router = useRouter();
  const gigId = params.id as string;

  // Gig form state
  const [gigForm, setGigForm] = useState({
    title: 'I will create a stunning modern website with React and Next.js',
    description: 'Transform your digital presence with a cutting-edge, responsive website that captivates your audience and drives results. Using the latest technologies including React, Next.js, and modern design principles, I\'ll craft a unique digital experience tailored specifically to your brand and business goals.',
    category: 'Web Development',
    tags: ['React', 'Next.js', 'Web Design', 'Responsive', 'Modern'],
    active: true
  });

  // Package form state
  const [packageForms, setPackageForms] = useState<Record<string, PackageFormData>>({
    BASIC: {
      tier: 'BASIC',
      title: 'Starter Website',
      description: 'Perfect landing page with responsive design and basic features.',
      price: 299,
      deliveryDays: 7,
      revisions: 2,
      features: ['Responsive Design', '5 Pages', 'Contact Form', 'Basic SEO', 'Mobile Optimized']
    },
    STANDARD: {
      tier: 'STANDARD',
      title: 'Professional Website',
      description: 'Complete business website with advanced features and integrations.',
      price: 599,
      deliveryDays: 14,
      revisions: 5,
      features: ['Everything in Basic', '10 Pages', 'E-commerce Ready', 'Advanced SEO', 'Analytics Setup', 'Social Media Integration']
    },
    PREMIUM: {
      tier: 'PREMIUM',
      title: 'Enterprise Solution',
      description: 'Full-stack web application with custom features and premium support.',
      price: 1299,
      deliveryDays: 21,
      revisions: 10,
      features: ['Everything in Standard', 'Unlimited Pages', 'Custom Features', 'Database Integration', '1 Year Support', 'Performance Optimization', 'Advanced Security']
    }
  });

  const [activeTab, setActiveTab] = useState('gig');
  const [newFeature, setNewFeature] = useState('');
  const [newTag, setNewTag] = useState('');
  const [savingPackages, setSavingPackages] = useState<Record<string, boolean>>({
    'BASIC': false,
    'STANDARD': false,
    'PREMIUM': false
  });

  // Predefined tags
  const predefinedTags = [
    "React", "Next.js", "Web Design", "Responsive", "Modern", "JavaScript",
    "CSS", "HTML", "Figma", "UI/UX", "E-commerce", "SEO", "Animation",
    "Mobile", "Performance", "Security", "Database", "API", "CMS", "WordPress"
  ];

  // Categories
  const categories = [
    'Web Development',
    'Mobile Development', 
    'Graphic Design',
    'Digital Marketing',
    'Writing & Translation',
    'Video & Animation',
    'Music & Audio',
    'Programming & Tech'
  ];

  const handleGigFormChange = (field: string, value: any) => {
    setGigForm(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handlePackageFormChange = (tier: string, field: string, value: any) => {
    setPackageForms(prev => ({
      ...prev,
      [tier]: {
        ...prev[tier],
        [field]: value
      }
    }));
  };

  const addTag = (tag: string) => {
    if (!gigForm.tags.includes(tag) && gigForm.tags.length < 10) {
      setGigForm(prev => ({
        ...prev,
        tags: [...prev.tags, tag]
      }));
    }
  };

  const removeTag = (tagToRemove: string) => {
    setGigForm(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  const addFeatureToPackage = (tier: string) => {
    if (newFeature.trim() && !packageForms[tier].features.includes(newFeature.trim())) {
      handlePackageFormChange(tier, 'features', [...packageForms[tier].features, newFeature.trim()]);
      setNewFeature('');
    }
  };

  const removeFeatureFromPackage = (tier: string, featureToRemove: string) => {
    setPackageForms(prev => ({
      ...prev,
      [tier]: {
        ...prev[tier],
        features: prev[tier].features.filter(feature => feature !== featureToRemove)
      }
    }));
  };

  const savePackage = async (tier: 'BASIC' | 'STANDARD' | 'PREMIUM') => {
    try {
      setSavingPackages(prev => ({ ...prev, [tier]: true }));
      
      const packageData = packageForms[tier];
      
      // Prepare the data for the API
      const apiData = {
        tier: tier,
        title: packageData.title,
        description: packageData.description,
        priceCents: Math.round(packageData.price * 100), // Convert to cents
        currency: 'USD',
        deliveryDays: packageData.deliveryDays,
        revisions: packageData.revisions
      };

      // Call the API to create/update the package
      await gigAPI.createGigPackage(parseInt(gigId), apiData);
      
      toast.success(`${tier.charAt(0) + tier.slice(1).toLowerCase()} package saved successfully!`);
      
    } catch (error: any) {
      console.error(`Error saving ${tier} package:`, error);
      toast.error(error.message || `Failed to save ${tier.toLowerCase()} package`);
    } finally {
      setSavingPackages(prev => ({ ...prev, [tier]: false }));
    }
  };

  const saveAllPackages = async () => {
    try {
      // Save all packages sequentially
      await Promise.all([
        savePackage('BASIC'),
        savePackage('STANDARD'),
        savePackage('PREMIUM')
      ]);
      
      toast.success('All packages launched successfully! 🚀');
      
    } catch (error: any) {
      console.error('Error saving all packages:', error);
      toast.error('Some packages failed to save. Please try again.');
    }
  };  const getTierInfo = (tier: string) => {
    const tiers = {
      BASIC: {
        name: 'Basic',
        icon: Shield,
        gradient: 'from-emerald-400 via-green-500 to-teal-600',
        bgGradient: 'from-emerald-50 via-green-50 to-teal-50',
        borderColor: 'border-green-300',
        textColor: 'text-green-700',
        glowColor: 'shadow-green-500/25'
      },
      STANDARD: {
        name: 'Standard', 
        icon: Star,
        gradient: 'from-blue-400 via-indigo-500 to-purple-600',
        bgGradient: 'from-blue-50 via-indigo-50 to-purple-50',
        borderColor: 'border-blue-300',
        textColor: 'text-blue-700',
        glowColor: 'shadow-blue-500/25'
      },
      PREMIUM: {
        name: 'Premium',
        icon: Crown,
        gradient: 'from-purple-400 via-pink-500 to-orange-600', 
        bgGradient: 'from-purple-50 via-pink-50 to-orange-50',
        borderColor: 'border-purple-300',
        textColor: 'text-purple-700',
        glowColor: 'shadow-purple-500/25'
      }
    };
    return tiers[tier as keyof typeof tiers];
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      {/* Animated Background */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -inset-10 opacity-50">
          <div className="absolute top-0 -left-4 w-72 h-72 bg-purple-500 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse"></div>
          <div className="absolute top-0 -right-4 w-72 h-72 bg-yellow-500 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse animation-delay-200"></div>
          <div className="absolute -bottom-8 left-20 w-72 h-72 bg-pink-500 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse animation-delay-400"></div>
        </div>
      </div>

      <div className="relative z-10 container mx-auto px-4 py-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
          className="flex items-center justify-between mb-12"
        >
          <div className="flex items-center space-x-6">
            <motion.button 
              onClick={() => router.back()}
              whileHover={{ scale: 1.05, x: -5 }}
              whileTap={{ scale: 0.95 }}
              className="flex items-center space-x-3 bg-white/10 backdrop-blur-lg border border-white/20 text-white px-6 py-3 rounded-2xl hover:bg-white/20 transition-all duration-300"
            >
              <ArrowLeft className="h-5 w-5" />
              <span className="font-medium">Back</span>
            </motion.button>
            
            <div>
              <motion.h1 
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2, duration: 0.6 }}
                className="text-4xl font-bold bg-gradient-to-r from-white via-purple-200 to-pink-200 bg-clip-text text-transparent flex items-center space-x-4"
              >
                <Sparkles className="h-10 w-10 text-yellow-400" />
                <span>Edit Gig Magic</span>
              </motion.h1>
              <motion.p 
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.4, duration: 0.6 }}
                className="text-gray-300 mt-2 text-lg"
              >
                Transform your gig into something extraordinary
              </motion.p>
            </div>
          </div>
          
          <motion.button
            onClick={() => window.open(`/gigs/${gigId}`, '_blank')}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="flex items-center space-x-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white px-6 py-3 rounded-2xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300"
          >
            <Eye className="h-5 w-5" />
            <span>Preview</span>
          </motion.button>
        </motion.div>

        {/* Tabs */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3, duration: 0.6 }}
          className="flex space-x-2 bg-white/10 backdrop-blur-lg rounded-2xl p-2 mb-12 border border-white/20"
        >
          <motion.button
            onClick={() => setActiveTab('gig')}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            className={`flex items-center space-x-3 px-8 py-4 rounded-xl font-bold text-lg transition-all duration-300 ${
              activeTab === 'gig'
                ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white shadow-lg'
                : 'text-gray-300 hover:text-white hover:bg-white/10'
            }`}
          >
            <Wand2 className="h-6 w-6" />
            <span>Gig Details</span>
          </motion.button>
          
          <motion.button
            onClick={() => setActiveTab('packages')}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            className={`flex items-center space-x-3 px-8 py-4 rounded-xl font-bold text-lg transition-all duration-300 ${
              activeTab === 'packages'
                ? 'bg-gradient-to-r from-purple-500 to-pink-600 text-white shadow-lg'
                : 'text-gray-300 hover:text-white hover:bg-white/10'
            }`}
          >
            <Package className="h-6 w-6" />
            <span>Packages</span>
          </motion.button>
        </motion.div>

        {/* Content */}
        <AnimatePresence mode="wait">
          {activeTab === 'gig' && (
            <motion.div
              key="gig"
              initial={{ opacity: 0, x: -50, scale: 0.95 }}
              animate={{ opacity: 1, x: 0, scale: 1 }}
              exit={{ opacity: 0, x: 50, scale: 0.95 }}
              transition={{ duration: 0.5, ease: "easeInOut" }}
              className="bg-white/10 backdrop-blur-2xl rounded-3xl shadow-2xl p-8 border border-white/20"
            >
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2, duration: 0.6 }}
                className="text-center mb-8"
              >
                <h2 className="text-3xl font-bold bg-gradient-to-r from-white to-purple-200 bg-clip-text text-transparent mb-3 flex items-center justify-center space-x-3">
                  <Palette className="h-8 w-8 text-purple-400" />
                  <span>Craft Your Story</span>
                </h2>
                <p className="text-gray-300 text-lg">Make your gig irresistible to potential clients</p>
              </motion.div>

              <div className="space-y-8">
                {/* Title */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.3, duration: 0.6 }}
                  className="space-y-3"
                >
                  <label className="text-xl font-bold text-white flex items-center space-x-2 mb-3">
                    <Sparkles className="h-5 w-5 text-yellow-400" />
                    <span>Gig Title</span>
                  </label>
                  <div className="relative">
                    <Input
                      value={gigForm.title}
                      onChange={(e) => handleGigFormChange('title', e.target.value)}
                      className="text-lg py-4 px-6 bg-white/5 border-2 border-white/20 rounded-2xl text-white placeholder-gray-400 focus:border-purple-400 focus:bg-white/10 transition-all duration-300"
                      placeholder="I will create something amazing for you..."
                      maxLength={80}
                    />
                    <div className="absolute -bottom-6 right-2 text-sm text-gray-400">
                      {gigForm.title.length}/80
                    </div>
                  </div>
                </motion.div>

                {/* Description */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.4, duration: 0.6 }}
                  className="space-y-3"
                >
                  <label className="text-xl font-bold text-white flex items-center space-x-2 mb-3">
                    <FileText className="h-5 w-5 text-blue-400" />
                    <span>Description</span>
                  </label>
                  <div className="relative">
                    <Textarea
                      value={gigForm.description}
                      onChange={(e) => handleGigFormChange('description', e.target.value)}
                      rows={6}
                      className="text-lg py-4 px-6 bg-white/5 border-2 border-white/20 rounded-2xl text-white placeholder-gray-400 focus:border-blue-400 focus:bg-white/10 transition-all duration-300 resize-none"
                      placeholder="Tell clients exactly what you'll deliver and why they should choose you..."
                      maxLength={1200}
                    />
                    <div className="absolute -bottom-6 right-2 text-sm text-gray-400">
                      {gigForm.description.length}/1200
                    </div>
                  </div>
                </motion.div>

                {/* Category */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.5, duration: 0.6 }}
                  className="space-y-3"
                >
                  <label className="text-xl font-bold text-white flex items-center space-x-2 mb-3">
                    <Layers className="h-5 w-5 text-green-400" />
                    <span>Category</span>
                  </label>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    {categories.map((category) => (
                      <motion.button
                        key={category}
                        onClick={() => handleGigFormChange('category', category)}
                        whileHover={{ scale: 1.05, y: -2 }}
                        whileTap={{ scale: 0.95 }}
                        className={`p-4 rounded-2xl border-2 transition-all duration-300 ${
                          gigForm.category === category
                            ? 'bg-gradient-to-r from-green-500 to-blue-600 border-transparent text-white shadow-lg'
                            : 'bg-white/5 border-white/20 text-gray-300 hover:border-green-400 hover:bg-white/10'
                        }`}
                      >
                        <div className="font-semibold text-center">{category}</div>
                      </motion.button>
                    ))}
                  </div>
                </motion.div>

                {/* Tags */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.6, duration: 0.6 }}
                  className="space-y-4"
                >
                  <label className="text-xl font-bold text-white flex items-center space-x-2 mb-4">
                    <Tag className="h-5 w-5 text-pink-400" />
                    <span>Tags</span>
                  </label>
                  
                  {/* Selected Tags */}
                  <div className="flex flex-wrap gap-3 mb-4">
                    {gigForm.tags.map((tag, index) => (
                      <motion.span
                        key={index}
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.8 }}
                        whileHover={{ scale: 1.1 }}
                        className="bg-gradient-to-r from-pink-500 to-purple-600 text-white px-4 py-2 rounded-full text-sm font-semibold flex items-center space-x-2 shadow-lg"
                      >
                        <span>#{tag}</span>
                        <button
                          onClick={() => removeTag(tag)}
                          className="text-pink-200 hover:text-white transition-colors"
                        >
                          <X className="h-4 w-4" />
                        </button>
                      </motion.span>
                    ))}
                  </div>

                  {/* Add Custom Tag */}
                  <div className="flex space-x-3 mb-4">
                    <Input
                      value={newTag}
                      onChange={(e) => setNewTag(e.target.value)}
                      placeholder="Add custom tag..."
                      className="flex-1 py-3 px-4 bg-white/5 border-2 border-white/20 rounded-2xl text-white placeholder-gray-400 focus:border-pink-400"
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          addTag(newTag);
                          setNewTag('');
                        }
                      }}
                    />
                    <motion.button
                      onClick={() => { addTag(newTag); setNewTag(''); }}
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      className="bg-gradient-to-r from-pink-500 to-purple-600 text-white px-6 py-3 rounded-2xl font-semibold hover:shadow-lg transition-all duration-300"
                    >
                      <Plus className="h-5 w-5" />
                    </motion.button>
                  </div>

                  {/* Predefined Tags */}
                  <div className="flex flex-wrap gap-2">
                    {predefinedTags
                      .filter(tag => !gigForm.tags.includes(tag))
                      .slice(0, 15)
                      .map((tag) => (
                        <motion.button
                          key={tag}
                          onClick={() => addTag(tag)}
                          whileHover={{ scale: 1.05, y: -2 }}
                          whileTap={{ scale: 0.95 }}
                          className="bg-white/5 text-gray-300 px-3 py-2 rounded-xl text-sm border border-white/20 hover:border-pink-400 hover:bg-white/10 transition-all duration-300"
                          disabled={gigForm.tags.length >= 10}
                        >
                          #{tag}
                        </motion.button>
                      ))}
                  </div>
                  <p className="text-sm text-gray-400">
                    {gigForm.tags.length}/10 tags selected
                  </p>
                </motion.div>

                {/* Active Status */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.7, duration: 0.6 }}
                  className="flex items-center justify-between p-6 bg-white/5 rounded-2xl border border-white/20"
                >
                  <div className="flex items-center space-x-3">
                    <Globe className="h-6 w-6 text-blue-400" />
                    <div>
                      <h3 className="text-lg font-bold text-white">Gig Visibility</h3>
                      <p className="text-gray-300">Make your gig visible to potential clients</p>
                    </div>
                  </div>
                  <motion.button
                    onClick={() => handleGigFormChange('active', !gigForm.active)}
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    className={`relative w-16 h-8 rounded-full transition-all duration-300 ${
                      gigForm.active ? 'bg-gradient-to-r from-green-400 to-blue-500' : 'bg-gray-600'
                    }`}
                  >
                    <motion.div
                      initial={false}
                      animate={{ x: gigForm.active ? 32 : 4 }}
                      transition={{ type: "spring", stiffness: 500, damping: 30 }}
                      className="absolute top-1 w-6 h-6 bg-white rounded-full shadow-lg"
                    />
                  </motion.button>
                </motion.div>

                {/* Save Button */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.8, duration: 0.6 }}
                  className="flex justify-center pt-8"
                >
                  <motion.button
                    whileHover={{ scale: 1.05, y: -2 }}
                    whileTap={{ scale: 0.95 }}
                    className="bg-gradient-to-r from-purple-500 via-pink-500 to-orange-500 text-white px-12 py-4 rounded-2xl font-bold text-xl shadow-2xl hover:shadow-purple-500/25 transition-all duration-300 flex items-center space-x-3"
                  >
                    <Save className="h-6 w-6" />
                    <span>Save Gig Details</span>
                    <Sparkles className="h-6 w-6" />
                  </motion.button>
                </motion.div>
              </div>
            </motion.div>
          )}

          {activeTab === 'packages' && (
            <motion.div
              key="packages"
              initial={{ opacity: 0, x: 50, scale: 0.95 }}
              animate={{ opacity: 1, x: 0, scale: 1 }}
              exit={{ opacity: 0, x: -50, scale: 0.95 }}
              transition={{ duration: 0.5, ease: "easeInOut" }}
              className="space-y-8"
            >
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2, duration: 0.6 }}
                className="text-center mb-12"
              >
                <h2 className="text-4xl font-bold bg-gradient-to-r from-white via-purple-200 to-pink-200 bg-clip-text text-transparent mb-4 flex items-center justify-center space-x-4">
                  <Gem className="h-10 w-10 text-purple-400" />
                  <span>Premium Packages</span>
                  <Rocket className="h-10 w-10 text-orange-400" />
                </h2>
                <p className="text-gray-300 text-xl">Create irresistible offers for every budget</p>
              </motion.div>

              {Object.entries(packageForms).map(([tier, packageData], index) => {
                const tierInfo = getTierInfo(tier);
                const TierIcon = tierInfo.icon;

                return (
                  <motion.div
                    key={tier}
                    initial={{ opacity: 0, y: 50, rotateX: -15 }}
                    animate={{ opacity: 1, y: 0, rotateX: 0 }}
                    transition={{ delay: index * 0.2, duration: 0.8, ease: "easeOut" }}
                    whileHover={{ scale: 1.02, y: -5 }}
                    className={`bg-gradient-to-br ${tierInfo.bgGradient} backdrop-blur-2xl rounded-3xl shadow-2xl p-8 border-2 ${tierInfo.borderColor} ${tierInfo.glowColor} hover:shadow-2xl transition-all duration-500`}
                  >
                    <div className="flex items-center justify-between mb-8">
                      <div className="flex items-center space-x-4">
                        <motion.div
                          initial={{ scale: 0, rotate: -180 }}
                          animate={{ scale: 1, rotate: 0 }}
                          transition={{ delay: index * 0.2 + 0.3, type: "spring", stiffness: 200 }}
                          className={`w-16 h-16 bg-gradient-to-r ${tierInfo.gradient} rounded-2xl flex items-center justify-center shadow-lg`}
                        >
                          <TierIcon className="h-8 w-8 text-white" />
                        </motion.div>
                        <div>
                          <h3 className="text-3xl font-bold text-gray-800 flex items-center space-x-2">
                            <span>{tierInfo.name} Package</span>
                            {tier === 'PREMIUM' && <Crown className="h-6 w-6 text-yellow-500" />}
                          </h3>
                          <p className="text-gray-600 text-lg">Perfect for {tier.toLowerCase()} level projects</p>
                        </div>
                      </div>
                      <motion.button
                        onClick={() => savePackage(tier as 'BASIC' | 'STANDARD' | 'PREMIUM')}
                        disabled={savingPackages[tier]}
                        whileHover={{ scale: savingPackages[tier] ? 1 : 1.05, rotate: savingPackages[tier] ? 0 : 5 }}
                        whileTap={{ scale: savingPackages[tier] ? 1 : 0.95 }}
                        className={`bg-gradient-to-r ${tierInfo.gradient} text-white px-8 py-3 rounded-2xl font-bold text-lg shadow-lg hover:shadow-xl transition-all duration-300 flex items-center space-x-2 ${
                          savingPackages[tier] ? 'opacity-70 cursor-not-allowed' : ''
                        }`}
                      >
                        {savingPackages[tier] ? (
                          <>
                            <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                            <span>Saving...</span>
                          </>
                        ) : (
                          <>
                            <Save className="h-5 w-5" />
                            <span>Save</span>
                          </>
                        )}
                      </motion.button>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                      {/* Left Column */}
                      <div className="space-y-6">
                        <div>
                          <label className="text-lg font-bold text-gray-700 mb-3 flex items-center space-x-2">
                            <Sparkles className="h-5 w-5 text-purple-500" />
                            <span>Package Title</span>
                          </label>
                          <Input
                            value={packageData.title}
                            onChange={(e) => handlePackageFormChange(tier, 'title', e.target.value)}
                            className="text-lg py-4 px-6 rounded-2xl border-2 focus:border-purple-400 transition-all duration-300"
                            placeholder={`${tierInfo.name} Package Title`}
                          />
                        </div>

                        <div>
                          <label className="text-lg font-bold text-gray-700 mb-3 flex items-center space-x-2">
                            <FileText className="h-5 w-5 text-blue-500" />
                            <span>Description</span>
                          </label>
                          <Textarea
                            value={packageData.description}
                            onChange={(e) => handlePackageFormChange(tier, 'description', e.target.value)}
                            rows={4}
                            className="text-lg py-4 px-6 rounded-2xl border-2 focus:border-blue-400 transition-all duration-300 resize-none"
                            placeholder="Describe what's included in this amazing package..."
                          />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <label className="text-lg font-bold text-gray-700 mb-3 flex items-center space-x-2">
                              <DollarSign className="h-5 w-5 text-green-500" />
                              <span>Price</span>
                            </label>
                            <div className="relative">
                              <Input
                                type="number"
                                value={packageData.price}
                                onChange={(e) => handlePackageFormChange(tier, 'price', parseFloat(e.target.value || '0'))}
                                className="text-2xl font-bold py-4 px-6 rounded-2xl border-2 focus:border-green-400 transition-all duration-300 pl-12"
                                placeholder="299"
                                min={5}
                              />
                              <DollarSign className="absolute left-4 top-1/2 transform -translate-y-1/2 h-6 w-6 text-green-500" />
                            </div>
                          </div>

                          <div>
                            <label className="text-lg font-bold text-gray-700 mb-3 flex items-center space-x-2">
                              <Clock className="h-5 w-5 text-orange-500" />
                              <span>Delivery</span>
                            </label>
                            <div className="relative">
                              <Input
                                type="number"
                                value={packageData.deliveryDays}
                                onChange={(e) => handlePackageFormChange(tier, 'deliveryDays', parseInt(e.target.value || '0'))}
                                className="text-xl font-bold py-4 px-6 rounded-2xl border-2 focus:border-orange-400 transition-all duration-300 pr-16"
                                placeholder="7"
                                min={1}
                              />
                              <span className="absolute right-4 top-1/2 transform -translate-y-1/2 text-orange-500 font-medium">days</span>
                            </div>
                          </div>
                        </div>

                        <div>
                          <label className="text-lg font-bold text-gray-700 mb-3 flex items-center space-x-2">
                            <RefreshCw className="h-5 w-5 text-blue-500" />
                            <span>Revisions</span>
                          </label>
                          <Input
                            type="number"
                            value={packageData.revisions}
                            onChange={(e) => handlePackageFormChange(tier, 'revisions', parseInt(e.target.value || '0'))}
                            className="text-xl font-bold py-4 px-6 rounded-2xl border-2 focus:border-blue-400 transition-all duration-300"
                            placeholder="2"
                            min={0}
                          />
                        </div>
                      </div>

                      {/* Right Column */}
                      <div className="space-y-6">
                        <div>
                          <label className="text-lg font-bold text-gray-700 mb-3 flex items-center space-x-2">
                            <CheckCircle className="h-5 w-5 text-green-500" />
                            <span>Features Included</span>
                          </label>
                          
                          <div className="space-y-3 mb-4">
                            {packageData.features.map((feature, featureIndex) => (
                              <motion.div
                                key={featureIndex}
                                initial={{ opacity: 0, x: -20 }}
                                animate={{ opacity: 1, x: 0 }}
                                exit={{ opacity: 0, x: 20 }}
                                transition={{ delay: featureIndex * 0.1 }}
                                whileHover={{ scale: 1.02 }}
                                className="flex items-center space-x-3 bg-white/50 rounded-xl p-4 border border-gray-200"
                              >
                                <CheckCircle className="h-5 w-5 text-green-500 flex-shrink-0" />
                                <span className="flex-1 font-medium text-gray-700">{feature}</span>
                                <motion.button
                                  onClick={() => removeFeatureFromPackage(tier, feature)}
                                  whileHover={{ scale: 1.1, rotate: 5 }}
                                  whileTap={{ scale: 0.9 }}
                                  className="text-red-500 hover:text-red-700 transition-colors"
                                >
                                  <X className="h-4 w-4" />
                                </motion.button>
                              </motion.div>
                            ))}
                          </div>

                          <div className="flex space-x-2">
                            <Input
                              value={newFeature}
                              onChange={(e) => setNewFeature(e.target.value)}
                              placeholder="Add new feature..."
                              className="flex-1 py-3 px-4 rounded-xl border-2 focus:border-green-400 transition-all duration-300"
                              onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                  addFeatureToPackage(tier);
                                }
                              }}
                            />
                            <motion.button
                              onClick={() => addFeatureToPackage(tier)}
                              whileHover={{ scale: 1.05, rotate: 5 }}
                              whileTap={{ scale: 0.95 }}
                              className="bg-gradient-to-r from-green-500 to-blue-600 text-white px-6 py-3 rounded-xl font-semibold hover:shadow-lg transition-all duration-300"
                            >
                              <Plus className="h-5 w-5" />
                            </motion.button>
                          </div>
                        </div>

                        {/* Package Preview Card */}
                        <motion.div
                          whileHover={{ scale: 1.02, rotate: 1 }}
                          className={`bg-gradient-to-br ${tierInfo.bgGradient} rounded-2xl p-6 border-2 ${tierInfo.borderColor} shadow-lg`}
                        >
                          <div className="flex items-center justify-between mb-4">
                            <h4 className="text-xl font-bold text-gray-800 flex items-center space-x-2">
                              <Zap className="h-5 w-5 text-yellow-500" />
                              <span>Live Preview</span>
                            </h4>
                            <div className="flex items-center space-x-1">
                              {[...Array(5)].map((_, i) => (
                                <Star key={i} className="h-4 w-4 text-yellow-400 fill-current" />
                              ))}
                            </div>
                          </div>
                          
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-gray-600 font-medium">💰 Price:</span>
                              <span className="text-2xl font-bold text-green-600">${packageData.price}</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-gray-600 font-medium">⏱️ Delivery:</span>
                              <span className="font-bold text-orange-600">{packageData.deliveryDays} days</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-gray-600 font-medium">🔄 Revisions:</span>
                              <span className="font-bold text-blue-600">{packageData.revisions}</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-gray-600 font-medium">✨ Features:</span>
                              <span className="font-bold text-purple-600">{packageData.features.length} items</span>
                            </div>
                          </div>

                          <motion.div
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                            className="mt-4 bg-gradient-to-r from-purple-500 to-pink-600 text-white text-center py-3 rounded-xl font-bold cursor-pointer shadow-lg hover:shadow-xl transition-all duration-300"
                          >
                            <Heart className="inline h-4 w-4 mr-2" />
                            Order Now
                          </motion.div>
                        </motion.div>
                      </div>
                    </div>
                  </motion.div>
                );
              })}

              {/* Final Save All Button */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.8, duration: 0.6 }}
                className="text-center pt-12"
              >
                <motion.button
                  onClick={saveAllPackages}
                  disabled={Object.values(savingPackages).some(saving => saving)}
                  whileHover={{ 
                    scale: Object.values(savingPackages).some(saving => saving) ? 1 : 1.05, 
                    y: Object.values(savingPackages).some(saving => saving) ? 0 : -5 
                  }}
                  whileTap={{ scale: Object.values(savingPackages).some(saving => saving) ? 1 : 0.95 }}
                  className={`bg-gradient-to-r from-purple-600 via-pink-600 to-orange-600 text-white px-16 py-6 rounded-3xl font-bold text-2xl shadow-2xl hover:shadow-purple-500/25 transition-all duration-500 flex items-center space-x-4 ${
                    Object.values(savingPackages).some(saving => saving) ? 'opacity-70 cursor-not-allowed' : ''
                  }`}
                >
                  {Object.values(savingPackages).some(saving => saving) ? (
                    <>
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-white"></div>
                      <span>Launching...</span>
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-white"></div>
                    </>
                  ) : (
                    <>
                      <Rocket className="h-8 w-8" />
                      <span>Launch All Packages</span>
                      <Sparkles className="h-8 w-8" />
                    </>
                  )}
                </motion.button>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};

export default EditGigPage;
