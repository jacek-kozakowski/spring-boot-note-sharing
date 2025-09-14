import axios from 'axios';
import {registerUserDto, loginUserDto, verifyUserDto, resendVerificationDto, updateUserDto} from '../types/user'

const API_BASE_URL: string = 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json"
    }
});

api.interceptors.request.use(
    (config) => {
        const token: string | null = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        // @ts-ignore
        return Promise.reject(error);
    }
);
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
        }
        // @ts-ignore
        return Promise.reject(error);
    }
);

// ENDPOINTS
export const notexAPI = {
    auth: {
        register: (registerUserDto: registerUserDto) => api.post('/auth/register', registerUserDto),
        login: (loginUserRequestDto: loginUserDto) => api.post('/auth/login', loginUserRequestDto),
        verify: (verifyUserDto: verifyUserDto) => api.post('/auth/verify', verifyUserDto),
        resend: (resendVerificationDto: resendVerificationDto) => api.post('/auth/resend', resendVerificationDto),
    },

    users: {
        getMe: () => api.get('/users/me'),
        getAllUsers: () => api.get('/users'), // Admin Only
        getMyNotes: () => api.get('/users/me/notes'),
        getMyGroups: () => api.get('/users/me/groups'),
        getUserByUsername: (username:string) => api.get(`/users/${username}`),
        getUserGroups: (username:string) => api.get(`/users/${username}/groups`), // Admin Only
        getUserNotes: (username:string) => api.get(`/users/${username}/notes`),
        getUserNotesAdmin: (username:string, filter: "ALL" | "ACTIVE" | "DELETED" = "ALL" ) => api.get(`/users/${username}/notes/admin`, {params: {filter}}), // Admin Only
        updateUser: (updateUserDto: updateUserDto) => api.patch('/users/me', updateUserDto),
        updateUserByAdmin: (username:string, updateUserDto: updateUserDto ) => api.patch(`/users/${username}`, updateUserDto),
    },

    notes: {
        getNoteById: (noteId: bigint) => api.get(`/notes/${noteId}`),
        getNoteByPartialName: (partialName: string) => api.get(`/notes`, {params: {partialName}}),
        createNote: (createNoteDto:any) => api.post('/notes', createNoteDto, {headers: {'Content-Type': 'multipart/form-data'}}),
        updateNote: (noteId: bigint, updateNoteDto:any)=> api.patch(`/notes/${noteId}`, updateNoteDto, {headers: {'Content-Type': 'multipart/form-data'}}),
        deleteNote: (noteId: bigint) => api.delete(`/notes/${noteId}`),
        deleteNoteImage: (noteId: bigint, imageId: bigint) => api.delete(`/notes/${noteId}/images/${imageId}`)
    },

    groups: {
        getGroupsByName: (name: string) => api.get(`/groups`, {params: {name}}),
        getGroupById: (groupId: bigint) => api.get(`/groups/${groupId}`),
        createGroup: (createGroupDto:any) => api.post('/groups', createGroupDto),
        updateGroup: (groupId: bigint, updateGroupDto:any) => api.patch(`/groups/${groupId}`, updateGroupDto),
        deleteGroup: (groupId: bigint)=> api.delete(`/groups/${groupId}`),
        addUserToGroup: (groupId: bigint, username: string) => api.post(`/groups/${groupId}/members/${username}`),
        joinGroup: (groupId: bigint, joinGroupRequestDto: any) => api.post(`/groups/${groupId}/members`, joinGroupRequestDto),
        removeUserFromGroup: (groupId: bigint, username: string) => api.delete(`/groups/${groupId}/members/${username}`),
        leaveGroup: (groupId: bigint) => api.delete(`/groups/${groupId}/members/me`)
    },

    messages: {
        getMessages: (groupId: bigint, page: number, pageSize: number) => api.get(`/groups/${groupId}/messages`, {params: {page, pageSize}}),
        sendMessage: (groupID: bigint, sendMessageDto: any) => api.post(`/groups/${groupID}/messages`, sendMessageDto)
    }
}

export const apiHelpers = {
    isLoggedIn: () => {
        return !!localStorage.getItem('token');
    },

    getToken: () => {
        return localStorage.getItem('token');
    },

    setToken: (token: string) => {
        localStorage.setItem('token', token);
    },

    removeToken: () => {
        localStorage.removeItem('token');
    },

    healthCheck: async () => {
        try{
            const response = await api.get('/health');
            return response.status === 200;
        }catch (error){
            return false;
        }
    }
}


export default api;
