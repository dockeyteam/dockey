import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../services';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

export const ProfilePage: React.FC = () => {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editForm, setEditForm] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
  });
  const [isLoading, setIsLoading] = useState(false);

  if (!user) {
    return null;
  }

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    try {
      // Send only the fields we want to update
      await userService.updateUser(user.id, {
        fullName: editForm.fullName,
        email: editForm.email,
      });
      await refreshUser();
      setIsEditModalOpen(false);
      toast.success('Profile updated successfully');
    } catch (error) {
      toast.error((t) => (
        <div className="flex items-center gap-2">
          <span>Failed to update profile</span>
          <button onClick={() => toast.dismiss(t.id)} className="btn btn-ghost btn-xs btn-circle">✕</button>
        </div>
      ), { duration: 3000 });
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    setIsLoading(true);
    
    try {
      await userService.deleteCurrentUser();
      toast.success('Account deleted successfully');
      logout();
      navigate('/login');
    } catch (error) {
      toast.error((t) => (
        <div className="flex items-center gap-2">
          <span>Failed to delete account</span>
          <button onClick={() => toast.dismiss(t.id)} className="btn btn-ghost btn-xs btn-circle">✕</button>
        </div>
      ), { duration: 3000 });
      setIsLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-6 py-8">
      {/* Page Header */}
      <div className="mb-12 flex items-start justify-between">
        <div>
          <h1 className="text-5xl font-bold mb-2">Profile</h1>
          <div className="h-1 w-24 bg-primary"></div>
        </div>
        <div className="flex gap-3">
          <button 
            onClick={() => {
              setEditForm({ fullName: user.fullName || '', email: user.email });
              setIsEditModalOpen(true);
            }}
            className="btn btn-primary"
          >
            Edit Profile
          </button>
          <button 
            onClick={() => setIsDeleteModalOpen(true)}
            className="btn btn-error btn-outline"
          >
            Delete Account
          </button>
        </div>
      </div>

      {/* Avatar and Basic Info Section */}
      <div className="mb-16">
        <div className="flex items-center gap-8 pb-8 border-b-2 border-base-content/10">
          <div className="flex-shrink-0">
            <div className="w-32 h-32 rounded-full bg-primary/10 flex items-center justify-center">
              <span className="text-6xl font-bold text-primary">
                {user.username[0].toUpperCase()}
              </span>
            </div>
          </div>
          <div className="flex-1">
            <h2 className="text-4xl font-bold mb-2">{user.username}</h2>
            <p className="text-xl text-base-content/70 mb-4">{user.email}</p>
            <div className="flex gap-3">
              <span className="badge badge-primary badge-lg px-4 py-3 text-base">{user.role}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Account Details Section */}
      <div className="mb-12">
        <h3 className="text-3xl font-bold mb-8">Account Information</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-16 gap-y-10">
          <div className="space-y-2 pb-6 border-b border-base-content/10">
            <p className="text-sm font-semibold text-base-content/60 uppercase tracking-wider">Full Name</p>
            <p className="text-2xl font-semibold">{user.fullName || 'Not provided'}</p>
          </div>
          <div className="space-y-2 pb-6 border-b border-base-content/10">
            <p className="text-sm font-semibold text-base-content/60 uppercase tracking-wider">User ID</p>
            <p className="text-2xl font-mono">{user.id}</p>
          </div>
          <div className="space-y-2 pb-6 border-b border-base-content/10">
            <p className="text-sm font-semibold text-base-content/60 uppercase tracking-wider">Email Address</p>
            <p className="text-2xl">{user.email}</p>
          </div>
          <div className="space-y-2 pb-6 border-b border-base-content/10">
            <p className="text-sm font-semibold text-base-content/60 uppercase tracking-wider">Member Since</p>
            <p className="text-2xl">{new Date(user.createdAt).toLocaleDateString('en-US', { 
              year: 'numeric', 
              month: 'long', 
              day: 'numeric' 
            })}</p>
          </div>
        </div>
      </div>

      {/* Edit Profile Modal */}
      {isEditModalOpen && (
        <div className="modal modal-open">
          <div className="modal-box max-w-2xl">
            <div className="flex items-start justify-between mb-6">
              <div>
                <h3 className="font-bold text-3xl mb-2">Edit Profile</h3>
                <p className="text-base-content/70">Update your account information below</p>
              </div>
              <button
                onClick={() => setIsEditModalOpen(false)}
                className="btn btn-ghost btn-sm btn-circle"
                disabled={isLoading}
              >
                ✕
              </button>
            </div>
            
            <form onSubmit={handleEditSubmit}>
              <div className="space-y-6">
                {/* Avatar Preview */}
                <div className="flex items-center gap-6 pb-6 border-b border-base-content/10">
                  <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                    <span className="text-3xl font-bold text-primary">
                      {user.username[0].toUpperCase()}
                    </span>
                  </div>
                  <div>
                    <p className="text-lg font-bold">{user.username}</p>
                    <p className="text-sm text-base-content/60">Username cannot be changed</p>
                  </div>
                </div>

                {/* Full Name Field */}
                <div className="grid grid-cols-3 gap-4 items-start">
                  <div className="col-span-1 pt-3">
                    <label className="font-semibold text-base uppercase tracking-wider">Full Name</label>
                    <p className="text-xs text-base-content/60 mt-1">Your display name</p>
                  </div>
                  <div className="col-span-2">
                    <input
                      type="text"
                      value={editForm.fullName}
                      onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                      className="input input-bordered input-lg w-full"
                      placeholder="Enter your full name"
                    />
                  </div>
                </div>

                {/* Email Field */}
                <div className="grid grid-cols-3 gap-4 items-start">
                  <div className="col-span-1 pt-3">
                    <label className="font-semibold text-base uppercase tracking-wider">Email Address</label>
                    <p className="text-xs text-base-content/60 mt-1">For notifications</p>
                  </div>
                  <div className="col-span-2">
                    <input
                      type="email"
                      value={editForm.email}
                      onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                      className="input input-bordered input-lg w-full"
                      placeholder="Enter your email"
                      required
                    />
                  </div>
                </div>

                {/* Account Info (Read-only) */}
                <div className="bg-base-200 rounded-lg p-6 space-y-4">
                  <h4 className="font-semibold text-lg mb-4">Account Information</h4>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-xs font-semibold text-base-content/60 uppercase tracking-wider mb-1">User ID</p>
                      <p className="font-mono text-sm">{user.id}</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-base-content/60 uppercase tracking-wider mb-1">Role</p>
                      <span className="badge badge-primary">{user.role}</span>
                    </div>
                    <div className="col-span-2">
                      <p className="text-xs font-semibold text-base-content/60 uppercase tracking-wider mb-1">Member Since</p>
                      <p className="text-sm">{new Date(user.createdAt).toLocaleDateString('en-US', { 
                        year: 'numeric', 
                        month: 'long', 
                        day: 'numeric' 
                      })}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="modal-action mt-8">
                <button
                  type="button"
                  onClick={() => setIsEditModalOpen(false)}
                  className="btn btn-ghost btn-lg"
                  disabled={isLoading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-lg"
                  disabled={isLoading}
                >
                  {isLoading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
          <div className="modal-backdrop" onClick={() => setIsEditModalOpen(false)}></div>
        </div>
      )}

      {/* Delete Account Confirmation Modal */}
      {isDeleteModalOpen && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold text-2xl mb-4">Delete Account</h3>
            <p className="text-lg mb-6">
              Are you sure you want to delete your account? This action cannot be undone.
              All your data will be permanently removed.
            </p>
            <div className="modal-action">
              <button
                onClick={() => setIsDeleteModalOpen(false)}
                className="btn btn-ghost"
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteAccount}
                className="btn btn-error"
                disabled={isLoading}
              >
                {isLoading ? 'Deleting...' : 'Delete Account'}
              </button>
            </div>
          </div>
          <div className="modal-backdrop" onClick={() => setIsDeleteModalOpen(false)}></div>
        </div>
      )}
    </div>
  );
};
