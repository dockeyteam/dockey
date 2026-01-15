import React, { useEffect, useState, useCallback } from 'react';
import { CommentDialog } from './CommentDialog';
import { commentService } from '../../services';

interface CommentableContentProps {
  docId: string;
  content: string;
  className?: string;
  initialLineCounts?: Record<number, number>; // Line counts from docs-service response
}

interface LineState {
  isHovered: boolean;
}

export const CommentableContent: React.FC<CommentableContentProps> = ({
  docId,
  content,
  className = '',
  initialLineCounts,
}) => {
  const [lineCounts, setLineCounts] = useState<Record<number, number>>(initialLineCounts || {});
  const [lineStates, setLineStates] = useState<Record<number, LineState>>({});
  const [activeDialog, setActiveDialog] = useState<{
    lineNumber: number;
    lineContent: string;
    position: { x: number; y: number };
  } | null>(null);
  const [isLoadingCounts, setIsLoadingCounts] = useState(!initialLineCounts);

  // Parse content into lines
  const parseContentToLines = useCallback((htmlContent: string): string[] => {
    // Create a temporary div to parse HTML
    const temp = document.createElement('div');
    temp.innerHTML = htmlContent;
    
    // Get text content and split by newlines, or split by block elements
    const textContent = temp.textContent || temp.innerText || '';
    const lines = textContent.split(/\n/).filter(line => line.trim());
    
    // If no newlines, try to split by common block patterns
    if (lines.length <= 1 && htmlContent.length > 100) {
      // Split by common block-level HTML elements
      const blockRegex = /<\/(p|div|h[1-6]|li|tr|pre|blockquote)>/gi;
      const blocks = htmlContent.split(blockRegex).filter(block => {
        const stripped = block.replace(/<[^>]*>/g, '').trim();
        return stripped.length > 0 && !['p', 'div', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'li', 'tr', 'pre', 'blockquote'].includes(stripped.toLowerCase());
      });
      if (blocks.length > 1) {
        return blocks.map(block => block.replace(/<[^>]*>/g, '').trim()).filter(Boolean);
      }
    }
    
    return lines.length > 0 ? lines : [textContent || 'No content'];
  }, []);

  const lines = parseContentToLines(content);

  // Update line counts when initialLineCounts changes
  useEffect(() => {
    if (initialLineCounts) {
      setLineCounts(initialLineCounts);
      setIsLoadingCounts(false);
    }
  }, [initialLineCounts]);

  // Only fetch from comments service if no initial counts provided
  useEffect(() => {
    const loadCounts = async () => {
      if (!docId || initialLineCounts) return;
      
      setIsLoadingCounts(true);
      try {
        const response = await commentService.getLineCommentCounts(docId);
        setLineCounts(response.lineCounts || {});
      } catch (error) {
        console.error('Failed to load comment counts:', error);
        // Don't reset to empty - keep any existing counts
      } finally {
        setIsLoadingCounts(false);
      }
    };

    loadCounts();
  }, [docId, initialLineCounts]);

  const handleLineHover = (lineNumber: number, isHovered: boolean) => {
    setLineStates(prev => ({
      ...prev,
      [lineNumber]: { ...prev[lineNumber], isHovered },
    }));
  };

  const handleIndicatorClick = (
    lineNumber: number,
    lineContent: string
  ) => {
    // Toggle dialog - close if already open for this line
    if (activeDialog?.lineNumber === lineNumber) {
      setActiveDialog(null);
    } else {
      setActiveDialog({
        lineNumber,
        lineContent,
        position: { x: 0, y: 0 }, // Position no longer used for inline rendering
      });
    }
  };

  const handleCloseDialog = () => {
    setActiveDialog(null);
  };

  const handleCommentAdded = () => {
    // Increment the count for the active line
    if (activeDialog) {
      setLineCounts(prev => ({
        ...prev,
        [activeDialog.lineNumber]: (prev[activeDialog.lineNumber] || 0) + 1,
      }));
    }
  };

  const handleCommentDeleted = () => {
    // Decrement the count for the active line
    if (activeDialog) {
      setLineCounts(prev => ({
        ...prev,
        [activeDialog.lineNumber]: Math.max(0, (prev[activeDialog.lineNumber] || 1) - 1),
      }));
    }
  };

  const totalComments = Object.values(lineCounts).reduce((sum, count) => sum + count, 0);

  return (
    <div className={`commentable-content relative ${className}`}>
      {/* Stats bar */}
      <div className="flex items-center gap-4 mb-6 pb-4 border-b border-base-content/10">
        <div className="flex items-center gap-2 text-sm text-base-content/60">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
          </svg>
          <span>
            {isLoadingCounts ? (
              <span className="loading loading-spinner loading-xs"></span>
            ) : (
              `${totalComments} comment${totalComments !== 1 ? 's' : ''}`
            )}
          </span>
        </div>
        <div className="flex items-center gap-2 text-sm text-base-content/60">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
          </svg>
          <span>{lines.length} lines</span>
        </div>
        <div className="text-xs text-base-content/40 ml-auto">
          Hover over lines to add comments
        </div>
      </div>

      {/* Content with line indicators */}
      <div className="relative">
        {lines.map((line, index) => {
          const lineNumber = index + 1;
          const commentCount = lineCounts[lineNumber] || 0;
          const lineState = lineStates[lineNumber] || { isHovered: false };
          const isDialogOpen = activeDialog?.lineNumber === lineNumber;

          return (
            <React.Fragment key={lineNumber}>
              <div
                className={`group relative flex items-start py-1 rounded transition-colors ${
                  isDialogOpen ? 'bg-primary/10' : 'hover:bg-base-content/5'
                }`}
                onMouseEnter={() => handleLineHover(lineNumber, true)}
                onMouseLeave={() => handleLineHover(lineNumber, false)}
              >
                {/* Left gutter: line number + comment button */}
                <div className="shrink-0 w-16 flex items-center justify-end gap-1 pr-3 select-none">
                  {/* Comment indicator button */}
                  <button
                    onClick={() => handleIndicatorClick(lineNumber, line)}
                    className={`
                      flex items-center justify-center gap-0.5
                      min-w-6 h-6 px-1 rounded
                      transition-all duration-150
                      ${hasComments(commentCount)
                        ? 'bg-primary/20 hover:bg-primary/30 text-primary'
                        : 'text-base-content/20 hover:bg-primary/10 hover:text-primary'
                      }
                      ${lineState.isHovered && !hasComments(commentCount) ? 'opacity-100' : ''}
                      ${hasComments(commentCount) ? 'opacity-100' : 'opacity-0'}
                      group-hover:opacity-100
                    `}
                    title={hasComments(commentCount) ? `${commentCount} comment${commentCount > 1 ? 's' : ''}` : 'Add comment'}
                  >
                    {hasComments(commentCount) ? (
                      <>
                        <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                        </svg>
                        <span className="text-xs font-medium">{commentCount}</span>
                      </>
                    ) : (
                      <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                      </svg>
                    )}
                  </button>
                  
                  {/* Line number */}
                  <span className="w-6 text-right text-xs text-base-content/30 font-mono">
                    {lineNumber}
                  </span>
                </div>

                {/* Line content */}
                <div className="flex-1 min-w-0 px-2">
                  <span className="text-base-content/90 wrap-break-word">{line}</span>
                </div>

                {/* Highlight bar for lines with comments */}
                {commentCount > 0 && (
                  <div className="absolute left-0 top-0 bottom-0 w-0.5 bg-primary/50 rounded-full" />
                )}
              </div>

              {/* Inline comment dialog - appears below the active line */}
              {isDialogOpen && (
                <div className="ml-16 mr-4 my-1">
                  <CommentDialog
                    isOpen={true}
                    onClose={handleCloseDialog}
                    docId={docId}
                    lineNumber={activeDialog.lineNumber}
                    lineContent={activeDialog.lineContent}
                    onCommentAdded={handleCommentAdded}
                    onCommentDeleted={handleCommentDeleted}
                  />
                </div>
              )}
            </React.Fragment>
          );
        })}
      </div>
    </div>
  );

  function hasComments(count: number): boolean {
    return count > 0;
  }
};
