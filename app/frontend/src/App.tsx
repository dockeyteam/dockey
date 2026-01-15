import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import { DataCacheProvider } from './context/DataCacheContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { 
  LoginPage, 
  RegisterPage, 
  DocumentsPage, 
  DocGroupDetailPage,
  DocumentDetailPage, 
  ProfilePage,
  UsersPage,
  UserDetailPage,
  UserProfilePage
} from './pages';

function App() {
  return (
    <Router>
      <AuthProvider>
        <DataCacheProvider>
        <div className="min-h-screen bg-base-200 relative overflow-hidden">
          {/* Topography Background Pattern */}
          <div 
            className="absolute inset-0 opacity-20 topography-pattern"
          />
          
          {/* Fade at top */}
          <div 
            className="absolute top-0 left-0 right-0 h-[25%] pointer-events-none z-[5]"
            style={{
              background: 'linear-gradient(to bottom, #1e1e1e 0%, transparent 100%)',
            }}
          />
          
          <div className="relative z-10">
            <Toaster 
              position="top-right"
            />
            <Navbar />
          <Routes>
            <Route path="/" element={<Navigate to="/documents" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/documents" element={<DocumentsPage />} />
            <Route path="/docs/:groupName" element={<DocGroupDetailPage />} />
            <Route path="/docs/:groupName/:docSlug" element={<DocGroupDetailPage />} />
            <Route path="/documents/:id" element={<DocumentDetailPage />} />
            <Route
              path="/users"
              element={
                <ProtectedRoute>
                  <UsersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/users/:id"
              element={
                <ProtectedRoute>
                  <UserDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile/:keycloakId"
              element={
                <ProtectedRoute>
                  <UserProfilePage />
                </ProtectedRoute>
              }
            />
          </Routes>
          </div>
        </div>
        </DataCacheProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
