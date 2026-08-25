export interface User {
  id: number;
  email: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  role: string;
}

export interface UpdateRoleRequest {
  role: string;
}
