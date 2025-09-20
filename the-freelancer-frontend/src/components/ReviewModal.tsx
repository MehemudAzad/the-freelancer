'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  X, 
  Star, 
  Send, 
  Loader2, 
  CheckCircle,
  AlertCircle,
  MessageSquare,
  Award,
  Clock,
  Users,
  Briefcase
} from 'lucide-react';
import reviewService, { ReviewData } from '@/services/review';
import { toast } from 'react-hot-toast';

interface ReviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  project: {
    id: string;
    jobTitle: string;
    freelancerId: number;
    jobId: string;
    freelancerName?: string;
  };
  onReviewSubmitted: () => void;
}

interface RatingState {
  overallRating: number;
  qualityRating: number;
  communicationRating: number;
  timelinessRating: number;
  professionalismRating: number;
}

const ReviewModal: React.FC<ReviewModalProps> = ({ 
  isOpen, 
  onClose, 
  project, 
  onReviewSubmitted 
}) => {
  const [ratings, setRatings] = useState<RatingState>({
    overallRating: 0,
    qualityRating: 0,
    communicationRating: 0,
    timelinessRating: 0,
    professionalismRating: 0,
  });

  const [reviewData, setReviewData] = useState({
    title: '',
    comment: '',
    wouldRecommend: true,
    isAnonymous: false,
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<{[key: string]: string}>({});

  const ratingCategories = [
    {
      key: 'overallRating' as keyof RatingState,
      label: 'Overall Rating',
      description: 'How would you rate this freelancer overall?',
      icon: Award,
      color: 'text-yellow-500',
    },
    {
      key: 'qualityRating' as keyof RatingState,
      label: 'Quality of Work',
      description: 'How satisfied are you with the quality of work delivered?',
      icon: Briefcase,
      color: 'text-blue-500',
    },
    {
      key: 'communicationRating' as keyof RatingState,
      label: 'Communication',
      description: 'How well did the freelancer communicate throughout the project?',
      icon: MessageSquare,
      color: 'text-green-500',
    },
    {
      key: 'timelinessRating' as keyof RatingState,
      label: 'Timeliness',
      description: 'Did the freelancer deliver work on time?',
      icon: Clock,
      color: 'text-purple-500',
    },
    {
      key: 'professionalismRating' as keyof RatingState,
      label: 'Professionalism',
      description: 'How professional was the freelancer\'s approach?',
      icon: Users,
      color: 'text-indigo-500',
    },
  ];

  const renderStars = (
    rating: number, 
    onRatingChange: (rating: number) => void,
    disabled: boolean = false
  ) => {
    return (
      <div className="flex space-x-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            disabled={disabled}
            onClick={() => !disabled && onRatingChange(star)}
            className={`p-1 transition-all duration-200 ${
              disabled ? 'cursor-not-allowed' : 'hover:scale-110 cursor-pointer'
            }`}
          >
            <Star
              className={`w-6 h-6 transition-colors duration-200 ${
                star <= rating
                  ? 'fill-yellow-400 text-yellow-400'
                  : 'text-gray-300 hover:text-yellow-300'
              }`}
            />
          </button>
        ))}
      </div>
    );
  };

  const validateForm = (): boolean => {
    const newErrors: {[key: string]: string} = {};

    // Check if all ratings are provided
    Object.entries(ratings).forEach(([key, value]) => {
      if (value === 0) {
        const category = ratingCategories.find(cat => cat.key === key);
        newErrors[key] = `Please provide a ${category?.label.toLowerCase()} rating`;
      }
    });

    if (!reviewData.title.trim()) {
      newErrors.title = 'Please provide a review title';
    } else if (reviewData.title.length < 5) {
      newErrors.title = 'Title must be at least 5 characters long';
    }

    if (!reviewData.comment.trim()) {
      newErrors.comment = 'Please provide a review comment';
    } else if (reviewData.comment.length < 20) {
      newErrors.comment = 'Comment must be at least 20 characters long';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      toast.error('Please fix the errors in the form');
      return;
    }

    setIsSubmitting(true);
    
    try {
      const submissionData: ReviewData = {
        freelancerId: project.freelancerId,
        jobId: project.jobId,
        contractId: project.id,
        ...ratings,
        title: reviewData.title.trim(),
        comment: reviewData.comment.trim(),
        reviewType: 'JOB_REVIEW',
        isAnonymous: reviewData.isAnonymous,
        wouldRecommend: reviewData.wouldRecommend,
      };

      console.log('Submitting review data:', submissionData);
      await reviewService.submitReview(submissionData);
      
      toast.success('Review submitted successfully!');
      onReviewSubmitted();
      onClose();
      
    } catch (error: any) {
      console.error('Error submitting review:', error);
      toast.error(error.response?.data?.message || 'Failed to submit review. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setRatings({
      overallRating: 0,
      qualityRating: 0,
      communicationRating: 0,
      timelinessRating: 0,
      professionalismRating: 0,
    });
    setReviewData({
      title: '',
      comment: '',
      wouldRecommend: true,
      isAnonymous: false,
    });
    setErrors({});
  };

  const handleClose = () => {
    if (!isSubmitting) {
      resetForm();
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        onClick={handleClose}
      >
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.9, opacity: 0 }}
          onClick={(e) => e.stopPropagation()}
          className="bg-white rounded-2xl w-full max-w-4xl max-h-[90vh] overflow-y-auto shadow-2xl"
        >
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-purple-600 p-6 text-white relative">
            <button
              onClick={handleClose}
              disabled={isSubmitting}
              className="absolute top-4 right-4 p-2 hover:bg-white/20 rounded-full transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <X className="w-5 h-5" />
            </button>
            <div>
              <h2 className="text-2xl font-bold mb-2">Write a Review</h2>
              <p className="text-blue-100">
                Share your experience working with {project.freelancerName || 'this freelancer'} on "{project.jobTitle}"
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-8">
            {/* Rating Categories */}
            <div className="space-y-6">
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Rate Your Experience</h3>
              
              {ratingCategories.map((category) => (
                <div key={category.key} className="space-y-3">
                  <div className="flex items-center space-x-3">
                    <category.icon className={`w-5 h-5 ${category.color}`} />
                    <div className="flex-1">
                      <label className="text-sm font-medium text-gray-900">
                        {category.label}
                      </label>
                      <p className="text-xs text-gray-600">{category.description}</p>
                    </div>
                    {renderStars(
                      ratings[category.key],
                      (rating) => setRatings(prev => ({ ...prev, [category.key]: rating })),
                      isSubmitting
                    )}
                  </div>
                  {errors[category.key] && (
                    <p className="text-red-500 text-xs flex items-center space-x-1">
                      <AlertCircle className="w-3 h-3" />
                      <span>{errors[category.key]}</span>
                    </p>
                  )}
                </div>
              ))}
            </div>

            {/* Review Title */}
            <div className="space-y-3">
              <label className="text-sm font-medium text-gray-900">
                Review Title *
              </label>
              <input
                type="text"
                value={reviewData.title}
                onChange={(e) => setReviewData(prev => ({ ...prev, title: e.target.value }))}
                disabled={isSubmitting}
                placeholder="e.g., Excellent Freelancer!"
                className={`w-full px-4 py-3 border-2 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 disabled:bg-gray-100 disabled:cursor-not-allowed ${
                  errors.title ? 'border-red-300' : 'border-gray-200'
                }`}
              />
              {errors.title && (
                <p className="text-red-500 text-xs flex items-center space-x-1">
                  <AlertCircle className="w-3 h-3" />
                  <span>{errors.title}</span>
                </p>
              )}
            </div>

            {/* Review Comment */}
            <div className="space-y-3">
              <label className="text-sm font-medium text-gray-900">
                Review Comment *
              </label>
              <textarea
                value={reviewData.comment}
                onChange={(e) => setReviewData(prev => ({ ...prev, comment: e.target.value }))}
                disabled={isSubmitting}
                rows={5}
                placeholder="Share your detailed experience working with this freelancer..."
                className={`w-full px-4 py-3 border-2 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 resize-none disabled:bg-gray-100 disabled:cursor-not-allowed ${
                  errors.comment ? 'border-red-300' : 'border-gray-200'
                }`}
              />
              <div className="flex justify-between text-xs text-gray-500">
                <span>{reviewData.comment.length} characters</span>
                <span>Minimum 20 characters</span>
              </div>
              {errors.comment && (
                <p className="text-red-500 text-xs flex items-center space-x-1">
                  <AlertCircle className="w-3 h-3" />
                  <span>{errors.comment}</span>
                </p>
              )}
            </div>

            {/* Additional Options */}
            <div className="space-y-4 p-4 bg-gray-50 rounded-xl">
              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  id="wouldRecommend"
                  checked={reviewData.wouldRecommend}
                  onChange={(e) => setReviewData(prev => ({ ...prev, wouldRecommend: e.target.checked }))}
                  disabled={isSubmitting}
                  className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 disabled:cursor-not-allowed"
                />
                <label htmlFor="wouldRecommend" className="text-sm font-medium text-gray-900">
                  I would recommend this freelancer to others
                </label>
              </div>
              
              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  id="isAnonymous"
                  checked={reviewData.isAnonymous}
                  onChange={(e) => setReviewData(prev => ({ ...prev, isAnonymous: e.target.checked }))}
                  disabled={isSubmitting}
                  className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 disabled:cursor-not-allowed"
                />
                <label htmlFor="isAnonymous" className="text-sm font-medium text-gray-900">
                  Submit this review anonymously
                </label>
              </div>
            </div>

            {/* Submit Button */}
            <div className="flex space-x-4 pt-4">
              <button
                type="button"
                onClick={handleClose}
                disabled={isSubmitting}
                className="flex-1 px-6 py-3 text-gray-700 font-semibold bg-gray-100 border border-gray-300 rounded-xl hover:bg-gray-200 hover:border-gray-400 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex-1 px-6 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold rounded-xl hover:shadow-lg transition-all duration-200 flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>Submitting...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-5 h-5" />
                    <span>Submit Review</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
};

export default ReviewModal;