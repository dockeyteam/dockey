import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="hero min-h-screen bg-base-200">
      <div className="hero-content text-center">
        <div className="max-w-md">
          <h1 className="text-5xl font-bold">Welcome to Dockey 📚</h1>
          <p className="py-6">
            A collaborative document platform where you can create, share, and comment on documents
            with line-by-line precision.
          </p>
          {isAuthenticated ? (
            <Link to="/documents" className="btn btn-primary">
              View Documents
            </Link>
          ) : (
            <div className="flex gap-2 justify-center">
              <Link to="/login" className="btn btn-primary">
                Get Started
              </Link>
              <Link to="/register" className="btn btn-outline">
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
