import { Observable } from "rxjs";
import { CreerInscriptionRequest, InscriptionCours, InscritCours } from "../models/inscription.model";

export abstract class BaseInscriptionAdapter {
  abstract getInscrits(coursPlanifieId: number): Observable<InscritCours[]>;
  abstract creerInscription(coursPlanifieId: number, request: CreerInscriptionRequest): Observable<InscriptionCours>;
}
