import api from '@/lib/api';

export interface CreateStripeAccountRequest {
  email: string;
  country: string;
}

export interface CreateStripeAccountResponse {
  accountId: string;
  email: string;
  country: string;
  chargesEnabled: boolean;
  payoutsEnabled: boolean;
  detailsSubmitted: boolean;
  type: string;
}

export interface CreateOnboardingLinkRequest {
  refreshUrl: string;
  returnUrl: string;
}

export interface CreateOnboardingLinkResponse {
  url: string;
  created: number;
  expires_at: number;
}

export const stripeAPI = {
  // Create Stripe account
  createAccount: async (data: CreateStripeAccountRequest): Promise<CreateStripeAccountResponse> => {
    const response = await api.post('/api/payments/accounts/create', data);
    return response.data;
  },

  // Create onboarding link
  createOnboardingLink: async (
    accountId: string, 
    data: CreateOnboardingLinkRequest
  ): Promise<CreateOnboardingLinkResponse> => {
    const response = await api.post(`/api/payments/accounts/${accountId}/onboarding-link`, null, {
      params: data
    });
    return response.data;
  },

  // Get user's Stripe status
  getStripeStatus: async (): Promise<{
    userId: number;
    hasStripeAccount: boolean;
    stripeAccountId: string | null;
  }> => {
    const response = await api.get('/api/auth/me/stripe-status');
    return response.data;
  },
};

export default stripeAPI;