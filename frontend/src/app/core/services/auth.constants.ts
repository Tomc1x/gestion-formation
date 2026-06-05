export type Role = 'REF' | 'ADMIN' | 'FORMATEUR' | 'ELEVE';

export interface RoleMeta {
  label: string;
  bg: string;
  color: string;
}

export interface UserProfile {
  nom: string;
  email: string;
}

export const ROLE_META: Record<Role, RoleMeta> = {
  REF: {
    label: 'Référent',
    bg: '#E3ECF6',
    color: '#1E3A5F',
  },
  ADMIN: {
    label: 'Administrateur',
    bg: '#EDE9F8',
    color: '#5B21B6',
  },
  FORMATEUR: {
    label: 'Formateur',
    bg: '#E4F5EE',
    color: '#1E9E6A',
  },
  ELEVE: {
    label: 'Élève',
    bg: '#FDF3E5',
    color: '#D97706',
  },
};

export const USER_FOR_ROLE: Record<Role, UserProfile> = {
  REF: {
    nom: 'Marie Dupont',
    email: 'marie.dupont@eni-ecole.fr',
  },
  ADMIN: {
    nom: 'Jean-Pierre Moreau',
    email: 'jp.moreau@eni-ecole.fr',
  },
  FORMATEUR: {
    nom: 'Claire Bernard',
    email: 'claire.bernard@eni-ecole.fr',
  },
  ELEVE: {
    nom: 'Lucas Martin',
    email: 'lucas.martin@eni-ecole.fr',
  },
};
