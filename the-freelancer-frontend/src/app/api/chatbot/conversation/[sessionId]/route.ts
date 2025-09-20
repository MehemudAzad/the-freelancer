import { NextRequest, NextResponse } from 'next/server';
import { getBackendUrl } from "@/config/api";

const BACKEND_URL = getBackendUrl();

export async function DELETE(
  request: NextRequest,
  { params }: { params: { sessionId: string } }
) {
  try {
    const { sessionId } = params;
    console.log('🗑️ Proxying clear conversation request to backend for session:', sessionId);
    
    // Get authorization header from the incoming request
    const authHeader = request.headers.get('authorization');
    
    // Prepare headers for backend request
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    
    if (authHeader) {
      headers['Authorization'] = authHeader;
    }
    
    console.log('📡 Deleting conversation at:', `${BACKEND_URL}/api/ai/chatbot/conversation/${sessionId}`);
    
    // Make request to backend
    const response = await fetch(`${BACKEND_URL}/api/ai/chatbot/conversation/${sessionId}`, {
      method: 'DELETE',
      headers,
    });
    
    console.log('📋 Backend response status:', response.status);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ Backend error:', response.status, errorText);
      return NextResponse.json(
        { error: 'Failed to clear conversation' },
        { status: response.status }
      );
    }

    console.log('✅ Conversation cleared successfully');
    
    return NextResponse.json({ message: 'Conversation cleared successfully' });
  } catch (error: any) {
    console.error('❌ Error in clear conversation proxy:', error.message);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}