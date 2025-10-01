export interface registerUserDto {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
}

export interface loginUserDto {
    username: string;
    password: string;
}

export interface verifyUserDto {
    username: string;
    verificationCode: string;
}

export interface resendVerificationDto {
    username: string;
}

export interface updateUserDto {
    username?: string;
    password?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
}

