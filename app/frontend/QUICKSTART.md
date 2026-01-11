# Dockey Frontend - Quick Start Guide

## ✅ What's Been Implemented

### Core Infrastructure
- ✅ React 19 + TypeScript + Vite project setup
- ✅ Tailwind CSS + DaisyUI for styling
- ✅ React Router for navigation
- ✅ Axios with automatic token refresh interceptors
- ✅ Production-ready Dockerfile with Nginx
- ✅ Docker Compose integration

### Authentication & Authorization
- ✅ JWT-based authentication with Keycloak
- ✅ Token management (access + refresh tokens)
- ✅ Automatic token refresh 60s before expiry
- ✅ Protected routes with redirect to login
- ✅ AuthContext for global auth state

### Service Layer (Ready to Use)
- ✅ **authService** - Login, register, logout, token refresh
- ✅ **userService** - Get current user, CRUD operations
- ✅ **documentService** - CRUD documents, line comment counts
- ✅ **commentService** - Create, get, like/unlike comments

### TypeScript Types
- ✅ User, Document, Comment types
- ✅ Request/Response DTOs
- ✅ API error types
- ✅ Full type safety across the app

### Pages & Components
- ✅ **HomePage** - Landing page
- ✅ **LoginPage** - User authentication
- ✅ **RegisterPage** - New user registration
- ✅ **DocumentsPage** - List all documents
- ✅ **ProfilePage** - User profile view
- ✅ **Navbar** - Navigation with user dropdown
- ✅ **ProtectedRoute** - Auth guard component
- ✅ **Loading** - Reusable loading spinner

### Custom Hooks
- ✅ **useAuth** - Access auth context
- ✅ **useAsync** - Handle async operations
- ✅ **useDebounce** - Debounce input values

## 🚀 Running the Frontend

### Local Development
```bash
cd app/frontend
npm install
npm run dev
```

Access at: `http://localhost:5173`

### With Docker Compose
```bash
cd app
docker-compose up frontend
```

Access at: `http://localhost:3000`

### Full Stack with All Services
```bash
cd app
docker-compose up
```

Services:
- Frontend: `http://localhost:3000`
- User Service: `http://localhost:8081`
- Docs Service: `http://localhost:8080`
- Comments Service: `http://localhost:8082`
- Keycloak: `http://localhost:8180`

## 🎯 Next Steps for Development

### 1. Document Viewer with Line Comments
Create a new page to display document content with line-by-line commenting:

```typescript
// src/pages/DocumentViewerPage.tsx
import { useParams } from 'react-router-dom';
import { documentService, commentService } from '../services';

export const DocumentViewerPage: React.FC = () => {
  const { id } = useParams();
  const [document, setDocument] = useState<DocumentResponse | null>(null);
  const [selectedLine, setSelectedLine] = useState<number | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);

  // Fetch document and comments
  // Display content with line numbers
  // Show comment badges per line
  // Handle comment creation and likes
};
```

### 2. Comment Components
Create reusable comment UI components:

```typescript
// src/components/CommentThread.tsx
// src/components/CommentItem.tsx
// src/components/CommentForm.tsx
```

### 3. Document Editor
Build a form to create/edit documents:

```typescript
// src/pages/DocumentEditorPage.tsx
// Use documentService.createDocument() or updateDocument()
// Support DRAFT/PUBLISHED status
```

### 4. Enhanced Profile Page
Show user contributions and statistics:

```typescript
// Fetch user's documents
const docs = await documentService.getDocumentsByUserId(userId);

// Display comment count, document count, etc.
```

### 5. Real-time Updates (Optional)
Implement polling or WebSocket for live comment updates:

```typescript
// Poll for new comments every 30 seconds
useEffect(() => {
  const interval = setInterval(() => {
    loadComments();
  }, 30000);
  return () => clearInterval(interval);
}, [docId]);
```

## 📁 Using the Service Layer

All backend integrations are ready to use:

