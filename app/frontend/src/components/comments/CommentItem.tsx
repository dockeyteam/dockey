import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import type { Comment } from '../../types';
import { useAuth } from '../../context/AuthContext';

interface CommentItemProps {
  comment: Comment;
  onLike: (commentId: string) => Promise<void>;
  onUnlike: (commentId: string) => Promise<void>;
  onDelete?: (commentId: string) => Promise<void>;
}

export const CommentItem: React.FC<CommentItemProps> = ({
  comment,
  onLike,
  onUnlike,
  onDelete,
}) => {
  const { user } = useAuth();
  const [isLiking, setIsLiking] = useState(false);
  const [localLikeCount, setLocalLikeCount] = useState(comment.likeCount || 0);
  const [localLiked, setLocalLiked] = useState(comment.likedByCurrentUser || false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const isOwner = user?.keycloakId === comment.userId;

  const handleLikeToggle = async () => {
    if (isLiking) return;
    setIsLiking(true);
    
    try {
      if (localLiked) {
        await onUnlike(comment.id);
        setLocalLikeCount(prev => Math.max(0, prev - 1));
        setLocalLiked(false);
      } else {
        await onLike(comment.id);
        setLocalLikeCount(prev => prev + 1);
        setLocalLiked(true);
      }
    } catch (error) {
      console.error('Failed to toggle like:', error);
    } finally {
      setIsLiking(false);
    }
  };

  const handleDelete = async () => {
    if (!onDelete || isDeleting) return;
    setIsDeleting(true);
    try {
      await onDelete(comment.id);
    } catch (error) {
      console.error('Failed to delete comment:', error);
    } finally {
      setIsDeleting(false);
      setShowDeleteConfirm(false);
    }
  };

  // Parse date from either ISO string or LocalDateTime array [year, month, day, hour, minute, second, nano]
  // Server sends dates in UTC, so we need to parse them as UTC and then display in local time
  const parseDate = (dateValue: string | number[] | undefined | null): Date | null => {
    if (!dateValue) return null;
    
    try {
      // Handle array format from Java LocalDateTime: [year, month, day, hour, minute, second, nano]
      // These are UTC values from the server
      if (Array.isArray(dateValue)) {
        const [year, month, day, hour = 0, minute = 0, second = 0] = dateValue;
        // Month is 1-indexed in Java, but 0-indexed in JS Date
        // Use Date.UTC to create the date in UTC timezone
        return new Date(Date.UTC(year, month - 1, day, hour, minute, second));
      }
      
      // Handle ISO string format (already includes timezone info)
      if (typeof dateValue === 'string') {
        const date = new Date(dateValue);
        if (!isNaN(date.getTime())) return date;
      }
      
      return null;
    } catch {
      return null;
    }
  };
  const formatDate = (dateValue: string | number[] | undefined | null): string => {
    const date = parseDate(dateValue);
    if (!date) return '';
    
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffSecs = Math.floor(diffMs / 1000);
    const diffMins = Math.floor(diffSecs / 60);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffSecs < 30) return 'just now';
    if (diffSecs < 60) return `${diffSecs}s ago`;
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  };

  const formatFullDate = (dateValue: string | number[] | undefined | null): string => {
    const date = parseDate(dateValue);
    if (!date) return '';
    return date.toLocaleString(undefined, { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const createdDate = parseDate(comment.createdAt);
  const updatedDate = parseDate(comment.updatedAt);
  const isEdited = createdDate && updatedDate && 
    Math.abs(updatedDate.getTime() - createdDate.getTime()) > 1000; // More than 1 second difference

  const dateStr = formatDate(comment.createdAt);
  const fullDateStr = formatFullDate(comment.createdAt);

  return (
    <div className="group flex gap-2 py-1.5 hover:bg-base-200/50 rounded px-1 -mx-1 transition-colors">
      {/* Compact avatar - links to user profile */}
      <Link 
        to={`/profile/${comment.userId}`}
        className="shrink-0 w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center text-xs font-semibold text-primary hover:bg-primary/30 transition-colors"
        title={`View ${comment.userName || 'user'}'s profile`}
      >
        {comment.userName?.charAt(0)?.toUpperCase() || '?'}
      </Link>
      
      {/* Content */}
      <div className="flex-1 min-w-0">
        {/* Header line: name, time, actions */}
        <div className="flex items-center gap-2 text-xs">
          <Link 
            to={`/profile/${comment.userId}`}
            className="font-medium text-base-content hover:text-primary truncate transition-colors" 
            title={`View ${comment.userName}'s profile`}
          >
            {comment.userName || 'Unknown'}
          </Link>
          {dateStr && (
            <span 
              className="text-base-content/40 shrink-0 cursor-default"
              title={fullDateStr + (isEdited ? ' (edited)' : '')}
            >
              {dateStr}{isEdited && ' •edited'}
            </span>
          )}

          {/* Like count - always visible when there are likes */}
          {localLikeCount > 0 && (
            <span 
              className={`flex items-center gap-0.5 text-xs ${localLiked ? 'text-error' : 'text-base-content/50'}`}
              title={`${localLikeCount} ${localLikeCount === 1 ? 'like' : 'likes'}`}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-3 w-3"
                fill={localLiked ? 'currentColor' : 'none'}
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                />
              </svg>
              {localLikeCount}
            </span>
          )}
          
          {/* Actions - show on hover */}
          <div className="ml-auto flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              onClick={handleLikeToggle}
              disabled={isLiking}
              className={`p-1 rounded text-xs transition-colors ${
                localLiked ? 'text-error' : 'text-base-content/40 hover:text-error hover:bg-error/10'
              }`}
              title={localLiked ? 'Unlike this comment' : 'Like this comment'}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-3.5 w-3.5"
                fill={localLiked ? 'currentColor' : 'none'}
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                />
              </svg>
            </button>
            
            {isOwner && onDelete && (
              <button
                onClick={() => setShowDeleteConfirm(true)}
                className="p-1 rounded text-base-content/40 hover:text-error hover:bg-error/10 transition-colors"
                title="Delete comment"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            )}
          </div>
        </div>
        
        {/* Comment text */}
        <p className="text-sm text-base-content/80 mt-0.5 whitespace-pre-wrap break-words">
          {comment.content}
        </p>
      </div>

      {/* Delete confirmation modal */}
      {showDeleteConfirm && (
        <>
          {/* Backdrop */}
          <div 
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50"
            onClick={() => setShowDeleteConfirm(false)}
          />
          {/* Modal */}
          <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50 bg-base-100 rounded-xl shadow-2xl border border-base-content/10 p-6 w-full max-w-sm">
            <h3 className="text-lg font-semibold mb-2">Delete Comment</h3>
            <p className="text-sm text-base-content/70 mb-4">
              Are you sure you want to delete this comment? This action cannot be undone.
            </p>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                disabled={isDeleting}
                className="btn btn-ghost btn-sm"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                disabled={isDeleting}
                className="btn btn-error btn-sm"
              >
                {isDeleting ? (
                  <>
                    <span className="loading loading-spinner loading-xs"></span>
                    Deleting...
                  </>
                ) : (
                  'Delete'
                )}
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};
