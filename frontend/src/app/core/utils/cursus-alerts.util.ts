import { Cours } from '../models/cours.model';

/** Une alerte "prérequis mal ordonné" pour un cours d'un cursus. */
export interface CursusPrereqAlert {
  /** Le cours dont le prérequis est mal placé. */
  cours: Cours;
  /** Position (1-indexée) du cours concerné dans la liste ordonnée. */
  coursPosition: number;
  /** Le prérequis manquant en amont. */
  prereq: Cours;
  /** Position actuelle (1-indexée) du prérequis dans la liste ordonnée. */
  prereqPosition: number;
}

/** Renvoie tous les prérequis (directs et transitifs) d'un cours, sans doublons. */
export function transitivePrerequis(cours: Cours, seen = new Set<number>()): Cours[] {
  const result: Cours[] = [];
  for (const prereq of cours.prerequis) {
    if (seen.has(prereq.id)) continue;
    seen.add(prereq.id);
    result.push(prereq);
    result.push(...transitivePrerequis(prereq, seen));
  }
  return result;
}

/**
 * Calcule les alertes "prérequis mal ordonné" pour une liste ordonnée de cours.
 *
 * Pour chaque cours, si un de ses prérequis (direct ou transitif) apparaît APRÈS lui
 * dans la liste (ou est absent), une alerte est générée avec la position actuelle du
 * prérequis (0 si absent de la liste).
 *
 * Fonction pure, sans dépendance Angular, réutilisable pour calculer un badge
 * "N alerte(s)" sur la liste des cursus.
 */
export function computeCursusPrereqAlerts(cours: Cours[]): CursusPrereqAlert[] {
  const alerts: CursusPrereqAlert[] = [];

  for (let i = 0; i < cours.length; i++) {
    const c = cours[i];
    const allPrereqs = transitivePrerequis(c);

    for (const prereq of allPrereqs) {
      const prereqIndex = cours.findIndex(x => x.id === prereq.id);
      if (prereqIndex === -1 || prereqIndex > i) {
        alerts.push({
          cours: c,
          coursPosition: i + 1,
          prereq,
          prereqPosition: prereqIndex + 1,
        });
      }
    }
  }

  return alerts;
}
