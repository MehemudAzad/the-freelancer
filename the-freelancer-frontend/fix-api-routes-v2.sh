#!/bin/bash

# Script to properly fix all API route files

echo "🔧 Fixing API route files properly..."

# Find all route.ts files that might need fixing
find src/app/api -name "route.ts" -type f | while read -r file; do
    echo "Processing: $file"
    
    # Skip if no localhost references found
    if ! grep -q "localhost\|BACKEND_URL.*getBackendUrl" "$file"; then
        echo "  ✅ No changes needed"
        continue
    fi
    
    # Fix the BACKEND_URL line if it exists and has the template literal issue
    if grep -q "BACKEND_URL.*\${getBackendUrl()}" "$file"; then
        sed -i '' "s/const BACKEND_URL = process.env.BACKEND_URL || '\${getBackendUrl()}';/const BACKEND_URL = getBackendUrl();/g" "$file"
        echo "  ✅ Fixed BACKEND_URL declaration"
    fi
    
    # Add import if not present but file uses localhost
    if grep -q "localhost" "$file" && ! grep -q "import.*getBackendUrl" "$file"; then
        # Add the import after the NextRequest import
        sed -i '' "/import.*NextRequest/a\\
import { getBackendUrl } from \"@/config/api\";
" "$file"
        echo "  ✅ Added getBackendUrl import"
    fi
    
    # Replace any remaining localhost:8080 references
    if grep -q "http://localhost:8080" "$file"; then
        sed -i '' 's|http://localhost:8080|${BACKEND_URL}|g' "$file"
        echo "  ✅ Replaced localhost:8080 URLs"
    fi
    
done

echo "🎉 All files processed!"