import React from 'react';
import { motion } from 'framer-motion';
import { 
  Search, 
  Star, 
  Clock, 
  Sparkles, 
  Zap, 
  Award, 
  Eye, 
  Heart, 
  MessageCircle, 
  Share2, 
  Bookmark,
  MoreHorizontal,
  MapPin,
  Calendar,
  TrendingUp,
  CheckCircle,
  Users
} from 'lucide-react';
import { Gig } from '@/types/api';

interface GigCardProps {
  gig: Gig;
  onClick?: () => void;
}

const GigCard = ({ gig, onClick }: GigCardProps) => {
  // Generate mock data for social engagement
  const likes = Math.floor(Math.random() * 200) + 50;
  const comments = Math.floor(Math.random() * 50) + 5;
  const views = Math.floor(Math.random() * 1000) + 200;
  const saved = Math.floor(Math.random() * 30) + 10;
  const orders = Math.floor(Math.random() * 100) + 20;
  
  return (
    <motion.div
      className="bg-white rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-500 border border-gray-100 overflow-hidden cursor-pointer mb-6"
      whileHover={{ 
        y: -4,
        transition: { duration: 0.3 }
      }}
      whileTap={{ scale: 0.99 }}
      onClick={onClick}
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6 }}
    >
      {/* Header - User Profile Section */}
      <div className="p-6 pb-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="relative">
              {gig.freelancerInfo?.profilePictureUrl ? (
                <img
                  src={gig.freelancerInfo.profilePictureUrl}
                  alt={gig.freelancerInfo.name}
                  className="w-12 h-12 rounded-full border-2 border-purple-200 shadow-lg object-cover"
                />
              ) : (
                <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-lg shadow-lg">
                  {(gig.freelancerInfo?.name || gig.freelancer_id?.toString() || 'U')[0].toUpperCase()}
                </div>
              )}
              <motion.div
                animate={{ scale: [1, 1.2, 1] }}
                transition={{ duration: 2, repeat: Infinity }}
                className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-white"
              />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h4 className="font-bold text-gray-900">
                  {gig.freelancerInfo?.name || `Freelancer #${gig.freelancer_id || 'Unknown'}`}
                </h4>
                <CheckCircle className="h-4 w-4 text-blue-500" />
              </div>
              <div className="flex items-center space-x-2 text-sm text-gray-500">
                <Calendar className="h-3 w-3" />
                <span>{Math.floor(Math.random() * 30) + 1} days ago</span>
                <span>•</span>
                <MapPin className="h-3 w-3" />
                <span>Remote</span>
              </div>
            </div>
          </div>
          <button className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <MoreHorizontal className="h-5 w-5 text-gray-400" />
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="px-6 pb-4">
        {/* Service Title */}
        <h3 className="text-xl font-bold text-gray-900 mb-3 leading-tight">
          {gig.title}
        </h3>
        
        {/* Description */}
        <p className="text-gray-600 text-base leading-relaxed mb-4 line-clamp-3">
          {gig.description}
        </p>

        {/* Skills Tags */}
        <div className="flex flex-wrap gap-2 mb-4">
          {gig.skills?.slice(0, 4).map((skill: string, index: number) => (
            <motion.span
              key={index}
              whileHover={{ scale: 1.05 }}
              className="bg-gradient-to-r from-purple-100 to-blue-100 text-purple-700 text-sm px-3 py-1.5 rounded-full border border-purple-200 font-medium shadow-sm"
            >
              #{skill}
            </motion.span>
          ))}
          {gig.skills && gig.skills.length > 4 && (
            <span className="text-gray-500 text-sm self-center bg-gray-100 px-3 py-1.5 rounded-full">
              +{gig.skills.length - 4} more
            </span>
          )}
        </div>

        {/* Service Stats */}
        <div className="flex items-center justify-between p-4 bg-gradient-to-r from-gray-50 to-blue-50 rounded-xl mb-4 border border-gray-100">
          <div className="flex items-center space-x-6">
            <div className="text-center">
              <div className="flex items-center space-x-1 text-emerald-600">
                <Clock className="h-4 w-4" />
                <span className="font-semibold">{gig.deliveryDays || '7'}</span>
              </div>
              <span className="text-xs text-gray-500">Days</span>
            </div>
            <div className="text-center">
              <div className="flex items-center space-x-1 text-blue-600">
                <Users className="h-4 w-4" />
                <span className="font-semibold">{orders}</span>
              </div>
              <span className="text-xs text-gray-500">Orders</span>
            </div>
            <div className="text-center">
              <div className="flex items-center space-x-1 text-yellow-600">
                <Star className="h-4 w-4 fill-current" />
                <span className="font-semibold">{gig.rating?.toFixed(1) || '4.9'}</span>
              </div>
              <span className="text-xs text-gray-500">Rating</span>
            </div>
          </div>
          
          <div className="text-right">
            <div className="text-gray-500 text-sm">Starting at</div>
            <motion.div 
              whileHover={{ scale: 1.05 }}
              className="font-bold text-green-600 text-2xl flex items-center justify-end"
            >
              ${gig.startingPrice?.toFixed(2) || '0.00'}
              <TrendingUp className="h-4 w-4 ml-1 text-green-500" />
            </motion.div>
          </div>
        </div>
      </div>

      {/* Engagement Bar */}
      <div className="px-6 py-3 border-t border-gray-100 bg-gray-50/50">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-1 text-sm text-gray-500">
            <Heart className="h-4 w-4 text-red-500" />
            <Eye className="h-4 w-4 text-blue-500" />
            <span>{likes + views} people engaged with this</span>
          </div>
          <div className="text-sm text-gray-500">
            {saved} saved
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="px-6 py-4 border-t border-gray-100">
        <div className="flex items-center justify-between">
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-gradient-to-r from-red-50 to-pink-50 text-red-600 hover:from-red-100 hover:to-pink-100 transition-all duration-200 font-medium"
          >
            <Heart className="h-4 w-4" />
            <span>{likes}</span>
          </motion.button>
          
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-gradient-to-r from-blue-50 to-indigo-50 text-blue-600 hover:from-blue-100 hover:to-indigo-100 transition-all duration-200 font-medium"
          >
            <MessageCircle className="h-4 w-4" />
            <span>{comments}</span>
          </motion.button>
          
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-gradient-to-r from-green-50 to-emerald-50 text-green-600 hover:from-green-100 hover:to-emerald-100 transition-all duration-200 font-medium"
          >
            <Share2 className="h-4 w-4" />
            <span>Share</span>
          </motion.button>
          
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-gradient-to-r from-purple-50 to-violet-50 text-purple-600 hover:from-purple-100 hover:to-violet-100 transition-all duration-200 font-medium"
          >
            <Bookmark className="h-4 w-4" />
            <span>Save</span>
          </motion.button>
        </div>
      </div>

      {/* CTA Button */}
      <div className="px-6 pb-6">
        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          className="w-full bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 text-white py-4 rounded-2xl font-bold text-lg shadow-xl hover:shadow-2xl transition-all duration-300 flex items-center justify-center space-x-2"
        >
          <Sparkles className="h-5 w-5" />
          <span>View Service Details</span>
        </motion.button>
      </div>
    </motion.div>
  );
};

