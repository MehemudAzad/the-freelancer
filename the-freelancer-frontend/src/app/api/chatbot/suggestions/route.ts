import { NextRequest, NextResponse } from 'next/server';
import { getBackendUrl } from "@/config/api";

const BACKEND_URL = getBackendUrl();

export async function GET(request: NextRequest) {
  try {
    console.log('🔗 Proxying chatbot suggestions request to backend...');
    
    // Get authorization header from the incoming request
    const authHeader = request.headers.get('authorization');
    
    // Prepare headers for backend request
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    
    if (authHeader) {
      headers['Authorization'] = authHeader;
    }
    
    console.log('📡 Fetching from:', `${BACKEND_URL}/api/ai/chatbot/suggestions`);
    
    // Make request to backend
    const response = await fetch(`${BACKEND_URL}/api/ai/chatbot/suggestions`, {
      method: 'GET',
      headers,
    });
    
    console.log('📋 Backend response status:', response.status);
    
    if (!response.ok) {
      console.error('❌ Backend error:', response.status, response.statusText);
      return NextResponse.json(
        { error: 'Failed to fetch suggestions from backend' },
        { status: response.status }
      );
    }
    
    const data = await response.json();
    console.log('✅ Successfully fetched suggestions:', data);
    
    return NextResponse.json(data);
  } catch (error: any) {
    console.error('❌ Error in chatbot suggestions proxy:', error.message);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}