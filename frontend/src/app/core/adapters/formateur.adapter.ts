import { Observable } from "rxjs";

export interface FormateurInfo {
  id: number;
  firstName: string;
  lastName: string;
}

export abstract class BaseFormateurAdapter {
  abstract getAll(): Observable<FormateurInfo[]>;
}
