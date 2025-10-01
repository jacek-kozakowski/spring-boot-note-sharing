export interface NoteImage {
  id: number;
  url: string;
}

export interface Note {
  id: number;
  title: string;
  content: string;
  ownerUsername: string;
  createdAt: string;
  updatedAt: string;
  images: NoteImage[];
}

export interface CreateNoteDto {
  title: string;
  content: string;
  images?: File[];
}

export interface UpdateNoteDto {
  title?: string;
  content?: string;
  newImages?: File[];
  removeImageIds?: number[];
}

export interface NoteSearchParams {
  query: string;
  filter?: 'ALL' | 'ACTIVE' | 'DELETED';
}