```typescript
import { 
  authService, 
  userService, 
  documentService, 
  commentService 
} from './services';

// Authentication
await authService.login({ username, password });
await authService.register({ username, email, password });
authService.logout();

// Documents
const docs = await documentService.getAllDocuments();
const doc = await documentService.getDocumentById(1);
await documentService.createDocument({
  title: 'My Doc',
  content: 'Line 1\nLine 2',
  status: 'PUBLISHED'
});

// Comments
await commentService.createComment({
  docId: '123',
  lineNumber: 5,
  content: 'Great point!'
});
const comments = await commentService.getCommentsByLine('123', 5);
await commentService.likeComment(commentId);

// Users
const currentUser = await userService.getCurrentUser();
const user = await userService.getUserById(123);
```

## 🎨 DaisyUI Components

Available components for quick development:

```tsx
// Buttons
<button className="btn btn-primary">Primary</button>
<button className="btn btn-secondary">Secondary</button>
<button className="btn btn-outline">Outline</button>

// Cards
<div className="card bg-base-100 shadow-xl">
  <div className="card-body">
    <h2 className="card-title">Title</h2>
    <p>Content</p>
  </div>
</div>

// Badges
<span className="badge badge-primary">Primary</span>
<span className="badge badge-outline">Outline</span>

// Modals
<dialog className="modal">
  <div className="modal-box">
    <h3 className="font-bold text-lg">Modal Title</h3>
    <p>Modal content</p>
  </div>
</dialog>

// Alerts
<div className="alert alert-success">Success message</div>
<div className="alert alert-error">Error message</div>

// Loading
<span className="loading loading-spinner loading-lg"></span>

// Dropdown
<div className="dropdown">
  <button className="btn">Click</button>
  <ul className="dropdown-content menu">
    <li><a>Item 1</a></li>
  </ul>
</div>
```

## 🔐 Authentication Flow

The authentication is fully implemented and works as follows:

1. User visits `/login` or `/register`
2. Form submission calls `authService.login()` or `authService.register()`
3. Tokens stored in localStorage
4. AuthContext updated with user data
5. User redirected to `/documents`
6. All API calls automatically include auth header
7. Token refreshes automatically before expiry
8. On 401 error, redirects to login

## 🐛 Debugging

Enable detailed logging:

```typescript
// In api.client.ts, add console.logs to interceptors
instance.interceptors.request.use(
  async (config) => {
    console.log('Request:', config.url, config.headers);
    // ...
  }
);
```

Check localStorage:

```javascript
// In browser console
localStorage.getItem('dockey_access_token')
localStorage.getItem('dockey_user')
```

## 📝 Environment Variables

Create `.env` for local development:

```env
VITE_USER_SERVICE_URL=http://localhost:8081
VITE_DOCS_SERVICE_URL=http://localhost:8080
VITE_COMMENTS_SERVICE_URL=http://localhost:8082
```

For Docker, these are set in `docker-compose.yml`.

## 🎯 Component Development Pattern

When building new components:

1. **Import types** from `./types`
2. **Use services** from `./services`
3. **Handle loading** with `<Loading />` component
4. **Handle errors** with DaisyUI alerts
5. **Use custom hooks** for complex logic

Example:

```typescript
import { useState, useEffect } from 'react';
import { documentService } from '../services';
import type { Document } from '../types';
import { Loading } from '../components';

export const MyComponent: React.FC = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    const load = async () => {
      try {
        const docs = await documentService.getAllDocuments();
        setDocuments(docs);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to load');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <Loading fullScreen />;
  if (error) return <div className="alert alert-error">{error}</div>;
  
  return (
    <div>
      {/* Your component UI */}
    </div>
  );
};
```

## ✅ What's Working

- ✅ Build passes without errors
- ✅ TypeScript strict mode enabled
- ✅ All API clients configured with interceptors
- ✅ Authentication context and token refresh
- ✅ Docker integration ready
- ✅ Tailwind + DaisyUI configured
- ✅ Routing with protected routes
- ✅ Production-ready Dockerfile with Nginx

## 🚧 To Be Implemented

The foundation is solid. Focus on:

1. Document viewer page with line numbers
2. Comment thread UI components
3. Comment creation and like functionality
4. Document editor for creating new docs
5. User contributions on profile page
6. Search and filtering
7. Toast notifications
8. Theme switcher (dark mode)
9. Admin dashboard for user management
10. Real-time comment updates

All the infrastructure and services are ready - just build the UI components and connect them to the existing service layer!
