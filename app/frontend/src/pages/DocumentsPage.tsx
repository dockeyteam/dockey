import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { documentService } from '../services';
import type { Document } from '../types';

export const DocumentsPage: React.FC = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      const docs = await documentService.getAllDocuments();
      setDocuments(docs);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <span className="loading loading-spinner loading-lg"></span>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Documents</h1>
        <Link to="/documents/new" className="btn btn-primary">
          + New Document
        </Link>
      </div>

      {error && (
        <div className="alert alert-error mb-4">
          <span>{error}</span>
        </div>
      )}

      {documents.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-lg text-base-content/70">No documents found</p>
          <Link to="/documents/new" className="btn btn-primary mt-4">
            Create your first document
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {documents.map((doc) => (
            <Link
              key={doc.id}
              to={`/documents/${doc.id}`}
              className="card bg-base-100 shadow-xl hover:shadow-2xl transition-shadow"
            >
              <div className="card-body">
                <h2 className="card-title">{doc.title}</h2>
                <p className="text-sm text-base-content/70 line-clamp-3">
                  {doc.content.substring(0, 150)}...
                </p>
                <div className="card-actions justify-between items-center mt-4">
                  <div className="badge badge-outline">{doc.status}</div>
                  <span className="text-xs text-base-content/50">
                    {new Date(doc.createdAt).toLocaleDateString()}
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};
