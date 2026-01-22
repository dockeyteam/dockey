import React, { useState } from 'react';
import logo from '../assets/logo.png';
import { useNavigate, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import type { RegisterRequest } from '../types';

export const RegisterPage: React.FC = () => {
  const [formData, setFormData] = useState<RegisterRequest>({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
  });
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
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
      await register(formData);
      navigate('/documents');
      toast.success(
        (t) => (
          <div className="flex items-center gap-2">
            <span>Account created successfully!</span>
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
            <span>{err.response?.data?.message || 'Registration failed. Please try again.'}</span>
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
    <div className="min-h-screen flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8 flex flex-col items-center">
          <img src={logo} alt="Dockey Logo" className="h-14 w-14 mb-2 rounded" />
          <h1 className="text-4xl font-bold mb-2">Create Account</h1>
          <p className="text-base-content/70">Join Dockey to unlock more features</p>
        </div>

        {/* Form Card */}
        <div className="rounded-2xl bg-base-100 border border-base-content/10 p-8 shadow-lg">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-semibold mb-2">
                Username <span className="text-error">*</span>
              </label>
              <input
                type="text"
                name="username"
                placeholder="Choose a username"
                className="input input-bordered w-full"
                value={formData.username}
                onChange={handleChange}
                minLength={3}
                maxLength={50}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold mb-2">
                Email <span className="text-error">*</span>
              </label>
              <input
                type="email"
                name="email"
                placeholder="your.email@example.com"
                className="input input-bordered w-full"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold mb-2">
                Password <span className="text-error">*</span>
              </label>
              <input
                type="password"
                name="password"
                placeholder="Minimum 6 characters"
                className="input input-bordered w-full"
                value={formData.password}
                onChange={handleChange}
                minLength={6}
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold mb-2">
                  First Name
                </label>
                <input
                  type="text"
                  name="firstName"
                  placeholder="First name"
                  className="input input-bordered w-full"
                  value={formData.firstName}
                  onChange={handleChange}
                />
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">
                  Last Name
                </label>
                <input
                  type="text"
                  name="lastName"
                  placeholder="Last name"
                  className="input input-bordered w-full"
                  value={formData.lastName}
                  onChange={handleChange}
                />
              </div>
            </div>

            <button 
              type="submit" 
              className="btn btn-primary w-full mt-6"
              disabled={loading}
            >
              {loading ? <span className="loading loading-spinner"></span> : 'Create Account'}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-base-content/10 text-center">
            <p className="text-sm text-base-content/70">
              Already have an account?{' '}
              <Link to="/login" className="font-semibold text-primary hover:underline">
                Sign in
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
