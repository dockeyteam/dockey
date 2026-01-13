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
        <div className="bg-base-300/30 px-6 py-4 text-right select-none border-r border-base-content/10">
          {lines.map((_, index) => {
            const lineNumber = index + 1;
            const commentCount = getCommentCountForLine(lineNumber);
            
            return (
              <div
                key={lineNumber}
                className="flex items-center justify-end gap-3 font-mono text-sm leading-6 
                           hover:bg-primary/5 transition-colors rounded px-2 -mx-2"
              >
                {commentCount > 0 && (
                  <span className="px-2 py-0.5 rounded bg-primary/20 text-primary text-xs font-semibold">
                    {commentCount}
                  </span>
                )}
                <span className="text-base-content/40 min-w-[3ch]">{lineNumber}</span>
              </div>
            );
          })}
        </div>

        {/* Content */}
        <div className="flex-1 px-6 py-4 overflow-x-auto">
          <pre className="font-mono text-sm leading-6 whitespace-pre text-base-content/90">
            {lines.map((line, index) => (
              <div 
                key={index} 
                className="hover:bg-primary/5 transition-colors rounded px-2 -mx-2"
              >
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
      <div className="min-h-screen animate-pulse">
        {/* Header Skeleton */}
        <div className="mb-8">
          <div className="h-1 bg-primary mb-6"></div>
          <div className="px-8">
            <div className="flex items-center gap-4 mb-6">
              <div className="h-8 bg-base-content/10 rounded-lg w-20"></div>
              <div className="flex-1">
                <div className="h-10 bg-base-content/10 rounded-lg w-2/3 mb-2"></div>
                <div className="h-5 bg-base-content/10 rounded-lg w-1/3"></div>
              </div>
              <div className="h-8 bg-base-content/10 rounded-lg w-24"></div>
            </div>
            <div className="flex gap-6">
              <div className="h-5 bg-base-content/10 rounded-lg w-32"></div>
              <div className="h-5 bg-base-content/10 rounded-lg w-40"></div>
              <div className="h-5 bg-base-content/10 rounded-lg w-40"></div>
            </div>
          </div>
        </div>

        {/* Content Skeleton */}
        <div className="px-8 pb-8">
          <div className="rounded-xl border border-base-content/10 p-6">
            <div className="space-y-3">
              {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((i) => (
                <div key={i} className="h-4 bg-base-content/10 rounded" style={{ width: `${Math.random() * 30 + 70}%` }}></div>
              ))}
            </div>
          </div>
        </div>
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
    <div className="min-h-screen">
      {/* Header with primary accent bar */}
      <div className="mb-8">
        <div className="h-1 bg-primary mb-6"></div>
        <div className="px-8">
          <div className="flex items-center gap-4 mb-6">
            <Link 
              to="/documents" 
              className="btn btn-ghost btn-sm hover:bg-base-content/5 transition-colors"
            >
              ← Back
            </Link>
            <div className="flex-1">
              <h1 className="text-4xl font-bold mb-2">{document.title}</h1>
              {document.source && (
                <p className="text-base-content/60">
                  Source: {document.source}
                </p>
              )}
            </div>
            <div className="px-4 py-2 rounded-lg bg-primary/10 text-primary font-medium">
              {document.status}
            </div>
          </div>

          {/* Document metadata */}
          <div className="flex gap-6 text-sm text-base-content/60">
            {document.groupDisplayName && (
              <div className="flex items-center gap-2">
                <span className="font-semibold text-base-content/80">Group:</span>
                <span className="px-3 py-1 rounded-lg bg-primary/10 text-primary">
                  {document.groupDisplayName}
                </span>
              </div>
            )}
            <div>
              <span className="font-semibold text-base-content/80">Created:</span>{' '}
              {new Date(document.createdAt).toLocaleString()}
            </div>
            {document.updatedAt && (
              <div>
                <span className="font-semibold text-base-content/80">Updated:</span>{' '}
                {new Date(document.updatedAt).toLocaleString()}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="px-8 pb-8">
        <div 
          className="rounded-xl bg-base-200/50 border border-base-content/10 overflow-hidden backdrop-blur-sm
                     hover:shadow-lg hover:border-base-content/20 transition-all duration-200"
        >
          {renderContentWithLineNumbers()}
        </div>

        {/* Comment indicator info */}
        {document.lineCommentCounts && Object.keys(document.lineCommentCounts).length > 0 && (
          <div className="mt-6 p-4 rounded-lg bg-info/10 border border-info/20 text-info">
            <div className="flex items-start gap-3">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="w-5 h-5 mt-0.5 shrink-0">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" stroke="currentColor" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              <span className="text-sm">
                Numbers next to line numbers indicate comment counts. 
                Click on a line to view or add comments (feature coming soon).
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
