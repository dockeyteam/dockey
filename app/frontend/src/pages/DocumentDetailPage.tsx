import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { documentService } from '../services';
import type { DocumentResponse } from '../types';

export const DocumentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [document, setDocument] = useState<DocumentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    if (id) {
      loadDocument(parseInt(id));
    }
  }, [id]);

  const loadDocument = async (docId: number) => {
    try {
      setLoading(true);
      const doc = await documentService.getDocumentById(docId);
      setDocument(doc);
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load document';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const getCommentCountForLine = (lineNumber: number): number => {
    if (!document?.lineCommentCounts) return 0;
    return document.lineCommentCounts[lineNumber] || 0;
  };

  const renderContentWithLineNumbers = () => {
    if (!document) return null;

    const lines = document.content.split('\n');
    
    return (
      <div className="flex">
        {/* Line numbers with comment indicators */}
        <div className="bg-base-300 px-4 py-3 text-right select-none border-r border-base-content/10">
          {lines.map((_, index) => {
            const lineNumber = index + 1;
            const commentCount = getCommentCountForLine(lineNumber);
            
            return (
              <div
                key={lineNumber}
                className="flex items-center justify-end gap-2 font-mono text-sm leading-6 hover:bg-base-content/5"
              >
                {commentCount > 0 && (
                  <span className="badge badge-primary badge-xs">
                    {commentCount}
                  </span>
                )}
                <span className="text-base-content/50">{lineNumber}</span>
              </div>
            );
          })}
        </div>

        {/* Content */}
        <div className="flex-1 px-4 py-3 overflow-x-auto">
          <pre className="font-mono text-sm leading-6 whitespace-pre">
            {lines.map((line, index) => (
              <div key={index} className="hover:bg-base-content/5">
                {line || '\n'}
              </div>
            ))}
          </pre>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <span className="loading loading-spinner loading-lg"></span>
      </div>
    );
  }

  if (error || !document) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="alert alert-error">
          <span>{error || 'Document not found'}</span>
        </div>
        <Link to="/documents" className="btn btn-primary mt-4">
          Back to Documents
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-base-100">
      {/* Header */}
      <div className="bg-base-200 border-b border-base-300">
        <div className="container mx-auto px-4 py-6">
          <div className="flex items-center gap-4 mb-4">
            <Link to="/documents" className="btn btn-ghost btn-sm">
              ← Back
            </Link>
            <div className="flex-1">
              <h1 className="text-3xl font-bold">{document.title}</h1>
              {document.source && (
                <p className="text-sm text-base-content/70 mt-1">
                  Source: {document.source}
                </p>
              )}
            </div>
            <div className="badge badge-outline badge-lg">{document.status}</div>
          </div>

          {/* Document metadata */}
          <div className="flex gap-4 text-sm text-base-content/70">
            {document.groupDisplayName && (
              <div className="flex items-center gap-2">
                <span className="font-semibold">Group:</span>
                <span className="badge badge-primary">{document.groupDisplayName}</span>
              </div>
            )}
            <div>
              <span className="font-semibold">Created:</span>{' '}
              {new Date(document.createdAt).toLocaleString()}
            </div>
            {document.updatedAt && (
              <div>
                <span className="font-semibold">Updated:</span>{' '}
                {new Date(document.updatedAt).toLocaleString()}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="container mx-auto px-4 py-6">
        <div className="card bg-base-200 shadow-xl overflow-hidden">
          <div className="card-body p-0">
            {renderContentWithLineNumbers()}
          </div>
        </div>

        {/* Comment indicator info */}
        {document.lineCommentCounts && Object.keys(document.lineCommentCounts).length > 0 && (
          <div className="alert alert-info mt-4">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="stroke-current shrink-0 w-6 h-6">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <span>
              Numbers next to line numbers indicate comment counts. 
              Click on a line to view or add comments (feature coming soon).
            </span>
          </div>
        )}
      </div>
    </div>
  );
};
