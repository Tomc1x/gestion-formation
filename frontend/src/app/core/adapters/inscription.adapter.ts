import { Observable } from "rxjs";
import { InscritCours } from "../models/inscription.model";

export abstract class BaseInscriptionAdapter {
  abstract getInscrits(coursPlanifieId: number): Observable<InscritCours[]>;
}
