import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { docGroupService } from '../services';
import type { DocGroup } from '../types';

export const DocumentsPage: React.FC = () => {
  const [groups, setGroups] = useState<DocGroup[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadGroups();
  }, []);

  const loadGroups = async () => {
    try {
      setLoading(true);
      const groupsData = await docGroupService.getAllGroups();
      setGroups(groupsData);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load document groups');
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

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <span className="loading loading-spinner loading-lg"></span>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-6 py-8">
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-4">Documentation Groups</h1>
        <p className="text-base-content/70 mb-6">
          Browse documentation by technology or framework
        </p>

        {/* Search Bar */}
        <div className="form-control max-w-md">
          <input
            type="text"
            placeholder="Search groups..."
            className="input input-bordered w-full"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {error && (
        <div className="alert alert-error mb-6">
          <span>{error}</span>
        </div>
      )}

      {filteredGroups.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-lg text-base-content/70">
            {searchQuery ? 'No groups match your search' : 'No documentation groups available yet'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredGroups.map((group) => (
            <Link
              key={group.id}
              to={`/docs/${group.name}`}
              className="card bg-base-100 shadow-xl hover:shadow-2xl transition-all hover:-translate-y-1"
            >
              <figure className="h-32 bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
                {group.icon ? (
                  <span className="text-5xl">{group.icon}</span>
                ) : (
                  <span className="text-4xl font-bold text-primary-content">
                    {group.displayName.charAt(0)}
                  </span>
                )}
              </figure>
              <div className="card-body">
                <h2 className="card-title">
                  {group.displayName}
                  {group.documentCount !== undefined && group.documentCount > 0 && (
                    <div className="badge badge-secondary">{group.documentCount}</div>
                  )}
                </h2>
                {group.technology && (
                  <div className="badge badge-outline badge-sm">{group.technology}</div>
                )}
                {group.description && (
                  <p className="text-sm text-base-content/70 line-clamp-2">
                    {group.description}
                  </p>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};