interface GigListProps {
  gigs: Gig[];
  onGigClick?: (gig: Gig) => void;
  loading?: boolean;
}

const GigList = ({ gigs, onGigClick, loading }: GigListProps) => {
  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {[...Array(6)].map((_, index) => (
          <motion.div
            key={index}
            className="relative overflow-hidden rounded-3xl p-6 bg-white border border-gray-200 shadow-lg"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: index * 0.1 }}
          >
            {/* Shimmer effect */}
            <motion.div
              animate={{
                x: [-200, 200],
              }}
              transition={{
                duration: 2,
                repeat: Infinity,
                ease: "easeInOut"
              }}
              className="absolute inset-0 bg-gradient-to-r from-transparent via-purple-100/40 to-transparent skew-x-12"
            />
            
            <div className="space-y-4">
              {/* Header skeleton */}
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 bg-gray-200 rounded-full animate-pulse" />
                  <div className="space-y-2">
                    <div className="h-3 bg-gray-200 rounded w-20 animate-pulse" />
                    <div className="h-2 bg-gray-200 rounded w-16 animate-pulse" />
                  </div>
                </div>
                <div className="h-6 bg-gray-200 rounded-full w-12 animate-pulse" />
              </div>
              
              {/* Title and description skeleton */}
              <div className="space-y-2">
                <div className="h-5 bg-gray-200 rounded w-3/4 animate-pulse" />
                <div className="h-4 bg-gray-200 rounded w-full animate-pulse" />
                <div className="h-4 bg-gray-200 rounded w-2/3 animate-pulse" />
              </div>
              
              {/* Skills skeleton */}
              <div className="flex space-x-2">
                <div className="h-6 bg-gray-200 rounded-full w-16 animate-pulse" />
                <div className="h-6 bg-gray-200 rounded-full w-20 animate-pulse" />
                <div className="h-6 bg-gray-200 rounded-full w-14 animate-pulse" />
              </div>
              
              {/* Footer skeleton */}
              <div className="flex items-center justify-between pt-4 border-t border-gray-200">
                <div className="flex space-x-4">
                  <div className="h-4 bg-gray-200 rounded w-12 animate-pulse" />
                  <div className="h-4 bg-gray-200 rounded w-8 animate-pulse" />
                </div>
                <div className="h-6 bg-gray-200 rounded w-16 animate-pulse" />
              </div>
            </div>
          </motion.div>
        ))}
      </div>
    );
  }

  if (gigs.length === 0) {
    return (
      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.6 }}
        className="text-center py-16"
      >
        <div className="relative mb-8">
          <motion.div
            animate={{ 
              rotate: 360,
              scale: [1, 1.1, 1]
            }}
            transition={{ 
              rotate: { duration: 10, repeat: Infinity, ease: "linear" },
              scale: { duration: 2, repeat: Infinity, ease: "easeInOut" }
            }}
            className="w-24 h-24 mx-auto bg-gradient-to-r from-purple-400 to-blue-400 rounded-full flex items-center justify-center shadow-2xl"
          >
            <Search className="h-12 w-12 text-white" />
          </motion.div>
          
          {/* Floating sparkles */}
          <motion.div
            animate={{ y: [-10, 10, -10] }}
            transition={{ duration: 3, repeat: Infinity, ease: "easeInOut" }}
            className="absolute -top-2 -right-2"
          >
            <Sparkles className="h-6 w-6 text-yellow-400" />
          </motion.div>
          
          <motion.div
            animate={{ y: [10, -10, 10] }}
            transition={{ duration: 2.5, repeat: Infinity, ease: "easeInOut" }}
            className="absolute -bottom-2 -left-2"
          >
            <Sparkles className="h-4 w-4 text-pink-400" />
          </motion.div>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="bg-white/80 backdrop-blur-lg rounded-3xl p-8 shadow-xl border border-white/20 max-w-md mx-auto"
        >
          <h3 className="text-2xl font-bold bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent mb-4">
            No gigs found
          </h3>
          <p className="text-gray-600 mb-6 leading-relaxed">
            Try adjusting your search criteria or explore different categories to discover amazing services.
          </p>
          
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="bg-gradient-to-r from-purple-500 to-blue-500 text-white px-6 py-3 rounded-2xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300"
          >
            <Zap className="h-4 w-4 inline mr-2" />
            Explore All Gigs
          </motion.button>
        </motion.div>
      </motion.div>
    );
  }

  return (
    <motion.div
      className="space-y-6 max-w-2xl mx-auto"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.6 }}
    >
      {gigs.map((gig, index) => (
        <motion.div
          key={gig.id}
          initial={{ opacity: 0, y: 30, scale: 0.9 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ 
            duration: 0.6, 
            delay: index * 0.1,
            ease: "easeOut"
          }}
        >
          <GigCard 
            gig={gig} 
            onClick={() => onGigClick?.(gig)} 
          />
        </motion.div>
      ))}
    </motion.div>
  );
};

export default GigList;
