import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import type { LoginRequest } from '../types';

export const LoginPage: React.FC = () => {
  const [formData, setFormData] = useState<LoginRequest>({
    username: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await login(formData);
      navigate('/documents');
      toast.success(
        (t) => (
          <div className="flex items-center gap-2">
            <span>Welcome back!</span>
            <button
              onClick={() => toast.dismiss(t.id)}
              className="btn btn-ghost btn-xs btn-circle"
            >
              ✕
            </button>
          </div>
        ),
        { duration: 3000 }
      );
    } catch (err: any) {
      toast.error(
        (t) => (
          <div className="flex items-center gap-2">
            <span>{err.response?.data?.message || 'Login failed. Please try again.'}</span>
            <button
              onClick={() => toast.dismiss(t.id)}
              className="btn btn-ghost btn-xs btn-circle"
            >
              ✕
            </button>
          </div>
        ),
        { duration: 4000 }
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold mb-2">Welcome Back</h1>
          <p className="text-base-content/70">Sign in to your Dockey account</p>
        </div>

        {/* Form Card */}
        <div className="rounded-2xl bg-base-100 border border-base-content/10 p-8 shadow-lg">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-semibold mb-2">
                Username
              </label>
              <input
                type="text"
                name="username"
                placeholder="Enter your username"
                className="input input-bordered w-full"
                value={formData.username}
                onChange={handleChange}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold mb-2">
                Password
              </label>
              <input
                type="password"
                name="password"
                placeholder="Enter your password"
                className="input input-bordered w-full"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>

            <button 
              type="submit" 
              className="btn btn-primary w-full"
              disabled={loading}
            >
              {loading ? <span className="loading loading-spinner"></span> : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-base-content/10 text-center">
            <p className="text-sm text-base-content/70">
              Don't have an account?{' '}
              <Link to="/register" className="font-semibold text-primary hover:underline">
                Create one now
              </Link>
            </p>
          </div>
        </div>

        {/* Back to Documents */}
        <div className="text-center mt-6">
          <Link to="/documents" className="text-sm text-base-content/60 hover:text-base-content transition-colors">
            ← Back to Documents
          </Link>
        </div>
      </div>
    </div>
  );
};
