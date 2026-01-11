import React from 'react';
import { useAuth } from '../context/AuthContext';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();

  if (!user) {
    return null;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Profile</h1>

      <div className="card bg-base-100 shadow-xl max-w-2xl">
        <div className="card-body">
          <div className="flex items-center gap-4 mb-6">
            <div className="avatar placeholder">
              <div className="bg-primary text-primary-content rounded-full w-20">
                <span className="text-3xl">{user.username[0].toUpperCase()}</span>
              </div>
            </div>
            <div>
              <h2 className="text-2xl font-bold">{user.username}</h2>
              <p className="text-base-content/70">{user.email}</p>
            </div>
          </div>

          <div className="divider"></div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-base-content/70">Full Name</p>
              <p className="font-semibold">{user.fullName || 'Not provided'}</p>
            </div>
            <div>
              <p className="text-sm text-base-content/70">Role</p>
              <p>
                <span className="badge badge-primary">{user.role}</span>
              </p>
            </div>
            <div>
              <p className="text-sm text-base-content/70">User ID</p>
              <p className="font-mono text-sm">{user.id}</p>
            </div>
            <div>
              <p className="text-sm text-base-content/70">Keycloak ID</p>
              <p className="font-mono text-sm truncate">{user.keycloakId}</p>
            </div>
            <div>
              <p className="text-sm text-base-content/70">Member Since</p>
              <p>{new Date(user.createdAt).toLocaleDateString()}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
