'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowLeft, 
  Star, 
  Clock, 
  CheckCircle, 
  User, 
  MessageSquare, 
  Heart,
  Share2,
  Crown,
  Zap,
  Gift,
  Calendar,
  RefreshCw,
  Shield,
  Award,
  Sparkles
} from 'lucide-react';
import { useAppSelector } from '@/store/hooks';
import { gigAPI } from '@/services/gig';
import { Gig } from '@/types/api';
import { Button } from '@/components/ui/button';
import toast from 'react-hot-toast';

interface GigMedia {
  url: string;
}

interface GigPackage {
  id: number;
  tier: 'BASIC' | 'STANDARD' | 'PREMIUM';
  title: string;
  description: string;
  priceCents: number;
  currency: string;
  deliveryDays: number;
  revisions: number | null;
}

interface PackageCardProps {
  package: GigPackage;
  index: number;
  onSelect: () => void;
  selected: boolean;
}

const PackageCard = ({ package: pkg, index, onSelect, selected }: PackageCardProps) => {
  const tierColors = {
    BASIC: {
      gradient: 'from-green-400 to-blue-500',
      border: 'border-green-200',
      bg: 'bg-green-50',
      text: 'text-green-700',
      icon: Gift
    },
    STANDARD: {
      gradient: 'from-purple-400 to-pink-500',
      border: 'border-purple-200',
      bg: 'bg-purple-50',
      text: 'text-purple-700',
      icon: Crown
    },
    PREMIUM: {
      gradient: 'from-yellow-400 to-orange-500',
      border: 'border-yellow-200',
      bg: 'bg-yellow-50',
      text: 'text-yellow-700',
      icon: Zap
    }
  };

  const config = tierColors[pkg.tier] || tierColors.BASIC;
  const IconComponent = config.icon;
  // console.log(gig);

  return (
    <motion.div
      initial={{ opacity: 0, y: 50, scale: 0.9 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ duration: 0.6, delay: index * 0.2 }}
      whileHover={{ scale: 1.05, y: -10 }}
      className="relative"
    >
      <div 
        className={`relative overflow-hidden rounded-3xl border-2 transition-all duration-300 cursor-pointer ${
          selected ? 'border-blue-500 shadow-2xl' : `${config.border} hover:shadow-xl`
        }`}
        onClick={onSelect}
      >
        {/* Animated background gradient */}
        <div className={`absolute inset-0 bg-gradient-to-br ${config.gradient} opacity-10`} />
        
        {/* Tier badge */}
        <div className="relative p-6 bg-white/80 backdrop-blur-sm">
          <div className={`inline-flex items-center px-4 py-2 rounded-full ${config.bg} ${config.text} text-sm font-bold mb-4`}>
            <IconComponent className="h-4 w-4 mr-2" />
            {pkg.tier}
          </div>
          
          {selected && (
            <motion.div 
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              className="absolute top-4 right-4"
            >
              <div className="bg-blue-500 text-white rounded-full p-2">
                <CheckCircle className="h-5 w-5" />
              </div>
            </motion.div>
          )}
          
          <h3 className="text-xl font-bold text-gray-900 mb-2">{pkg.title}</h3>
          <p className="text-gray-600 mb-6 leading-relaxed">{pkg.description}</p>
          
          {/* Price */}
          <div className="mb-6">
            <div className="flex items-baseline">
              <span className="text-3xl font-bold text-gray-900">
                ${(pkg.priceCents / 100).toLocaleString()}
              </span>
              <span className="ml-2 text-gray-500">{pkg.currency}</span>
            </div>
          </div>
          
          {/* Features */}
          <div className="space-y-3 mb-6">
            <div className="flex items-center text-gray-700">
              <Calendar className="h-4 w-4 mr-3 text-blue-500" />
              <span>{pkg.deliveryDays} days delivery</span>
            </div>
            
            {pkg.revisions && (
              <div className="flex items-center text-gray-700">
                <RefreshCw className="h-4 w-4 mr-3 text-green-500" />
                <span>{pkg.revisions} revisions</span>
              </div>
            )}
            
            <div className="flex items-center text-gray-700">
              <Shield className="h-4 w-4 mr-3 text-purple-500" />
              <span>Money-back guarantee</span>
            </div>
          </div>
          
          {/* Select button */}
          <Button 
            className={`w-full py-3 rounded-xl transition-all duration-300 ${
              selected 
                ? 'bg-blue-600 hover:bg-blue-700 text-white' 
                : `bg-gradient-to-r ${config.gradient} hover:shadow-lg text-white`
            }`}
            onClick={onSelect}
          >
            {selected ? 'Selected' : 'Select Package'}
          </Button>
        </div>
      </div>
    </motion.div>
  );
};

