package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.CoursNotFoundException;
import fr.eni.gestionformation.exception.CycleDetectedException;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import fr.eni.gestionformation.repository.CoursRepository;
import fr.eni.gestionformation.repository.CursusCoursRepository;
import fr.eni.gestionformation.repository.InscriptionCoursRepository;
import fr.eni.gestionformation.repository.UserRepository;
import fr.eni.gestionformation.entity.CoursPlanifie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CoursService {

    private final CoursRepository coursRepository;
    private final UserRepository userRepository;
    private final CursusCoursRepository cursusCoursRepository;
    private final CoursPlanifieRepository coursPlanifieRepository;
    private final InscriptionCoursRepository inscriptionCoursRepository;

    public List<Cours> findAll() {
        return coursRepository.findAll();
    }

    public Cours findById(Long id) {
        return coursRepository.findById(id)
                .orElseThrow(() -> new CoursNotFoundException(id));
    }

    public Cours save(Cours cours) {
        return coursRepository.save(cours);
    }

    @Transactional
    public Cours updateNomEtDuree(Long id, String name, Integer dureeJours) {
        Cours cours = findById(id);
        if (name != null) {
            cours.setName(name);
        }
        cours.setDureeJours(dureeJours);
        return coursRepository.save(cours);
    }

    @Transactional
    public void deleteById(Long id) {
        Cours cours = findById(id);
        // Un cours du catalogue est independant : sa suppression retire aussi
        // toutes les associations CursusCours qui le referencaient, dans
        // n'importe quel cursus, sans bloquer la suppression.
        cursusCoursRepository.deleteAll(cursusCoursRepository.findByCoursId(id));

        // Nettoyage des sessions planifiees de ce cours et des inscriptions associees,
        // pour eviter une violation de FK (cours_planifie.cours_id, inscription_cours.cours_planifie_id).
        List<CoursPlanifie> planning = coursPlanifieRepository.findByCoursId(id);
        if (!planning.isEmpty()) {
            List<Long> coursPlanifieIds = planning.stream().map(CoursPlanifie::getId).toList();
            inscriptionCoursRepository.deleteAll(inscriptionCoursRepository.findByCoursPlanifieIdIn(coursPlanifieIds));
            coursPlanifieRepository.deleteAll(planning);
        }

        // Nettoyage bidirectionnel de la table de jointure cours_prerequis :
        // - les prerequis de ce cours (cours_id = id) sont retires via setPrerequis(vide) ;
        // - ce cours en tant que prerequis d'autres cours (prerequis_id = id) est retire
        //   de chaque cours referent, meme pattern que dans setPrerequis.
        if (!cours.getPrerequis().isEmpty()) {
            cours.setPrerequis(new HashSet<>());
            coursRepository.save(cours);
        }
        List<Cours> referents = coursRepository.findByPrerequisId(id);
        referents.forEach(referent -> referent.getPrerequis().removeIf(p -> p.getId().equals(id)));
        coursRepository.saveAll(referents);

        coursRepository.deleteById(id);
    }

    @Transactional
    public Cours assignFormateurs(Long coursId, List<Long> formateurIds) {
        Cours cours = findById(coursId);
        List<User> formateurs = userRepository.findAllById(formateurIds);
        formateurs.forEach(user -> {
            if (user.getRole() != Role.FORMATEUR) {
                throw new IllegalArgumentException(
                    "L'utilisateur avec l'id " + user.getUid() + " n'est pas un formateur.");
            }
        });
        cours.setFormateurs(formateurs);
        return coursRepository.save(cours);
    }

    @Transactional
    public Cours setPrerequis(Long coursId, List<Long> prerequisIds) {
        Cours cours = findById(coursId);
        List<Cours> prerequisCandidats = prerequisIds == null
                ? List.of()
                : coursRepository.findAllById(prerequisIds);

        for (Cours candidat : prerequisCandidats) {
            if (candidat.getId().equals(coursId)) {
                throw new CycleDetectedException(
                        "Un cours ne peut pas etre son propre prerequis (cours id " + coursId + ").");
            }
            if (wouldCreateCycle(cours, candidat)) {
                throw new CycleDetectedException(
                        "Ajouter le cours id " + candidat.getId() + " comme prerequis du cours id " + coursId
                                + " creerait un cycle dans le graphe des prerequis.");
            }
        }

        cours.setPrerequis(new HashSet<>(prerequisCandidats));
        return coursRepository.save(cours);
    }

    /**
     * Verifie si ajouter "candidat" comme prerequis de "cours" creerait un cycle,
     * c'est-a-dire si "cours" est atteignable depuis "candidat" en suivant
     * les arcs de prerequis existants.
     */
    private boolean wouldCreateCycle(Cours cours, Cours candidat) {
        Set<Long> visited = new HashSet<>();
        Deque<Cours> toVisit = new ArrayDeque<>();
        toVisit.push(candidat);

        while (!toVisit.isEmpty()) {
            Cours current = toVisit.pop();
            if (current.getId().equals(cours.getId())) {
                return true;
            }
            if (!visited.add(current.getId())) {
                continue;
            }
            Cours loaded = coursRepository.findById(current.getId()).orElse(current);
            for (Cours next : loaded.getPrerequis()) {
                if (!visited.contains(next.getId())) {
                    toVisit.push(next);
                }
            }
        }
        return false;
    }
}
