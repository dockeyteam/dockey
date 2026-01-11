export interface Comment {
  id: string;
  docId: string;
  lineNumber: number;
  userId: string;
  userName: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  likedByCurrentUser: boolean;
}

export interface CreateCommentRequest {
  docId: string;
  lineNumber: number;
  content: string;
}

export interface CommentResponse extends Comment {}
