export interface Message {
  id: number;
  content: string;
  author: string;
  createdAt: string;
  groupId: number;
}

export interface SendMessageDto {
  content: string;
  groupId: number;
}

export interface MessagePage {
  content: Message[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
