import { Observable } from "rxjs";
import { CreateFiliereRequest, Filiere } from "../models/cursus.model";

export abstract class BaseFiliereAdapter {
  abstract getAll(): Observable<Filiere[]>;

  abstract create(req: CreateFiliereRequest): Observable<Filiere>;

  abstract update(id: number, req: CreateFiliereRequest): Observable<Filiere>;

  abstract delete(id: number): Observable<void>;
}
