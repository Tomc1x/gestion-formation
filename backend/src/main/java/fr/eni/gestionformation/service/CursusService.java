package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.exception.CursusNotFoundException;
import fr.eni.gestionformation.repository.CoursRepository;
import fr.eni.gestionformation.repository.CursusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursusService {

    private final CursusRepository cursusRepository;
    private final CoursRepository coursRepository;

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
    public void deleteById(Long id, boolean cascade) {
        Cursus cursus = findById(id);
        List<Cours> coursList = coursRepository.findByCursusId(id);
        if (cascade) {
            coursRepository.deleteAll(coursList);
        } else {
            coursList.forEach(cours -> cours.setCursus(null));
            coursRepository.saveAll(coursList);
        }
        cursusRepository.delete(cursus);
    }
}
