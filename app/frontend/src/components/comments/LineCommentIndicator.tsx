import React from 'react';

interface LineCommentIndicatorProps {
  commentCount: number;
  isHovered: boolean;
  onClick: (e: React.MouseEvent) => void;
}

export const LineCommentIndicator: React.FC<LineCommentIndicatorProps> = ({
  commentCount,
  isHovered,
  onClick,
}) => {
  const hasComments = commentCount > 0;

  // Always show indicator area for easier clicking, just change visibility
  return (
    <div className="absolute -left-14 top-1/2 -translate-y-1/2 w-14 flex items-center">
      {/* Line number */}
      <span className="w-6 text-right text-xs text-base-content/30 select-none font-mono mr-1">
        {/* Line number is rendered by parent */}
      </span>
      <button
        onClick={onClick}
        className={`
          flex items-center justify-center gap-1 
          min-w-7 h-6 px-1 rounded
          transition-all duration-200 ease-out
          ${hasComments
            ? 'bg-primary/20 hover:bg-primary/30 text-primary'
            : 'bg-transparent hover:bg-primary/10 text-base-content/30 hover:text-primary'
          }
          ${isHovered && !hasComments ? 'opacity-100' : ''}
          ${hasComments ? 'opacity-100' : 'opacity-0'}
          group-hover:opacity-100
          hover:scale-105 active:scale-95
        `}
        title={hasComments ? `${commentCount} comment${commentCount > 1 ? 's' : ''}` : 'Add comment'}
        aria-label={hasComments ? `View ${commentCount} comment${commentCount > 1 ? 's' : ''}` : 'Add comment'}
      >
        {hasComments ? (
          <>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-4 w-4 shrink-0"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
              />
            </svg>
            <span className="text-xs font-semibold">{commentCount}</span>
          </>
        ) : (
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-4 w-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 4v16m8-8H4"
            />
          </svg>
        )}
      </button>
    </div>
  );
};
