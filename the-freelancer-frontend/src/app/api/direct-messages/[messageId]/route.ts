import { NextRequest, NextResponse } from 'next/server';
import { getBackendUrl } from "@/config/api";

export async function PUT(
  request: NextRequest,
  { params }: { params: { messageId: string } }
) {
  try {
    const body = await request.json();
    const messageId = params.messageId;
    
    // Forward request to backend
    const response = await fetch(`${getBackendUrl()}/api/direct-messages/${messageId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        // Forward any authorization headers from the original request
        ...(request.headers.get('authorization') && {
          'Authorization': request.headers.get('authorization')!
        }),
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Backend API error:', response.status, errorText);
      return NextResponse.json(
        { error: `Backend API error: ${response.status}` },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
    
  } catch (error) {
    console.error('Update message API error:', error);
    return NextResponse.json(
      { error: 'Failed to update message' },
      { status: 500 }
    );
  }
}