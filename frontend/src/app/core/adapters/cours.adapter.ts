import { Observable } from "rxjs";
import { Cours, CreateCoursRequest } from "../models/cours.model";

export abstract class BaseCoursAdapter {
  abstract getAll(): Observable<Cours[]>;

  abstract create(req: CreateCoursRequest): Observable<Cours>;

  abstract update(id: number, req: CreateCoursRequest): Observable<Cours>;

  abstract delete(id: number): Observable<void>;

  abstract setFormateurs(id: number, formateurIds: number[]): Observable<Cours>;

  abstract setPrerequis(id: number, prerequisIds: number[]): Observable<Cours>;
}
