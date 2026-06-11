import { FormateurInfo } from "./cours.model";

export interface Filiere {
  id: number;
  name: string;
}

export interface CreateFiliereRequest {
  name: string;
}

export interface CoursInCursus {
  id: number;
  name: string;
  ordre: number;
  formateurs: FormateurInfo[];
}

export interface Cursus {
  id: number;
  name: string;
  filiereId: number | null;
  filiereName: string | null;
  cours: CoursInCursus[];
}

export interface CreateCursusRequest {
  name: string;
  filiereId: number;
}
