export interface FormateurInfo {
  id: number;
  firstName: string;
  lastName: string;
}

export interface Cours {
  id: number;
  name: string;
  formateurs: FormateurInfo[];
  prerequis: Cours[];
}

export interface CreateCoursRequest {
  name: string;
  formateurIds?: number[];
  prerequisIds?: number[];
}
