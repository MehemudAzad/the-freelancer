# Real-Time Chat System Documentation

## 🚀 Overview

The workspace service now includes a complete real-time chat system with WebSocket support for instant messaging, typing indicators, read receipts, and more.

## 📡 WebSocket Configuration

### Connection Endpoint
```
ws://localhost:8084/ws
```

### Authentication Headers (Required)
```http
X-User-Id: <user-id>
X-User-Role: <CLIENT|FREELANCER|ADMIN>
X-User-Email: <user-email>
```

### WebSocket Topics

#### 1. **Room Messages**
- **Subscribe:** `/topic/room/{roomId}/messages`
- **Purpose:** Receive new messages in real-time
- **Payload:** `MessageResponseDto`

#### 2. **Typing Indicators**  
- **Subscribe:** `/topic/room/{roomId}/typing`
- **Send:** `/app/room/{roomId}/typing`
- **Purpose:** Show who is typing
- **Payload:** `TypingStatusDto`

#### 3. **Read Receipts**
- **Subscribe:** `/topic/room/{roomId}/read`  
- **Purpose:** Show when messages are read
- **Payload:** `MessageReadReceiptDto`

#### 4. **User Status**
- **Subscribe:** `/topic/room/{roomId}/status`
- **Purpose:** Show online/offline status
- **Payload:** `UserStatusDto`

---

## 🔌 WebSocket Usage Examples

### JavaScript Client (SockJS + STOMP)

```javascript
// 1. Connect to WebSocket
const socket = new SockJS('http://localhost:8084/ws', null, {
    headers: {
        'X-User-Id': 'user123',
        'X-User-Role': 'CLIENT', 
        'X-User-Email': 'user@example.com'
    }
});

const stompClient = Stomp.over(socket);

// 2. Connect and subscribe
stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to room messages
    stompClient.subscribe('/topic/room/1/messages', function(message) {
        const messageData = JSON.parse(message.body);
        displayMessage(messageData);
    });
    
    // Subscribe to typing indicators
    stompClient.subscribe('/topic/room/1/typing', function(message) {
        const typingData = JSON.parse(message.body);
        showTypingIndicator(typingData);
    });
    
    // Subscribe to read receipts
    stompClient.subscribe('/topic/room/1/read', function(message) {
        const readData = JSON.parse(message.body);
        markMessageAsRead(readData);
    });
});

// 3. Send a message via WebSocket
function sendMessage(roomId, content) {
    stompClient.send('/app/room/' + roomId + '/sendMessage', {}, JSON.stringify({
        content: content,
        messageType: 'TEXT'
    }));
}

// 4. Send typing indicator
function sendTyping(roomId, isTyping) {
    stompClient.send('/app/room/' + roomId + '/typing', {}, JSON.stringify({
        typing: isTyping,
        userId: 'user123',
        roomId: roomId
    }));
}
```

---

## 🎯 REST API Endpoints

### **1. Send Message**
```http
POST /api/workspaces/rooms/{roomId}/messages
Content-Type: application/json
X-User-Id: user123
X-User-Role: CLIENT

{
  "content": "Hello, how's the project going?",
  "messageType": "TEXT",
  "replyToId": null,
  "attachments": []
}
```

### **2. Get Message History**
```http
GET /api/workspaces/rooms/{roomId}/messages?page=0&limit=50&before=msg123
X-User-Id: user123
X-User-Role: CLIENT
```

### **3. Update Message**
```http
PUT /api/workspaces/rooms/{roomId}/messages/{messageId}
Content-Type: application/json
X-User-Id: user123

{
  "content": "Updated message content"
}
```

### **4. Delete Message**
```http
DELETE /api/workspaces/rooms/{roomId}/messages/{messageId}
X-User-Id: user123
```

### **5. Search Messages**
```http
GET /api/workspaces/rooms/{roomId}/messages/search?q=project&type=TEXT&page=0&limit=20
X-User-Id: user123
```

