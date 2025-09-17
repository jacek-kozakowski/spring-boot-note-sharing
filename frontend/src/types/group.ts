export interface Group {
  id: number;
  name: string;
  description: string;
  ownerUsername: string;
  createdAt: string;
  membersCount: number;
  isPrivate: boolean;
  isMember: boolean;
}

export interface GroupMember {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  enabled: boolean;
}

export interface CreateGroupDto {
  name: string;
  description: string;
  isPrivate: boolean;
  password?: string;
}

export interface UpdateGroupDto {
  name?: string;
  description?: string;
  privateGroup?: boolean;
  password?: string;
}

export interface JoinGroupRequestDto {
  password?: string;
}

export interface GroupSearchResult {
  id: number;
  name: string;
  description: string;
  ownerUsername: string;
  memberCount: number;
  isMember: boolean;
}
