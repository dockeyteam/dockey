import React from 'react';

export const Loading: React.FC<{ size?: 'sm' | 'md' | 'lg'; fullScreen?: boolean }> = ({
  size = 'md',
  fullScreen = false,
}) => {
  const sizeClass = {
    sm: 'loading-sm',
    md: 'loading-md',
    lg: 'loading-lg',
  }[size];

  const content = <span className={`loading loading-spinner ${sizeClass}`}></span>;

  if (fullScreen) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        {content}
      </div>
    );
  }

  return content;
};
