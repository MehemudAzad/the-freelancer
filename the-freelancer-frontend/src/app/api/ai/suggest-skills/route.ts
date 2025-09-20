import { NextRequest, NextResponse } from 'next/server';
import { getBackendUrl } from "@/config/api";

const BACKEND_URL = process.env.BACKEND_URL || process.env.NEXT_PUBLIC_API_URL || '${getBackendUrl()}';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    
    // Forward request to backend
    const response = await fetch(`${BACKEND_URL}/api/ai/suggest-skills`, {
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
    console.error('AI skills suggestion error:', error);
    return NextResponse.json(
      { error: 'Failed to suggest skills' },
      { status: 500 }
    );
  }
}