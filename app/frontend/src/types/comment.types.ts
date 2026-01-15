export interface Comment {
  id: string;
  docId: string;
  lineNumber: number;
  userId: string;
  userName: string;
  content: string;
  // Date can be ISO string or Java LocalDateTime array: [year, month, day, hour, minute, second, nano]
  createdAt: string | number[];
  updatedAt: string | number[];
  likeCount: number;
  likedByCurrentUser: boolean;
}

export interface CreateCommentRequest {
  docId: string;
  lineNumber: number;
  content: string;
}

export interface CommentResponse extends Comment {}

export interface LineCommentCountResponse {
  docId: string;
  lineCounts: Record<number, number>;
  totalComments: number;
}

export interface LineCommentState {
  lineNumber: number;
  commentCount: number;
  isHovered: boolean;
  isOpen: boolean;
}
