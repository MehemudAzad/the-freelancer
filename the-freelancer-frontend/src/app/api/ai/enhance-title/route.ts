import { NextRequest, NextResponse } from 'next/server';
import { getBackendUrl } from "@/config/api";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    
    // Forward request to backend
    const response = await fetch('${getBackendUrl()}/api/ai/enhance-title', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`);
    }

    const data = await response.json();
    
    return NextResponse.json(data);
  } catch (error) {
    console.error('AI title enhancement error:', error);
    return NextResponse.json(
      { error: 'Failed to enhance job title' },
      { status: 500 }
    );
  }
}