# Cloudinary File Storage Setup

This document explains how to set up Cloudinary for file storage in the workspace service.

## Prerequisites

1. Create a free Cloudinary account at https://cloudinary.com/
2. Get your Cloud Name, API Key, and API Secret from the Cloudinary Dashboard

## Configuration

### Environment Variables

Set the following environment variables or update your `application.properties`:

```properties
# Cloudinary Configuration
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
cloudinary.secure=true

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### Docker Environment (if using Docker)

Add to your docker-compose.yml or environment:

```bash
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
```

## API Endpoints

### Upload File (Multipart)

```
POST /api/workspaces/rooms/{roomId}/files/multipart
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- category: FileCategory (optional - IMAGE, DOCUMENT, CODE, DESIGN, OTHER)
- description: String (optional)
```

### Upload File (JSON)

The existing JSON-based upload endpoint is still available:

```
POST /api/workspaces/rooms/{roomId}/files
Content-Type: application/json

{
  "filename": "document.pdf",
  "originalFilename": "document.pdf",
  "url": "https://example.com/file.pdf",
  "contentType": "application/pdf",
  "fileSize": 1024000,
  "category": "DOCUMENT",
  "description": "Project documentation"
}
```

## Features

1. **Automatic File Upload**: Files are automatically uploaded to Cloudinary
2. **Thumbnail Generation**: Automatic thumbnail generation for images
3. **File Organization**: Files are organized in folders by workspace room
4. **Multiple File Types**: Supports images, documents, videos, and other file types
5. **File Integrity**: SHA-256 checksum calculation for file integrity
6. **Automatic Cleanup**: Files are deleted from Cloudinary when removed from the system

## File Structure in Cloudinary

Files are organized as:
```
workspace/{roomId}/files/{unique_filename}
```

## Response Format

The API returns files with additional Cloudinary information:

```json
{
  "id": 1,
  "roomId": 123,
  "uploaderId": 456,
  "filename": "document_1694598123_abc12345",
  "originalFilename": "document.pdf",
  "url": "https://res.cloudinary.com/your_cloud/raw/upload/v1694598123/workspace/123/files/document_1694598123_abc12345.pdf",
  "thumbnailUrl": "https://res.cloudinary.com/your_cloud/image/upload/c_fill,w_300,h_300/workspace/123/files/image_1694598123_abc12345.jpg",
  "cloudinaryPublicId": "workspace/123/files/document_1694598123_abc12345",
  "cloudinaryResourceType": "raw",
  "contentType": "application/pdf",
  "fileSize": 1024000,
  "category": "DOCUMENT",
  "description": "Project documentation",
  "checksum": "sha256_hash_here",
  "createdAt": "2023-09-13T10:30:00"
}
```

## Error Handling

Common errors:
- File too large (>50MB): HTTP 413 Payload Too Large
- Invalid file type: HTTP 400 Bad Request
- Cloudinary upload failure: HTTP 500 Internal Server Error
- Room not found: HTTP 404 Not Found

## Testing

You can test file uploads using curl:

```bash
curl -X POST \
  http://localhost:8084/api/workspaces/rooms/1/files/multipart \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/your/file.pdf' \
  -F 'category=DOCUMENT' \
  -F 'description=Test document upload'
```

## Security Considerations

1. **File Size Limits**: Maximum 50MB per file (configurable)
2. **File Type Validation**: Validates file types based on content-type
3. **User Authorization**: Ensure proper user authentication before file operations
4. **Secure URLs**: All Cloudinary URLs use HTTPS by default

## Troubleshooting

1. **Upload Fails**: Check Cloudinary credentials in environment variables
2. **Large Files**: Ensure `spring.servlet.multipart.max-file-size` is set appropriately
3. **Missing Thumbnails**: Only images get thumbnails automatically
4. **File Not Found**: Check if file exists in both database and Cloudinary
