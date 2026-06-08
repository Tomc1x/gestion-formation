package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.CoursNotFoundException;
import fr.eni.gestionformation.repository.CoursRepository;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursService {

    private final CoursRepository coursRepository;
    private final UserRepository userRepository;

    public List<Cours> findAll() {
        return coursRepository.findAll();
    }

    public Cours findById(Long id) {
        return coursRepository.findById(id)
                .orElseThrow(() -> new CoursNotFoundException(id));
    }

    public List<Cours> findByCursusId(Long cursusId) {
        return coursRepository.findByCursusId(cursusId);
    }

    public Cours save(Cours cours) {
        return coursRepository.save(cours);
    }

    public void deleteById(Long id) {
        findById(id);
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
}
