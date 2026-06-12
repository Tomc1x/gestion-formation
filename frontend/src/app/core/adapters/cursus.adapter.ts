import { Observable } from "rxjs";
import { CreateCursusRequest, Cursus } from "../models/cursus.model";

export abstract class BaseCursusAdapter {
  abstract getAll(): Observable<Cursus[]>;

  abstract create(req: CreateCursusRequest): Observable<Cursus>;

  abstract update(cursusId: number, req: CreateCursusRequest): Observable<Cursus>;

  abstract delete(cursusId: number): Observable<void>;

  abstract addCours(cursusId: number, coursId: number, ordre?: number): Observable<Cursus>;

  abstract removeCours(cursusId: number, coursId: number): Observable<Cursus>;

  abstract reorder(cursusId: number, coursIds: number[]): Observable<Cursus>;
}
