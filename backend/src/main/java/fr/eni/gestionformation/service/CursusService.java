package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.CursusCours;
import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.exception.CursusNotFoundException;
import fr.eni.gestionformation.repository.CursusCoursRepository;
import fr.eni.gestionformation.repository.CursusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CursusService {

    private final CursusRepository cursusRepository;
    private final CursusCoursRepository cursusCoursRepository;
    private final CoursService coursService;
    private final PromotionService promotionService;

    public List<Cursus> findAll() {
        return cursusRepository.findAll();
    }

    public Cursus findById(Long id) {
        return cursusRepository.findById(id)
                .orElseThrow(() -> new CursusNotFoundException(id));
    }

    public List<Cursus> findByFiliereId(Long filiereId) {
        return cursusRepository.findByFiliereId(filiereId);
    }

    public Cursus save(Cursus cursus) {
        cursusRepository.findByName(cursus.getName()).ifPresent(_ -> {
            throw new IllegalArgumentException("Un cursus avec le nom '" + cursus.getName() + "' existe déjà.");
        });
        return cursusRepository.save(cursus);
    }

    @Transactional
    public Cursus update(Long id, String name, Long filiereId, Filiere filiere) {
        Cursus cursus = findById(id);

        cursusRepository.findByName(name)
                .filter(c -> !c.getId().equals(id))
                .ifPresent(_ -> {
                    throw new IllegalArgumentException("Un cursus avec le nom '" + name + "' existe déjà.");
                });

        cursus.setName(name);
        cursus.setFiliere(filiereId != null ? filiere : null);
        return cursusRepository.save(cursus);
    }

    @Transactional
    public void deleteById(Long id) {
        Cursus cursus = findById(id);
        List<CursusCours> liaisons = cursusCoursRepository.findByCursusIdOrderByOrdre(id);
        cursusCoursRepository.deleteAll(liaisons);
        promotionService.clearCursusReferences(id);
        cursusRepository.delete(cursus);
    }

    @Transactional
    public void addCours(Long cursusId, Long coursId, Integer ordre) {
        Cursus cursus = findById(cursusId);
        Cours cours = coursService.findById(coursId);

        cursusCoursRepository.findByCursusIdAndCoursId(cursusId, coursId).ifPresent(_ -> {
            throw new IllegalArgumentException(
                "Le cours avec l'id " + coursId + " est deja present dans le cursus " + cursusId + ".");
        });

        int finalOrdre = Objects.requireNonNullElseGet(ordre, () -> cursusCoursRepository.findByCursusIdOrderByOrdre(cursusId).stream()
                .mapToInt(CursusCours::getOrdre)
                .max()
                .orElse(-1) + 1);

        CursusCours cursusCours = CursusCours.builder()
                .cursus(cursus)
                .cours(cours)
                .ordre(finalOrdre)
                .build();
        cursusCoursRepository.save(cursusCours);
    }

    @Transactional
    public void removeCours(Long cursusId, Long coursId) {
        CursusCours cursusCours = cursusCoursRepository.findByCursusIdAndCoursId(cursusId, coursId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Le cours avec l'id " + coursId + " n'est pas present dans le cursus " + cursusId + "."));
        cursusCoursRepository.delete(cursusCours);
    }

    @Transactional
    public void reorderCours(Long cursusId, List<Long> coursIdsInOrder) {
        List<CursusCours> liaisons = cursusCoursRepository.findByCursusIdOrderByOrdre(cursusId);

        Set<Long> currentCoursIds = new HashSet<>();
        liaisons.forEach(cc -> currentCoursIds.add(cc.getCours().getId()));
        Set<Long> requestedCoursIds = new HashSet<>(coursIdsInOrder);

        if (!currentCoursIds.equals(requestedCoursIds) || coursIdsInOrder.size() != liaisons.size()) {
            throw new IllegalArgumentException(
                    "La liste fournie doit contenir exactement les cours actuellement lies au cursus " + cursusId + ".");
        }

        for (CursusCours cursusCours : liaisons) {
            int newOrdre = coursIdsInOrder.indexOf(cursusCours.getCours().getId());
            cursusCours.setOrdre(newOrdre);
        }
        cursusCoursRepository.saveAll(liaisons);
    }

    public List<CursusCours> getCoursOrdonnes(Long cursusId) {
        return cursusCoursRepository.findByCursusIdOrderByOrdre(cursusId).stream()
                .sorted(Comparator.comparingInt(CursusCours::getOrdre))
                .toList();
    }
}
