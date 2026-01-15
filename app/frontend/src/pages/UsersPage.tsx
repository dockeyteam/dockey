import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserServiceGraphQL, type User } from '../services/graphql';
import { Loading } from '../components';
import toast from 'react-hot-toast';

export const UsersPage = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await UserServiceGraphQL.getUsers();
      setUsers(data);
    } catch (error: any) {
      toast.error(error.message || 'Failed to load users');
      console.error('Error loading users:', error);
    } finally {
      setLoading(false);
    }
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

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return <Loading />;
  }

  return (
    <div className="min-h-screen">
      {/* Header */}
      <div className="border-b border-base-300 bg-base-100">
        <div className="max-w-7xl mx-auto px-8 py-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-light mb-2">Users</h1>
              <p className="text-base-content/60">
                {users.length} {users.length === 1 ? 'user' : 'users'} registered
              </p>
            </div>
            <button
              onClick={loadUsers}
              className="btn btn-ghost btn-sm gap-2"
              title="Refresh users"
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
                  d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99"
                />
              </svg>
              Refresh
            </button>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-8 py-8">
        {users.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-base-content/40 mb-4">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="w-16 h-16 mx-auto mb-4"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z"
                />
              </svg>
              <p className="text-lg">No users found</p>
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Joined</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr
                    key={user.id}
                    className="hover cursor-pointer"
                    onClick={() => navigate(`/users/${user.id}`)}
                  >
                    <td>
                      <div className="flex items-center gap-3">
                        {/* Avatar with initials */}
                        <div className="avatar placeholder">
                          <div className="bg-primary/20 text-primary rounded-full w-10">
                            <span className="text-sm font-medium">
                              {user.fullName?.[0]?.toUpperCase() || ''}
                            </span>
                          </div>
                        </div>
                        <div>
                          <div className="font-medium">
                            {user.fullName}
                          </div>
                          <div className="text-sm text-base-content/60">
                            @{user.username}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="text-base-content/80">{user.email}</td>
                    <td>
                      <span className={`badge ${getRoleColor(user.role)}`}>
                        {user.role}
                      </span>
                    </td>
                    <td className="text-base-content/60">
                      {formatDate(user.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
