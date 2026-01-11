# Comments Service

The Comments Service manages user comments on documents, including likes and comment tracking. It uses MongoDB for storage and publishes events to Kafka to notify the docs-service about comment count changes.

## Features

- **Comment Management**: Create, retrieve, and delete comments on specific document lines
- **Like System**: Users can like/unlike comments with tracking
- **MongoDB Storage**: All comments stored in MongoDB with indexing
- **Kafka Events**: Publishes comment events to notify docs-service of count changes
- **Line Tracking**: Tracks comment counts per document line

## API Endpoints

### Create Comment
**Requires Authentication** - Include JWT token in Authorization header

```http
POST /api/comments
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "docId": "doc-123",
  "lineNumber": 42,
  "content": "This is a great point!"
}
```

**Note**: User information (userId and userName) is automatically extracted from the JWT token.

**Response**: Returns created comment with ID

### Get Comments by Document
```http
GET /api/comments/doc/{docId}?userId=user-456
```

Returns all comments for a document, with `likedByCurrentUser` flag based on userId.

### Get Comments by Document and Line
```http
GET /api/comments/doc/{docId}/line/{lineNumber}?userId=user-456
```

Returns all comments for a specific line in a document.

### Get Comment Counts Per Line
```http
GET /api/comments/doc/{docId}/counts
```

Returns a map of line numbers to comment counts:
```json
{
  "docId": "doc-123",
  "lineCommentCounts": {
    "10": 3,
    "25": 1,
    "42": 5
  }
}
```

### Like Comment
**Requires Authentication**

```http
POST /api/comments/{commentId}/like
Authorization: Bearer <jwt-token>
```

Adds a like from the authenticated user. Idempotent - returns success if already liked.

### Unlike Comment
**Requires Authentication**

```http
POST /api/comments/{commentId}/unlike
Authorization: Bearer <jwt-token>
```

Removes a like from the authenticated user. Idempotent - returns success if not liked.

### Delete Comment
```http
DELETE /api/comments/{commentId}
```

Soft deletes a comment (sets `isDeleted` flag).

### Health Check
```http
GET /health
```

## Kafka Events

The service publishes events to the `dockey-comments` topic:

### Event Types

#### COMMENT_ADDED
Published when a new comment is created.
```json
{
  "eventType": "COMMENT_ADDED",
  "commentId": "507f1f77bcf86cd799439011",
  "docId": "doc-123",
  "lineNumber": 42,
  "userId": "user-456",
  "newCommentCount": 5,
  "timestamp": "2026-01-11T10:30:00"
}
```

#### COMMENT_DELETED
Published when a comment is deleted.
```json
{
  "eventType": "COMMENT_DELETED",
  "commentId": "507f1f77bcf86cd799439011",
  "docId": "doc-123",
  "lineNumber": 42,
  "userId": "user-456",
  "newCommentCount": 4,
  "timestamp": "2026-01-11T10:35:00"
}
```

#### COMMENT_LIKED
Published when a user likes a comment.
```json
{
  "eventType": "COMMENT_LIKED",
  "commentId": "507f1f77bcf86cd799439011",
  "docId": "doc-123",
  "lineNumber": 42,
  "userId": "user-789",
  "newCommentCount": null,
  "timestamp": "2026-01-11T10:32:00"
}
```

#### COMMENT_UNLIKED
Published when a user unlikes a comment.

## MongoDB Schema

### Comments Collection

```javascript
{
  "_id": ObjectId,
  "docId": String,           // Document identifier
  "lineNumber": Integer,      // Line number in document
  "userId": String,           // Comment author ID
  "userName": String,         // Comment author name
  "content": String,          // Comment text (max 5000 chars)
  "createdAt": Date,
  "updatedAt": Date,
  "likedByUserIds": [String], // Array of user IDs who liked
  "likeCount": Integer,       // Total like count
  "isDeleted": Boolean        // Soft delete flag
}
```

### Recommended Indexes

```javascript
// For fetching comments by document
db.comments.createIndex({ "docId": 1, "isDeleted": 1, "lineNumber": 1, "createdAt": 1 })

// For fetching comments by document and line
db.comments.createIndex({ "docId": 1, "lineNumber": 1, "isDeleted": 1, "createdAt": 1 })

// For comment count aggregations
db.comments.createIndex({ "docId": 1, "isDeleted": 1 })
```

## Configuration

Environment variables:
- `MONGODB_CONNECTION_STRING`: MongoDB connection (default: mongodb://admin:admin@localhost:27017/commentsdb?authSource=admin)
- `MONGODB_DATABASE`: Database name (default: commentsdb)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka servers (default: localhost:9092)

## Integration with Docs-Service

The docs-service should consume events from the `dockey-comments` topic to:

1. **Track comment counts per line**: Update document metadata with line comment counts
2. **Display comment indicators**: Show which lines have comments
3. **Update in real-time**: React to new comments, deletions, and likes

Example docs-service consumer logic:
```java
@KafkaListener(topics = "dockey-comments", groupId = "docs-service-group")
public void handleCommentEvent(CommentEventMessage event) {
    switch (event.getEventType()) {
        case "COMMENT_ADDED":
        case "COMMENT_DELETED":
            // Update line comment count in document metadata
            updateDocumentLineCommentCount(
                event.getDocId(), 
                event.getLineNumber(), 
                event.getNewCommentCount()
            );
            break;
        case "COMMENT_LIKED":
        case "COMMENT_UNLIKED":
            // Optional: Track engagement metrics
            break;
    }
}
```

## Running

```bash
docker-compose up comments-service
```

The service runs on port 8082.

## Example Usage Flow

1. **User opens document** in docs-service
2. **Docs-service fetches comment counts**: `GET /api/comments/doc/{docId}/counts`
3. **UI displays comment indicators** on lines with comments
4. **User clicks line 42** to view comments
5. **Frontend fetches comments**: `GET /api/comments/doc/{docId}/line/42?userId=current-user`
6. **User adds a comment**: `POST /api/comments`
7. **Comments-service publishes COMMENT_ADDED** event to Kafka
8. **Docs-service consumer updates** line 42 comment count
9. **All connected clients receive update** via WebSocket/SSE from docs-service

## Testing

```bash
# First, get an access token from Keycloak
TOKEN=$(curl -X POST http://localhost:8180/realms/dockey/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=youruser" \
  -d "password=yourpassword" \
  -d "grant_type=password" \
  -d "client_id=dockey-client" \
  | jq -r '.access_token')

# Create a comment (authenticated)
curl -X POST http://localhost:8082/api/comments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "docId": "doc-123",
    "lineNumber": 42,
    "content": "Great explanation!"
  }'

# Get comments for a document
curl http://localhost:8082/api/comments/doc/doc-123?userId=user-456

# Get comment counts
curl http://localhost:8082/api/comments/doc/doc-123/counts

# Like a comment (authenticated)
curl -X POST http://localhost:8082/api/comments/{commentId}/like \
  -H "Authorization: Bearer $TOKEN"

# Unlike a comment (authenticated)
curl -X POST http://localhost:8082/api/comments/{commentId}/unlike \
  -H "Authorization: Bearer $TOKEN"
```
