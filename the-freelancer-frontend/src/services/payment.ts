import api from '@/lib/api';

export interface PaymentRequest {
  jobId: number;
  amountCents: number;
  currency: string;
  paymentMethodId: string;
  clientEmail: string;
  clientName: string;
  description: string;
}

export interface PaymentResponse {
  id: string;
  status: 'succeeded' | 'pending' | 'failed';
  amountCents: number;
  currency: string;
  createdAt: string;
}

export interface EscrowDetails {
  id: string;
  jobId: number;
  paymentIntentId: string;
  amountCents: number;
  currency: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  clientSecret: string | null;
}

export interface RefundRequest {
  escrowId: string;
  amountCents: number;
  reason: string;
}

class PaymentService {
  // Fund escrow for a job
  async fundEscrow(paymentData: PaymentRequest): Promise<PaymentResponse> {
    console.log('funding with the data : ', paymentData);
    const response = await api.post('/api/payments/escrow/fund', paymentData);
    return response.data;
  }

  // Release payment to freelancer
  async releasePayment(jobId: number): Promise<PaymentResponse> {
    const response = await api.post(`/api/payments/escrow/release/${jobId}`);
    return response.data;
  }

  // Get payment history
  async getPaymentHistory(): Promise<PaymentResponse[]> {
    const response = await api.get('/api/payments/history');
    return response.data;
  }

  // Get escrow details by jobId to retrieve escrowId
  async getEscrowByJobId(jobId: number): Promise<EscrowDetails> {
    console.log('🔍 Getting escrow details for jobId:', jobId);
    const response = await api.get(`/api/payments/escrow/milestone/${jobId}`);
    console.log('✅ Escrow details retrieved:', response.data);
    return response.data;
  }

  // Refund/discard proposal by refunding the escrow
  async refundEscrow(refundData: RefundRequest): Promise<PaymentResponse> {
    console.log('💰 Refunding escrow with data:', refundData);
    const response = await api.post('/api/payments/escrow/refund', refundData);
    console.log('✅ Escrow refunded successfully:', response.data);
    return response.data;
  }

  // Combined method to discard a proposal by jobId
  async discardProposal(jobId: number, reason: string = 'Proposal discarded by client'): Promise<PaymentResponse> {
    try {
      console.log('🗑️ Discarding proposal for jobId:', jobId);
      
      // First, get the escrow details to retrieve escrowId
      const escrowDetails = await this.getEscrowByJobId(jobId);
      
      if (!escrowDetails || !escrowDetails.id) {
        throw new Error('Escrow not found for this job');
      }

      console.log('📝 Found escrow ID:', escrowDetails.id);

      // Then refund the full amount
      const refundData: RefundRequest = {
        escrowId: escrowDetails.id,
        amountCents: escrowDetails.amountCents,
        reason: reason
      };

      const refundResult = await this.refundEscrow(refundData);
      
      console.log('✅ Proposal discarded successfully');
      return refundResult;
    } catch (error: any) {
      console.error('❌ Error discarding proposal:', error);
      throw new Error(error.response?.data?.message || 'Failed to discard proposal');
    }
  }
}

const paymentService = new PaymentService();
export default paymentService;