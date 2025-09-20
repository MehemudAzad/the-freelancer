import { NextRequest, NextResponse } from 'next/server';
import { getBackendUrl } from "@/config/api";

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const { id } = params;
    
    // Get Authorization header from the incoming request
    const authHeader = request.headers.get('Authorization');
    
    // Forward request to backend
    const response = await fetch(`${getBackendUrl()}/api/recommendations/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(authHeader && { 'Authorization': authHeader }),
      },
    });

    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`);
    }

    const data = await response.json();
    
    return NextResponse.json(data);
  } catch (error) {
    console.error('AI recommendations error:', error);
    return NextResponse.json(
      { error: 'Failed to fetch job recommendations' },
      { status: 500 }
    );
  }
}