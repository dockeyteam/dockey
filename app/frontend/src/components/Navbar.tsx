import React from 'react';
import logo from '../assets/logo.png';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <div className="navbar bg-base-100 border-b-2 border-base-300 sticky top-0 z-50 px-6 h-20">
      <div className="flex-1">
        <Link to="/documents" className="flex items-center gap-2 text-2xl font-bold tracking-tight hover:opacity-80 transition-opacity">
          <img src={logo} alt="Dockey Logo" className="h-8 w-8 rounded" />
          Dockey
        </Link>
      </div>
      <div className="flex-none">
        {isAuthenticated ? (
          <div className="flex items-center gap-6">
            <ul className="menu menu-horizontal p-0 hidden md:flex">
              <li>
                <Link 
                  to="/documents" 
                  className={`font-medium text-base ${isActive('/documents') ? 'text-primary' : ''}`}
                >
                  Documents
                </Link>
              </li>
            </ul>
            <div className="dropdown dropdown-end">
              <div tabIndex={0} role="button" className="btn btn-ghost btn-circle avatar">
                <div className="w-10 rounded-full bg-primary text-primary-content flex items-center justify-center font-semibold">
                  {user?.username?.[0]?.toUpperCase() || 'U'}
                </div>
              </div>
              <ul
                tabIndex={0}
                className="menu menu-sm dropdown-content mt-3 z-[1] shadow-lg bg-base-100 rounded-box w-48 border border-base-300"
              >
                <div className="py-2 mb-2 border-b border-base-content/10">
                  <div className="font-semibold px-4">{user?.username}</div>
                  <div className="text-xs opacity-60 px-4">{user?.role}</div>
                </div>
                <li><Link to="/profile">Profile</Link></li>
                <li><Link to="/users">Users</Link></li>
                <li className="md:hidden"><Link to="/documents">Documents</Link></li>
                <div className="border-b-2 border-base-content/10 my-2"></div>
                <li><a onClick={handleLogout} className="text-error">Logout</a></li>
              </ul>
            </div>
          </div>
        ) : (
          <div className="flex gap-2">
            <Link to="/login" className="btn btn-ghost">
              Login
            </Link>
            <Link to="/register" className="btn btn-primary">
              Register
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
