import { NextRequest, NextResponse } from 'next/server';
import { getBackendUrl } from "@/config/api";

const BACKEND_URL = getBackendUrl();

export async function POST(request: NextRequest) {
  try {
    console.log('🔗 Proxying chatbot chat request to backend...');
    
    // Get request body
    const body = await request.json();
    console.log('📨 Request body:', body);
    
    // Get authorization header from the incoming request
    const authHeader = request.headers.get('authorization');
    
    // Prepare headers for backend request
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    
    if (authHeader) {
      headers['Authorization'] = authHeader;
    }
    
    console.log('📡 Sending to:', `${BACKEND_URL}/api/ai/chatbot/chat`);
    console.log('📋 Headers:', headers);
    
    // Make request to backend
    const response = await fetch(`${BACKEND_URL}/api/ai/chatbot/chat`, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
    });
    
    console.log('📋 Backend response status:', response.status);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ Backend error:', response.status, errorText);
      return NextResponse.json(
        { error: 'Failed to send message to chatbot' },
        { status: response.status }
      );
    }
    
    const data = await response.json();
    console.log('✅ Successfully got chatbot response:', data);
    
    return NextResponse.json(data);
  } catch (error: any) {
    console.error('❌ Error in chatbot chat proxy:', error.message);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}