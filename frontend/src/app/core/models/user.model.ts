export type BackendRole = 'ADMINISTRATEUR' | 'REFERENTE_ADMINISTRATIVE' | 'FORMATEUR' | 'ETUDIANT';

export interface UserAdmin {
  uid: number;
  firstName: string;
  lastName: string;
  email: string;
  role: BackendRole;
  enabled: boolean;
}

export type FrontendRole = 'ADMIN' | 'REF' | 'FORMATEUR' | 'ELEVE';

export const BACKEND_TO_FRONTEND_ROLE: Record<BackendRole, FrontendRole> = {
  ADMINISTRATEUR: 'ADMIN',
  REFERENTE_ADMINISTRATIVE: 'REF',
  FORMATEUR: 'FORMATEUR',
  ETUDIANT: 'ELEVE',
};

export const ROLE_FILTER_LABELS: { value: BackendRole | 'all'; label: string }[] = [
  { value: 'all', label: 'Tous les rôles' },
  { value: 'ADMINISTRATEUR', label: 'Administrateur' },
  { value: 'REFERENTE_ADMINISTRATIVE', label: 'Référent' },
  { value: 'FORMATEUR', label: 'Formateur' },
  { value: 'ETUDIANT', label: 'Étudiant' },
];

export const BACKEND_ROLE_OPTIONS: { value: BackendRole; label: string }[] = [
  { value: 'ADMINISTRATEUR', label: 'Administrateur' },
  { value: 'REFERENTE_ADMINISTRATIVE', label: 'Référent administratif' },
  { value: 'FORMATEUR', label: 'Formateur' },
  { value: 'ETUDIANT', label: 'Étudiant' },
];

// Interfaces pour les requêtes API
export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: BackendRole;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  email: string;
}

export interface ChangeRoleRequest {
  role: BackendRole;
}

export interface ChangePasswordRequest {
  newPassword: string;
}

export interface InviteRequest {
  email: string;
  role: BackendRole;
}
