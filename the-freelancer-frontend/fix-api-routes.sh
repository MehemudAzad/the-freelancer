#!/bin/bash

# Script to fix all API route files by replacing localhost:8080 with environment variables

echo "Starting API routes fix..."

# Define the files that need to be updated
files=(
    "src/app/api/chatbot/chat/route.ts"
    "src/app/api/chatbot/suggestions/route.ts"
    "src/app/api/chatbot/conversation/[sessionId]/route.ts"
    "src/app/api/direct-messages/route.ts"
    "src/app/api/direct-messages/[messageId]/route.ts"
    "src/app/api/recommendations/[id]/route.ts"
    "src/app/api/ai/enhance-description/route.ts"
    "src/app/api/ai/enhance-title/route.ts"
    "src/app/api/ai/analyze-proposal/route.ts"
    "src/app/api/ai/generate-cover-letter/route.ts"
    "src/app/api/ai/suggest-skills/route.ts"
    "src/app/api/notifications/user/[userId]/route.ts"
    "src/app/api/notifications/[notificationId]/read/route.ts"
    "src/app/api/notifications/[notificationId]/route.ts"
)

# Function to update a file
update_file() {
    local file=$1
    echo "Updating $file..."
    
    # Skip if file doesn't exist
    if [ ! -f "$file" ]; then
        echo "  ❌ File not found: $file"
        return
    fi
    
    # Check if file contains localhost:8080
    if ! grep -q "localhost:8080" "$file"; then
        echo "  ✅ Already updated: $file"
        return
    fi
    
    # Create backup
    cp "$file" "$file.backup"
    
    # Add import if not present
    if ! grep -q "import { getBackendUrl }" "$file"; then
        # Find the line with NextRequest import and add our import after it
        if grep -q "import.*NextRequest" "$file"; then
            sed -i '' '/import.*NextRequest/a\
import { getBackendUrl } from "@/config/api";
' "$file"
        else
            # If no NextRequest import, add at the top after other imports
            sed -i '' '1i\
import { getBackendUrl } from "@/config/api";
' "$file"
        fi
    fi
    
    # Replace localhost:8080 with environment variable
    sed -i '' 's|http://localhost:8080|${getBackendUrl()}|g' "$file"
    
    # Also replace any remaining localhost:8080 references
    sed -i '' 's|localhost:8080|${getBackendUrl().replace("http://", "").replace("https://", "")}|g' "$file"
    
    echo "  ✅ Updated: $file"
}

# Update each file
for file in "${files[@]}"; do
    update_file "$file"
done

echo ""
echo "🎉 All API route files have been updated!"
echo ""
echo "Summary of changes:"
echo "1. Added import for getBackendUrl from @/config/api"
echo "2. Replaced http://localhost:8080 with \${getBackendUrl()}"
echo "3. Created backup files with .backup extension"
echo ""
echo "Next steps:"
echo "1. Test the application to ensure all API calls work"
echo "2. Remove .backup files once confirmed working"
echo "3. Commit the changes to version control"