// Test script for proposal discard functionality
// Run this in browser console to test the API calls

const testProposalDiscard = async () => {
  const jobId = 1; // Replace with actual job ID
  const reason = "Found a better proposal";
  
  console.log('🧪 Testing proposal discard functionality...');
  
  try {
    // Test 1: Get escrow details by jobId
    console.log('📋 Step 1: Getting escrow details for jobId:', jobId);
    const escrowResponse = await fetch(`/api/payments/escrow/milestone/${jobId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer YOUR_TOKEN_HERE' // Replace with actual token
      }
    });
    
    if (!escrowResponse.ok) {
      throw new Error(`Escrow API failed: ${escrowResponse.status}`);
    }
    
    const escrowData = await escrowResponse.json();
    console.log('✅ Escrow details retrieved:', escrowData);
    
    // Test 2: Refund the escrow (discard proposal)
    console.log('💰 Step 2: Refunding escrow with ID:', escrowData.id);
    const refundResponse = await fetch('/api/payments/escrow/refund', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer YOUR_TOKEN_HERE' // Replace with actual token
      },
      body: JSON.stringify({
        escrowId: escrowData.id,
        amountCents: escrowData.amountCents,
        reason: reason
      })
    });
    
    if (!refundResponse.ok) {
      throw new Error(`Refund API failed: ${refundResponse.status}`);
    }
    
    const refundData = await refundResponse.json();
    console.log('✅ Refund completed:', refundData);
    
    console.log('🎉 Proposal discard test completed successfully!');
    return { escrowData, refundData };
    
  } catch (error) {
    console.error('❌ Test failed:', error);
    throw error;
  }
};

// Uncomment to run the test
// testProposalDiscard();