### **6. Send Typing Indicator (REST)**
```http
POST /api/workspaces/rooms/{roomId}/typing
Content-Type: application/json
X-User-Id: user123

{
  "typing": true,
  "userId": "user123", 
  "roomId": "1"
}
```

### **7. Mark Messages as Read**
```http
POST /api/workspaces/rooms/{roomId}/read
Content-Type: application/json
X-User-Id: user123

["msg123", "msg124", "msg125"]
```

### **8. Get Unread Message Count**
```http
GET /api/workspaces/rooms/{roomId}/unread-count
X-User-Id: user123
```

---

## 📊 Data Models

### MessageResponseDto
```json
{
  "id": "msg123",
  "roomId": "1", 
  "senderId": "user456",
  "content": "Hello there!",
  "messageType": "TEXT",
  "replyToId": null,
  "attachments": [],
  "editedAt": null,
  "createdAt": "2025-09-13T10:30:00"
}
```

### TypingStatusDto
```json
{
  "userId": "user123",
  "roomId": "1",
  "typing": true,
  "timestamp": 1726220400000
}
```

### MessageReadReceiptDto
```json
{
  "messageId": "msg123",
  "userId": "user456", 
  "readAt": "2025-09-13T10:35:00"
}
```

---

## 🔧 Features

### ✅ **Implemented**
- [x] Real-time message delivery via WebSocket
- [x] Message history with pagination
- [x] Typing indicators  
- [x] Message editing and deletion
- [x] Message search functionality
- [x] Read receipts
- [x] File attachments support
- [x] Threaded conversations (reply-to)
- [x] System messages
- [x] Authentication & authorization
- [x] Room-based messaging
- [x] Unread message count

### 🎯 **Message Types Supported**
- `TEXT` - Plain text messages
- `FILE` - File attachments
- `IMAGE` - Image files  
- `VIDEO` - Video files
- `DOCUMENT` - Documents (PDF, DOC, etc.)
- `SYSTEM` - System-generated messages

---

## 🛡️ Security

- **Authentication Required:** All endpoints require valid user headers
- **Room Access Control:** Users can only access rooms they belong to
- **WebSocket Authentication:** Headers validated on connection
- **CORS Configured:** Limited to allowed origins

---

## 🚦 Error Handling

### Common HTTP Status Codes
- `200 OK` - Success
- `201 Created` - Message created
- `400 Bad Request` - Invalid data
- `401 Unauthorized` - Authentication required  
- `403 Forbidden` - Access denied
- `404 Not Found` - Room/message not found
- `500 Internal Server Error` - Server error

### WebSocket Error Handling
- Failed messages are logged but don't break the connection
- Authentication failures reject the WebSocket connection
- Invalid room access is handled gracefully

---

## ⚡ Performance Considerations

- **Message Broadcasting:** Uses Spring's `SimpMessagingTemplate` for efficient delivery
- **Database Queries:** Optimized with proper indexing on room_id and timestamps
- **Pagination:** All message endpoints support pagination to handle large chat histories
- **Connection Management:** WebSocket connections are managed automatically

---

## 🧪 Testing the Chat System

### 1. **Connect via WebSocket**
Use the JavaScript example above or any STOMP-compatible client.

### 2. **Send Test Message**
```bash
curl -X POST http://localhost:8084/api/workspaces/rooms/1/messages \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 123" \
  -H "X-User-Role: CLIENT" \
  -d '{"content": "Test message", "messageType": "TEXT"}'
```

### 3. **Check Message History**
```bash
curl http://localhost:8084/api/workspaces/rooms/1/messages \
  -H "X-User-Id: 123" \
  -H "X-User-Role: CLIENT"
```

---

## 🎉 **The chat system is now fully functional with real-time capabilities!**

Users can now communicate instantly with WebSocket-powered messaging, see typing indicators, receive read receipts, and enjoy a complete chat experience within their workspace rooms! 🚀
