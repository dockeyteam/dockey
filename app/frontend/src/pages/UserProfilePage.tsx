import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { UserServiceGraphQL, type User } from '../services/graphql';
import { useAuth } from '../context/AuthContext';
import { Loading } from '../components';
import toast from 'react-hot-toast';

export const UserProfilePage = () => {
  const { keycloakId } = useParams<{ keycloakId: string }>();
  const { user: currentUser } = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (keycloakId) {
      // If viewing own profile, redirect to /profile
      if (currentUser?.keycloakId === keycloakId) {
        navigate('/profile', { replace: true });
        return;
      }
      loadUser(keycloakId);
    }
  }, [keycloakId, currentUser, navigate]);

  const loadUser = async (id: string) => {
    try {
      setLoading(true);
      const data = await UserServiceGraphQL.getUserByKeycloakId(id);
      setUser(data);
    } catch (error: any) {
      toast.error(error.message || 'Failed to load user profile');
      console.error('Error loading user:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getRoleBadge = (role: string) => {
    switch (role?.toLowerCase()) {
      case 'admin':
        return <span className="badge badge-error badge-sm">Admin</span>;
      case 'user':
        return <span className="badge badge-primary badge-sm">User</span>;
      default:
        return <span className="badge badge-ghost badge-sm">{role}</span>;
    }
  };

  if (loading) {
    return <Loading />;
  }

  if (!user) {
    return (
      <div className="container mx-auto px-6 py-16 text-center">
        <div className="text-6xl mb-4">😕</div>
        <h1 className="text-2xl font-bold mb-2">User Not Found</h1>
        <p className="text-base-content/60 mb-6">
          The user profile you're looking for doesn't exist or has been removed.
        </p>
        <button onClick={() => navigate(-1)} className="btn btn-primary">
          Go Back
        </button>
      </div>
    );
  }

  const displayName = user.fullName || user.username;

  return (
    <div className="container mx-auto px-6 py-8">
      {/* Back button */}
      <button
        onClick={() => navigate(-1)}
        className="btn btn-ghost btn-sm mb-6 gap-2"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          strokeWidth={1.5}
          stroke="currentColor"
          className="w-4 h-4"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18"
          />
        </svg>
        Back
      </button>

      {/* Profile Card */}
      <div className="bg-base-100 rounded-2xl shadow-xl overflow-hidden max-w-2xl mx-auto">
        {/* Header with gradient */}
        <div className="h-32 bg-gradient-to-r from-primary/20 via-secondary/20 to-accent/20" />
        
        {/* Avatar and info */}
        <div className="px-8 pb-8">
          <div className="flex flex-col sm:flex-row items-center sm:items-end gap-4 -mt-16">
            {/* Avatar */}
            <div className="avatar placeholder">
              <div className="bg-primary text-primary-content rounded-full w-32 ring-4 ring-base-100">
                <span className="text-4xl font-bold">
                  {user.fullName
                    ? user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
                    : user.username[0].toUpperCase()}
                </span>
              </div>
            </div>

            {/* Name and role */}
            <div className="text-center sm:text-left pb-2">
              <h1 className="text-2xl font-bold">{displayName}</h1>
              <p className="text-base-content/60">@{user.username}</p>
            </div>

            <div className="sm:ml-auto pb-2">
              {getRoleBadge(user.role)}
            </div>
          </div>

          {/* User details */}
          <div className="mt-8 grid gap-4">
            <div className="flex items-center gap-3 text-base-content/70">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              <span>{user.email}</span>
            </div>

            <div className="flex items-center gap-3 text-base-content/70">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>Member since {formatDate(user.createdAt)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
