import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { UserServiceGraphQL, type User } from '../services/graphql';
import { Loading } from '../components';
import toast from 'react-hot-toast';

export const UserDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      loadUser(id);
    }
  }, [id]);

  const loadUser = async (userId: string) => {
    try {
      setLoading(true);
      const data = await UserServiceGraphQL.getUserById(userId);
      setUser(data);
    } catch (error: any) {
      toast.error(error.message || 'Failed to load user');
      console.error('Error loading user:', error);
      navigate('/users');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getRoleColor = (role: string) => {
    switch (role.toLowerCase()) {
      case 'admin':
        return 'badge-error';
      case 'user':
        return 'badge-primary';
      default:
        return 'badge-ghost';
    }
  };

  if (loading) {
    return <Loading />;
  }

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen">
      {/* Header */}
      <div className="border-b border-base-300 bg-base-100">
        <div className="max-w-5xl mx-auto px-8 py-6">
          <button
            onClick={() => navigate('/users')}
            className="btn btn-ghost btn-sm mb-4 gap-2"
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
            Back to Users
          </button>

          <div className="flex items-center gap-6">
            {/* Large Avatar */}
            <div className="avatar placeholder">
              <div className="bg-primary/20 text-primary rounded-full w-24">
                <span className="text-3xl font-medium">
                  {user.fullName?.[0]?.toUpperCase() || ''}
                </span>
              </div>
            </div>

            <div>
              <h1 className="text-4xl font-light mb-2">
                {user.fullName}
              </h1>
              <div className="flex items-center gap-3 text-base-content/60">
                <span>@{user.username}</span>
                <span>•</span>
                <span className={`badge ${getRoleColor(user.role)}`}>
                  {user.role}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-5xl mx-auto px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* User Information Card */}
          <div className="card bg-base-100 border border-base-300">
            <div className="card-body">
              <h2 className="card-title text-xl font-light mb-4">
                User Information
              </h2>

              <div className="space-y-4">
                <div>
                  <label className="text-sm text-base-content/60">User ID</label>
                  <p className="font-mono text-sm mt-1">{user.id}</p>
                </div>

                <div>
                  <label className="text-sm text-base-content/60">Username</label>
                  <p className="mt-1">@{user.username}</p>
                </div>

                <div>
                  <label className="text-sm text-base-content/60">Email</label>
                  <p className="mt-1">{user.email}</p>
                </div>

                <div>
                  <label className="text-sm text-base-content/60">Full Name</label>
                  <p className="mt-1">
                    {user.fullName}
                  </p>
                </div>

                <div>
                  <label className="text-sm text-base-content/60">Role</label>
                  <div className="mt-1">
                    <span className={`badge ${getRoleColor(user.role)}`}>
                      {user.role}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Account Activity Card */}
          <div className="card bg-base-100 border border-base-300">
            <div className="card-body">
              <h2 className="card-title text-xl font-light mb-4">
                Account Activity
              </h2>

              <div className="space-y-4">
                <div>
                  <label className="text-sm text-base-content/60">
                    Member Since
                  </label>
                  <p className="mt-1">{formatDate(user.createdAt)}</p>
                </div>

                <div className="pt-4 border-t border-base-300">
                  <p className="text-sm text-base-content/60">
                    This user was registered{' '}
                    {Math.floor(
                      (Date.now() - new Date(user.createdAt).getTime()) /
                        (1000 * 60 * 60 * 24)
                    )}{' '}
                    days ago
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