export default function GigDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const gigId = parseInt(params.id as string);
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  
  const [gig, setGig] = useState<Gig | null>(null);
  const [packages, setPackages] = useState<GigPackage[]>([]);
  const [selectedPackage, setSelectedPackage] = useState<GigPackage | null>(null);
  const [loading, setLoading] = useState(true);
  const [isLiked, setIsLiked] = useState(false);
  const [media, setMedia] = useState<GigMedia[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchGigDetails();
  }, [gigId]);

  const fetchGigDetails = async () => {
    try {
      setLoading(true);
      const [gigResponse, packagesResponse, mediaResponse] = await Promise.all([
        gigAPI.getGigById(gigId),
        gigAPI.getGigPackages(gigId),
        gigAPI.getGigMedia(gigId)
      ]);
      
      setGig(gigResponse);
      console.log(gig);
      setPackages(packagesResponse as GigPackage[]);
      setMedia(mediaResponse);
      if (packagesResponse.length > 0) {
        setSelectedPackage(packagesResponse[0] as GigPackage);
      }
    } catch (error) {
      console.error('Error fetching gig details:', error);
      setError('Failed to fetch gig details. Please try again later.');
      toast.error('Failed to fetch gig details.');
    } finally {
      setLoading(false);
    }
  };

  const handlePackageSelect = (pkg: GigPackage) => {
    setSelectedPackage(pkg);
  };

  const handleSendProposal = async () => {
    if (!selectedPackage) return;
    
    if (!isAuthenticated) {
      toast.error("Please log in to send a proposal.");
      router.push('/auth/login');
      return;
    }
    
    const freelancerInfo = gig?.freelancerInfo;
    if (!freelancerInfo) {
      toast.error("Freelancer information not available.");
      return;
    }

    try {
      // Create initial proposal message
      const proposalMessage = `Hi ${freelancerInfo.name}! 

I'm interested in your gig "${gig.title}" and would like to discuss the ${selectedPackage.title} package ($${(selectedPackage.priceCents / 100).toLocaleString()}).

Package details:
- ${selectedPackage.description}
- Delivery: ${selectedPackage.deliveryDays} days
- Revisions: ${selectedPackage.revisions || 'Unlimited'}

I'd love to discuss this project with you. Please let me know if you have any questions!`;

      // Get access token from localStorage
      const accessToken = localStorage.getItem('accessToken');
      
      // Call the direct-messages API to create the initial message
      const response = await fetch('/api/direct-messages', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(accessToken && { 'Authorization': `Bearer ${accessToken}` }),
        },
        body: JSON.stringify({
          receiverId: freelancerInfo.id,
          content: proposalMessage,
          messageType: 'TEXT'
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to send message');
      }

      // Show success message
      toast.success(`Proposal sent to ${freelancerInfo.name}!`);
      
      // Redirect to messages page
      router.push('/messages');
      
    } catch (error) {
      console.error('Error sending proposal:', error);
      toast.error('Failed to send proposal. Please try again.');
    }
  };



  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-100 flex items-center justify-center">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
          className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full"
        />
      </div>
    );
  }

  if (error || !gig) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">{error || 'Gig not found'}</h2>
          <Button onClick={() => router.push('/gigs')}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Gigs
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-100">
      {/* Header */}
      <div className="bg-white/80 backdrop-blur-sm border-b border-gray-200 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <Button 
              variant="ghost" 
              onClick={() => router.push('/gigs')}
              className="flex items-center text-gray-600 hover:text-gray-900"
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Gigs
            </Button>
            
            <div className="flex items-center space-x-4">
              <Button variant="ghost" onClick={() => setIsLiked(!isLiked)}>
                <Heart className={`h-5 w-5 ${isLiked ? 'fill-current text-red-500' : 'text-gray-500'}`} />
              </Button>
              <Button variant="ghost">
                <Share2 className="h-5 w-5 text-gray-500" />
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Gig Header */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="relative bg-cover bg-center bg-no-repeat rounded-3xl shadow-xl p-8 mb-8 border border-white/20 overflow-hidden"
          style={{ backgroundImage: media.length > 0 ? `url(${media[0].url})` : `url("https://plus.unsplash.com/premium_photo-1757922071537-d4235fd524ac?q=80&w=3132&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")` }}
        >
          <div className="absolute inset-0 bg-gradient-to-r from-white/90 via-white/80 to-transparent" />
          <div className="relative grid grid-cols-1 md:grid-cols-2">
            <div className="flex items-start justify-between">
              <div className="flex-1 flex flex-col h-full">
                <motion.h1 
                  initial={{ opacity: 0, x: -30 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.6, delay: 0.2 }}
                  className="text-4xl font-bold text-gray-900 mb-4 leading-tight"
                >
                  {gig.title}
                </motion.h1>
                
                <div className="flex items-center space-x-6 mb-6">
                  <div className="flex items-center">
                    <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-lg mr-3">
                      {gig.freelancerInfo?.name ? gig.freelancerInfo.name.charAt(0).toUpperCase() : 'F'}
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900">{gig.freelancerInfo?.name || 'Freelancer'}</p>
                      <div className="flex items-center">
                        <Star className="h-4 w-4 text-yellow-400 fill-current mr-1" />
                        <span className="text-sm text-gray-600">{gig.rating || 4.8} (127 reviews)</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <motion.p 
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ duration: 0.6, delay: 0.4 }}
                  className="text-gray-700 leading-relaxed text-lg mb-6 flex-grow"
                >
                  {gig.description}
                </motion.p>
                
                {/* Tags */}
                {gig.tags && gig.tags.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {gig.tags.map((tag, index) => (
                      <motion.span
                        key={index}
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ duration: 0.3, delay: 0.1 * index }}
                        className="px-3 py-1 bg-blue-100/50 text-blue-800 rounded-full text-sm font-medium"
                      >
                        {tag}
                      </motion.span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </motion.div>

        {/* Packages Section */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl p-6 border border-white/20 mb-8"
        >
          <div className="text-center mb-4">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Choose Your Perfect Package
            </h2>
            <p className="text-gray-600 text-sm">
              Select the package that best fits your needs
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {packages.map((pkg, index) => (
              <PackageCard
                key={pkg.id}
                package={pkg}
                index={index}
                onSelect={() => handlePackageSelect(pkg)}
                selected={selectedPackage?.id === pkg.id}
              />
            ))}
          </div>
        </motion.div>

        {/* Action Section - Only visible for CLIENT role */}
        {user?.role?.toLowerCase() === 'client' && (
          <AnimatePresence>
            {selectedPackage && (
              <motion.div
                initial={{ opacity: 0, y: 50, scale: 0.9 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={{ opacity: 0, y: 50, scale: 0.9 }}
                transition={{ duration: 0.5 }}
                className="bg-white/90 backdrop-blur-sm rounded-3xl shadow-2xl p-8 border border-white/20"
              >
                <div className="text-center mb-8">
                  <motion.div
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ duration: 0.5, delay: 0.2 }}
                    className="inline-flex items-center px-6 py-3 bg-gradient-to-r from-green-400 to-blue-500 text-white rounded-full font-bold text-lg mb-4"
                  >
                    <Award className="mr-2 h-5 w-5" />
                    {selectedPackage.tier} Package Selected
                  </motion.div>
                  <h3 className="text-2xl font-bold text-gray-900 mb-2">{selectedPackage.title}</h3>
                  <p className="text-3xl font-bold text-gray-900">
                    ${(selectedPackage.priceCents / 100).toLocaleString()}
                  </p>
                </div>

                <div className="flex justify-center">
                  <Button
                    onClick={handleSendProposal}
                    className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white px-12 py-4 rounded-xl text-lg font-bold transition-all duration-300 hover:shadow-2xl hover:scale-105"
                  >
                    Send Proposal
                  </Button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        )}

        {/* Info message for freelancers */}
        {user?.role?.toLowerCase() === 'freelancer' && (
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="bg-blue-50 rounded-3xl p-6 text-center border border-blue-200"
          >
            <div className="flex items-center justify-center mb-3">
              <User className="h-6 w-6 text-blue-600 mr-2" />
              <h3 className="text-lg font-semibold text-blue-800">Freelancer View</h3>
            </div>
            <p className="text-blue-600">
              You are viewing this gig as a freelancer. Only clients can send proposals to hire freelancers.
            </p>
          </motion.div>
        )}


      </div>
    </div>
  );
}
