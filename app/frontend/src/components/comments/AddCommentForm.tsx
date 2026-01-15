import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';

interface AddCommentFormProps {
  onSubmit: (content: string) => Promise<void>;
  onCancel?: () => void;
  placeholder?: string;
  autoFocus?: boolean;
}

export const AddCommentForm: React.FC<AddCommentFormProps> = ({
  onSubmit,
  onCancel,
  placeholder = 'Add a comment...',
  autoFocus = true,
}) => {
  const { user, isAuthenticated } = useAuth();
  const [content, setContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Auto-resize textarea
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = Math.min(textareaRef.current.scrollHeight, 120) + 'px';
    }
  }, [content]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim() || isSubmitting) return;

    setIsSubmitting(true);
    try {
      await onSubmit(content.trim());
      setContent('');
    } catch (error) {
      console.error('Failed to submit comment:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
      handleSubmit(e);
    }
    if (e.key === 'Escape' && onCancel) {
      onCancel();
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="py-2 text-center">
        <p className="text-xs text-base-content/50">
          <a href="/login" className="text-primary hover:underline">Sign in</a> to comment
        </p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="flex items-start gap-2">
      {/* Compact avatar */}
      <div className="shrink-0 w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center">
        <span className="text-xs font-semibold text-primary">
          {user?.username?.charAt(0)?.toUpperCase() || '?'}
        </span>
      </div>

      {/* Input + submit */}
      <div className="flex-1 flex items-end gap-2">
        <textarea
          ref={textareaRef}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          autoFocus={autoFocus}
          rows={1}
          className="flex-1 bg-base-200/50 border border-base-content/10 rounded-lg px-3 py-1.5 text-sm resize-none focus:outline-none focus:border-primary/50 placeholder:text-base-content/40"
          disabled={isSubmitting}
          style={{ minHeight: '32px', maxHeight: '120px' }}
        />
        
        <button
          type="submit"
          disabled={!content.trim() || isSubmitting}
          className="shrink-0 btn btn-primary btn-sm px-3 h-8 min-h-0"
        >
          {isSubmitting ? (
            <span className="loading loading-spinner loading-xs"></span>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          )}
        </button>
      </div>
    </form>
  );
};
