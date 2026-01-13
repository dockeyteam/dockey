import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { docGroupService } from '../services';
import { useDataCache } from '../context/DataCacheContext';
import type { DocGroup } from '../types';

export const DocumentsPage: React.FC = () => {
  const { docGroups, setDocGroups } = useDataCache();
  const [groups, setGroups] = useState<DocGroup[]>(docGroups || []);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(!docGroups);

  useEffect(() => {
    if (!docGroups) {
      loadGroups();
    }
  }, []);

  const loadGroups = async () => {
    try {
      setLoading(true);
      const groupsData = await docGroupService.getAllGroups();
      setGroups(groupsData);
      setDocGroups(groupsData);
    } catch (err: any) {
      toast.error(
        (t) => (
          <div className="flex items-center gap-2">
            <span>{err.response?.data?.message || 'Failed to load document groups'}</span>
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
    } finally {
      setLoading(false);
    }
  };

  const filteredGroups = groups.filter(
    (group) =>
      group.displayName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      group.technology?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      group.description?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="min-h-screen">
      <div className="container mx-auto px-6 py-12 max-w-7xl">
        {/* Header */}
        <div className="mb-12">
          <div className="h-1 bg-primary mb-8"></div>
          <div className="flex items-start justify-between mb-4">
            <div>
              <h1 className="text-4xl font-bold mb-2">Document Groups</h1>
              <p className="text-lg text-base-content/70">
                Browse and manage your documentation collections
              </p>
            </div>
            <button 
              onClick={() => loadGroups()}
              className="btn btn-sm btn-outline"
              disabled={loading}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Refresh
            </button>
          </div>
        </div>

        {/* Search Bar */}
        <div className="mb-8">
          <input
            type="text"
            placeholder="Search document groups..."
            className="input input-bordered w-full"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        {/* Cards */}
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="rounded-xl border border-base-content/10 p-6 animate-pulse">
                <div className="flex items-start gap-4 mb-4">
                  <div className="w-16 h-16 bg-base-content/10 rounded-lg"></div>
                  <div className="flex-1">
                    <div className="h-6 bg-base-content/10 rounded w-3/4 mb-2"></div>
                    <div className="h-4 bg-base-content/10 rounded w-1/2"></div>
                  </div>
                </div>
                <div className="h-4 bg-base-content/10 rounded w-full mb-2"></div>
                <div className="h-4 bg-base-content/10 rounded w-5/6"></div>
              </div>
            ))}
          </div>
        ) : filteredGroups.length === 0 ? (
          <div className="text-center py-20">
            <div className="w-20 h-20 mx-auto mb-4 rounded-full bg-base-300 flex items-center justify-center">
              <div className="w-12 h-12 border-4 border-base-content/20 rounded"></div>
            </div>
            <p className="text-lg text-base-content/60">
              {searchQuery ? 'No groups match your search' : 'No documentation groups available'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredGroups.map((group) => (
              <Link
                key={group.id}
                to={`/docs/${group.name}`}
                className="card bg-base-100 border border-base-300 hover:shadow-lg hover:-translate-y-1 transition-all duration-200 overflow-hidden"
              >
                <div className="h-32 bg-base-200 flex items-center justify-center relative overflow-hidden">
                  {/* SVG Background Pattern */}
                  <svg className="absolute inset-0 w-full h-full opacity-5" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                      <pattern id={`pattern-${group.id}`} x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
                        <circle cx="20" cy="20" r="2" fill="currentColor" />
                      </pattern>
                    </defs>
                    <rect width="100%" height="100%" fill={`url(#pattern-${group.id})`} />
                  </svg>
                  
                  <div className="w-16 h-16 rounded-lg bg-primary/10 flex items-center justify-center relative z-10">
                    <span className="text-2xl font-bold text-primary">
                      {group.displayName.charAt(0)}
                    </span>
                  </div>
                </div>
                <div className="card-body p-4">
                  <div className="flex items-start justify-between gap-2 pb-3 mb-3 border-b-2 border-base-content/10">
                    <h2 className="font-bold text-lg line-clamp-2">{group.displayName}</h2>
                    {group.documentCount !== undefined && group.documentCount > 0 && (
                      <div className="flex items-center justify-center w-6 h-6 rounded-full bg-primary text-primary-content text-xs font-bold flex-shrink-0">
                        {group.documentCount}
                      </div>
                    )}
                  </div>
                  {group.technology && (
                    <div className="badge badge-outline badge-sm mb-3">{group.technology}</div>
                  )}
                  {group.description && (
                    <p className="text-sm text-base-content/60 line-clamp-2">
                      {group.description}
                    </p>
                  )}
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
