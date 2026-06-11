export interface FormateurInfo {
  id: number;
  firstName: string;
  lastName: string;
}

export interface Cours {
  id: number;
  name: string;
  dureeJours: number | null;
  formateurs: FormateurInfo[];
  prerequis: Cours[];
}

export interface CreateCoursRequest {
  name: string;
  dureeJours?: number | null;
  formateurIds?: number[];
  prerequisIds?: number[];
}
