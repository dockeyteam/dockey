import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { docGroupService, documentService } from '../services';
import { useDataCache } from '../context/DataCacheContext';
import { CommentableContent } from '../components';
import type { DocGroup, DocumentMetadata, DocumentResponse } from '../types';

export const DocGroupDetailPage: React.FC = () => {
  const { groupName, docSlug } = useParams<{ groupName: string; docSlug?: string }>();
  const navigate = useNavigate();
  const { groupDocuments, setGroupDocuments, setDocument, getDocument } = useDataCache();

  const [group, setGroup] = useState<DocGroup | null>(null);
  const [documentsMetadata, setDocumentsMetadata] = useState<DocumentMetadata[]>([]);
  const [selectedDoc, setSelectedDoc] = useState<DocumentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [docLoading, setDocLoading] = useState(false);
  const [hasLoadedDocs, setHasLoadedDocs] = useState(false);

  useEffect(() => {
    if (groupName) {
      loadGroupAndDocuments(groupName);
    }
  }, [groupName]);

  useEffect(() => {
    // Only run when documents are first loaded or slug changes
    if (!hasLoadedDocs || documentsMetadata.length === 0) return;

    if (docSlug) {
      // Find document by slug/title
      const doc = documentsMetadata.find(d =>
        d.title.toLowerCase().replace(/\s+/g, '-') === docSlug
      );
      if (doc) {
        loadDocument(doc.id);
      }
    } else {
      // Auto-select first document and load its content
      const firstDoc = documentsMetadata[0];
      loadDocument(firstDoc.id);
      const slug = firstDoc.title.toLowerCase().replace(/\s+/g, '-');
      navigate(`/docs/${groupName}/${slug}`, { replace: true });
    }
  }, [docSlug, hasLoadedDocs]);

  const loadGroupAndDocuments = async (name: string) => {
    try {
      setLoading(true);

      // Load group info
      const groupData = await docGroupService.getGroupByName(name);
      setGroup(groupData);

      // Check cache for documents
      const cachedDocs = groupDocuments.get(groupData.id);
      if (cachedDocs) {
        setDocumentsMetadata(cachedDocs);
        setHasLoadedDocs(true);
        setLoading(false);
        return;
      }

      // Load documents in group
      const docsData = await docGroupService.getDocumentsInGroup(groupData.id);
      setDocumentsMetadata(docsData);
      setGroupDocuments(groupData.id, docsData);
      setHasLoadedDocs(true);

    } catch (err: any) {
      toast.error(
        (t) => (
          <div className="flex items-center gap-2">
            <span>{err.response?.data?.message || 'Failed to load group'}</span>
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
      navigate('/documents');
    } finally {
      setLoading(false);
    }
  };

  const loadDocument = async (id: number) => {
    // Check cache first
    const cachedDoc = getDocument(id);
    if (cachedDoc) {
      setSelectedDoc(cachedDoc);
      return;
    }

    try {
      setDocLoading(true);
      const doc = await documentService.getDocumentById(id);
      setSelectedDoc(doc);

      // Add to cache
      setDocument(id, doc);
    } catch (err: any) {
      toast.error(
        (t) => (
          <div className="flex items-center gap-2">
            <span>{err.response?.data?.message || 'Failed to load document'}</span>
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
      setDocLoading(false);
    }
  };

  const handleDocumentSelect = (doc: DocumentMetadata) => {
    const slug = doc.title.toLowerCase().replace(/\s+/g, '-');
    navigate(`/docs/${groupName}/${slug}`);
  };

  return (
    <div className="flex h-[calc(100vh-4rem)]">
      {/* Sidebar */}
      <div className="w-80 bg-base-200/50 backdrop-blur-sm border-r border-base-content/10 overflow-y-auto hidden md:block topography-pattern relative">
        {/* Radial fade overlay */}
        <div
          className="absolute inset-0 pointer-events-none"
          style={{
            background: 'radial-gradient(circle at center, transparent 0%, transparent 40%, #1e1e1e 100%)',
          }}
        />
        <div className="p-6 relative z-10 min-h-full">
          {/* Back Button */}
          <Link
            to="/documents"
            className="btn btn-ghost btn-sm mb-6 hover:bg-base-content/5 transition-colors"
          >
            ← Back
          </Link>

          {loading ? (
            <>
              {/* Group Info Skeleton */}
              <div className="mb-6 p-4 rounded-xl bg-base-content/10 animate-pulse">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-12 h-12 bg-base-content/20 rounded-lg"></div>
                  <div className="flex-1">
                    <div className="h-5 bg-base-content/20 rounded w-3/4 mb-2"></div>
                    <div className="h-4 bg-base-content/20 rounded w-1/2"></div>
                  </div>
                </div>
                <div className="h-4 bg-base-content/20 rounded w-full"></div>
              </div>
              {/* Documents List Skeleton */}
              <div className="mb-4">
                <div className="h-4 bg-base-content/10 rounded w-32 mb-3"></div>
              </div>
              <div className="space-y-2">
                {[1, 2, 3, 4, 5].map((i) => (
                  <div key={i} className="h-16 bg-base-content/10 rounded-lg animate-pulse"></div>
                ))}
              </div>
            </>
          ) : (
            <>
              {/* Group Info */}
              {group && (
                <div className="mb-6 p-4 rounded-xl bg-base-200 border border-base-content/10">
                  <div className="flex items-center gap-3 mb-3">
                    <div className="w-12 h-12 bg-primary/20 rounded-lg flex items-center justify-center">
                      <span className="text-xl font-bold text-primary">
                        {group.displayName.charAt(0)}
                      </span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <h2 className="font-bold text-lg truncate">{group.displayName}</h2>
                      {group.technology && (
                        <div className="px-2 py-1 rounded bg-primary/10 text-primary text-xs font-medium mt-1 inline-block">
                          {group.technology}
                        </div>
                      )}
                    </div>
                  </div>
                  {group.description && (
                    <p className="text-sm text-base-content/70 leading-relaxed">{group.description}</p>
                  )}
                </div>
              )}

              {/* Documents List */}
              <div className="mb-4">
                <h3 className="font-bold text-sm text-base-content/60 uppercase tracking-wider mb-3">
                  Documents ({documentsMetadata.length})
                </h3>
              </div>

              {documentsMetadata.length === 0 ? (
                <div className="text-center py-8 text-base-content/40">
                  <p className="text-sm">No documents</p>
                </div>
              ) : (
                <ul className="space-y-2">
                  {documentsMetadata.map((doc) => {
                    const slug = doc.title.toLowerCase().replace(/\s+/g, '-');
                    const isActive = docSlug === slug;

                    return (
                      <li key={doc.id}>
                        <button
                          onClick={() => handleDocumentSelect(doc)}
                          className={`w-full text-left p-3 rounded-lg transition-all duration-200
                        ${isActive
                              ? 'bg-primary/40 border border-primary/50 shadow-sm'
                              : 'bg-base-200 hover:bg-base-300 border border-transparent'
                            }`}
                        >
                          <div className="flex flex-col gap-2">
                            <span className="text-sm font-medium line-clamp-2 text-base-content">
                              {doc.title}
                            </span>
                            {doc.status && (
                              <span className={`text-xs font-medium px-2 py-0.5 rounded inline-block w-fit ${doc.status === 'PUBLISHED'
                                  ? 'bg-success/10 text-success'
                                  : 'bg-warning/10 text-warning'
                                }`}>
                                {doc.status}
                              </span>
                            )}
                          </div>
                        </button>
                      </li>
                    );
                  })}
                </ul>
              )}
            </>
          )}
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-y-auto bg-base-100">
        <div className="min-h-full">
          {loading || docLoading ? (
            <div className="animate-pulse">
              <div className="mb-8">
                <div className="h-1 bg-primary mb-6"></div>
                <div className="px-8">
                  <div className="h-10 bg-base-content/10 rounded-lg w-2/3 mb-4"></div>
                  <div className="flex gap-3">
                    <div className="h-6 bg-base-content/10 rounded-lg w-20"></div>
                    <div className="h-6 bg-base-content/10 rounded-lg w-24"></div>
                  </div>
                </div>
              </div>
              <div className="px-8 pb-8">
                <div className="space-y-4">
                  <div className="h-4 bg-base-content/10 rounded w-full"></div>
                  <div className="h-4 bg-base-content/10 rounded w-5/6"></div>
                  <div className="h-4 bg-base-content/10 rounded w-4/5"></div>
                  <div className="h-4 bg-base-content/10 rounded w-full"></div>
                  <div className="h-4 bg-base-content/10 rounded w-3/4"></div>
                </div>
              </div>
            </div>
          ) : selectedDoc ? (
            <>
              {/* Header with primary accent bar */}
              <div className="mb-8">
                <div className="h-1 bg-primary mb-6"></div>
                <div className="px-8">
                  <h1 className="text-4xl font-bold mb-4">{selectedDoc.title}</h1>
                  <div className="flex flex-wrap gap-3">
                    {selectedDoc.source && (
                      <div className="px-3 py-1 rounded-lg bg-base-content/5 text-base-content/70 text-sm">
                        {selectedDoc.source}
                      </div>
                    )}
                    {selectedDoc.status && (
                      <div className={`px-3 py-1 rounded-lg text-sm font-medium ${selectedDoc.status === 'PUBLISHED'
                          ? 'bg-success/10 text-success'
                          : 'bg-warning/10 text-warning'
                        }`}>
                        {selectedDoc.status}
                      </div>
                    )}
                    {selectedDoc.createdAt && (
                      <div className="text-sm text-base-content/60 flex items-center">
                        {new Date(selectedDoc.createdAt).toLocaleDateString()}
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Content */}
              <div className="px-8 pb-8">
                <article className="prose prose-lg max-w-none">
                  {selectedDoc.content ? (
                    <CommentableContent
                      docId={selectedDoc.id.toString()}
                      content={selectedDoc.content}
                      initialLineCounts={selectedDoc.lineCommentCounts}
                    />
                  ) : (
                    <div className="text-center py-12 text-base-content/40">
                      <p>No content available</p>
                    </div>
                  )}
                </article>
              </div>
            </>
          ) : (
            <div className="text-center py-20 text-base-content/40">
              <div className="w-20 h-20 mx-auto mb-4 rounded-lg bg-base-content/5 flex items-center justify-center">
                <div className="w-12 h-16 border-4 border-base-content/20 rounded"></div>
              </div>
              <p>Select a document to view</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
