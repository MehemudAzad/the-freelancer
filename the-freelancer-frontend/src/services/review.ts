import api from '@/lib/api';

export interface ReviewData {
  freelancerId: number;
  jobId: string;
  contractId: string;
  overallRating: number;
  qualityRating: number;
  communicationRating: number;
  timelinessRating: number;
  professionalismRating: number;
  title: string;
  comment: string;
  reviewType: 'GIG_REVIEW' | 'JOB_REVIEW';
  isAnonymous: boolean;
  wouldRecommend: boolean;
}

export interface Review {
  id: number;
  freelancerId: number;
  reviewerId: number;
  jobId: string;
  contractId: string;
  overallRating: number;
  qualityRating: number;
  communicationRating: number;
  timelinessRating: number;
  professionalismRating: number;
  title: string;
  comment: string;
  reviewType: 'GIG_REVIEW' | 'JOB_REVIEW';
  isAnonymous: boolean;
  wouldRecommend: boolean;
  createdAt: string;
  updatedAt: string;
}

class ReviewService {
  // Submit a new review for a freelancer
  async submitReview(reviewData: ReviewData): Promise<Review> {
    const response = await api.post('/api/reviews', reviewData);
    return response.data;
  }

  // Get reviews for a specific freelancer
  async getFreelancerReviews(freelancerId: number): Promise<Review[]> {
    const response = await api.get(`/api/reviews/freelancers/${freelancerId}`);
    // The API returns a paginated response with reviews array
    return response.data.reviews || [];
  }

  // Get reviews for a specific contract (to check if review already exists)
  async getContractReview(contractId: string): Promise<Review | null> {
    try {
      const response = await api.get(`/api/reviews/contract/${contractId}`);
      return response.data;
    } catch (error) {
      // Return null if no review found (404)
      return null;
    }
  }
}

const reviewService = new ReviewService();
export default reviewService;