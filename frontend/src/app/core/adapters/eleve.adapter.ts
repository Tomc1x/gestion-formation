import { Observable } from "rxjs";

export interface EleveInfo {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export abstract class BaseEleveAdapter {
  abstract getAll(): Observable<EleveInfo[]>;
}
