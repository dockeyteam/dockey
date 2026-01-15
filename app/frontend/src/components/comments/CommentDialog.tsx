import React, { useEffect, useState, useRef } from 'react';
import toast from 'react-hot-toast';
import { CommentItem } from './CommentItem';
import { AddCommentForm } from './AddCommentForm';
import { commentService } from '../../services';
import { useAuth } from '../../context/AuthContext';
import type { Comment } from '../../types';

interface CommentDialogProps {
  isOpen: boolean;
  onClose: () => void;
  docId: string;
  lineNumber: number;
  lineContent?: string;
  initialComments?: Comment[];
  onCommentAdded?: () => void;
  onCommentDeleted?: () => void;
  position?: { x: number; y: number };
}

export const CommentDialog: React.FC<CommentDialogProps> = ({
  isOpen,
  onClose,
  docId,
  lineNumber,
  lineContent,
  initialComments,
  onCommentAdded,
  onCommentDeleted,
}) => {
  const { user } = useAuth();
  const [comments, setComments] = useState<Comment[]>(initialComments || []);
  const [isLoading, setIsLoading] = useState(!initialComments);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const dialogRef = useRef<HTMLDivElement>(null);

  const loadComments = async () => {
    setIsLoading(true);
    try {
      const data = await commentService.getCommentsByLine(docId, lineNumber, user?.keycloakId);
      setComments(data);
    } catch (error) {
      console.error('Failed to load comments:', error);
      toast.error('Failed to load comments');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && !initialComments) {
      loadComments();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, docId, lineNumber, initialComments]);

  useEffect(() => {
    if (initialComments) {
      setComments(initialComments);
    }
  }, [initialComments]);

  // Close on escape key
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (isFullscreen) {
          setIsFullscreen(false);
        } else {
          onClose();
        }
      }
    };
    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
    }
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, isFullscreen, onClose]);

  const handleAddComment = async (content: string) => {
    try {
      const newComment = await commentService.createComment({
        docId,
        lineNumber,
        content,
      });
      setComments(prev => [...prev, newComment]);
      toast.success('Comment added');
      onCommentAdded?.();
    } catch (error) {
      toast.error('Failed to add comment');
      throw error;
    }
  };

  const handleLike = async (commentId: string) => {
    await commentService.likeComment(commentId);
  };

  const handleUnlike = async (commentId: string) => {
    await commentService.unlikeComment(commentId);
  };

  const handleDelete = async (commentId: string) => {
    try {
      await commentService.deleteComment(commentId);
      setComments(prev => prev.filter(c => c.id !== commentId));
      toast.success('Comment deleted');
      onCommentDeleted?.();
    } catch {
      toast.error('Failed to delete comment');
    }
  };

  if (!isOpen) return null;

  // Fullscreen modal version
  if (isFullscreen) {
    return (
      <>
        {/* Fullscreen backdrop with blur */}
        <div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40" onClick={() => setIsFullscreen(false)} />

        {/* Fullscreen dialog */}
        <div
          ref={dialogRef}
          className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl max-h-[85vh] bg-base-100 rounded-xl shadow-2xl border border-base-content/10 z-50 overflow-hidden flex flex-col"
        >
          {/* Header */}
          <div className="px-5 py-4 border-b border-base-content/10 bg-base-200/50 shrink-0">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                  </svg>
                </div>
                <div>
                  <span className="font-semibold">
                    Line {lineNumber} Comments
                    {comments.length > 0 && (
                      <span className="ml-2 px-2 py-0.5 rounded-full bg-primary/20 text-primary text-sm">
                        {comments.length}
                      </span>
                    )}
                  </span>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setIsFullscreen(false)}
                  className="btn btn-ghost btn-sm"
                  title="Minimize"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 14h6m0 0v6m0-6L3 21M20 10h-6m0 0V4m0 6l7-7" />
                  </svg>
                </button>
                <button
                  onClick={onClose}
                  className="btn btn-ghost btn-sm btn-circle"
                  title="Close"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            {/* Line content preview */}
            {lineContent && (
              <div className="mt-3 p-3 rounded-lg bg-base-300/50 text-sm font-mono text-base-content/70 overflow-x-auto">
                {lineContent}
              </div>
            )}
          </div>

          {/* Comments list */}
          <div className="flex-1 overflow-y-auto p-5 space-y-4">
            {isLoading ? (
              <div className="flex justify-center py-12">
                <span className="loading loading-spinner loading-lg text-primary"></span>
              </div>
            ) : comments.length === 0 ? (
              <div className="text-center py-12 text-base-content/50">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mx-auto mb-3 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                <p className="text-lg font-medium">No comments yet</p>
                <p className="text-sm mt-1">Be the first to comment on this line!</p>
              </div>
            ) : (
              comments.map(comment => (
                <CommentItem
                  key={comment.id}
                  comment={comment}
                  onLike={handleLike}
                  onUnlike={handleUnlike}
                  onDelete={handleDelete}
                />
              ))
            )}
          </div>

          {/* Add comment form */}
          <div className="p-5 border-t border-base-content/10 bg-base-200/30 shrink-0">
            <AddCommentForm
              onSubmit={handleAddComment}
              placeholder="Write a comment..."
              autoFocus={true}
            />
          </div>
        </div>
      </>
    );
  }

  // Inline (non-fullscreen) version - appears below the line
  return (
    <div
      ref={dialogRef}
      className="w-full bg-base-100 rounded-xl shadow-lg border border-base-content/10 overflow-hidden mt-2 mb-3 animate-in slide-in-from-top-2 duration-200"
    >
      {/* Compact header */}
      <div className="px-4 py-2.5 border-b border-base-content/10 bg-base-200/30">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
            </svg>
            <span className="text-sm font-medium">
              Line {lineNumber}
              {comments.length > 0 && (
                <span className="ml-1.5 px-1.5 py-0.5 rounded-full bg-primary/20 text-primary text-xs">
                  {comments.length}
                </span>
              )}
            </span>
          </div>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setIsFullscreen(true)}
              className="btn btn-ghost btn-xs"
              title="Open fullscreen"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5v-4m0 4h-4m4 0l-5-5" />
              </svg>
            </button>
            <button
              onClick={onClose}
              className="btn btn-ghost btn-xs btn-circle"
              title="Close"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Comments list - compact height */}
      <div className="max-h-48 overflow-y-auto p-3 space-y-2">
        {isLoading ? (
          <div className="flex justify-center py-4">
            <span className="loading loading-spinner loading-sm text-primary"></span>
          </div>
        ) : comments.length === 0 ? (
          <div className="text-center py-3 text-base-content/50">
            <p className="text-xs">No comments yet. Be the first!</p>
          </div>
        ) : (
          comments.map(comment => (
            <CommentItem
              key={comment.id}
              comment={comment}
              onLike={handleLike}
              onUnlike={handleUnlike}
              onDelete={handleDelete}
            />
          ))
        )}
      </div>

      {/* Add comment form - compact */}
      <div className="p-3 border-t border-base-content/10 bg-base-200/20">
        <AddCommentForm
          onSubmit={handleAddComment}
          placeholder="Write a comment..."
          autoFocus={comments.length === 0}
        />
      </div>
    </div>
  );
};
