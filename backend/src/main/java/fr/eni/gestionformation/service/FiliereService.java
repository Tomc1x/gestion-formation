package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.exception.FiliereAlreadyExistsException;
import fr.eni.gestionformation.exception.FiliereInUseException;
import fr.eni.gestionformation.exception.FiliereNotFoundException;
import fr.eni.gestionformation.repository.FiliereRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FiliereService {
    private final FiliereRepository filiereRepository;

    public boolean existsByName(String name) {
        return filiereRepository.findByName(name).isPresent();
    }

    public List<Filiere> findAll() {
        return filiereRepository.findAll();
    }

    public List<Filiere> findByName(String name) {
        return filiereRepository.findByName(name)
                .map(List::of)
                .orElse(List.of());
    }

    public Filiere findById(Long id) {
        return filiereRepository.findById(id)
                .orElseThrow(() -> new FiliereNotFoundException(id));
    }

    public Filiere save(Filiere filiere) {
        if (filiereRepository.findByNameIgnoreCase(filiere.getName()).isPresent()) {
            throw new FiliereAlreadyExistsException(filiere.getName());
        }
        return filiereRepository.save(filiere);
    }

    public Filiere update(Long id, String name) {
        Filiere filiere = this.findById(id);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Vous ne pouvez pas enregistrer une filière sans nom");
        }

        Optional<Filiere> filiereExistante = filiereRepository.findByNameIgnoreCase(name);
        if (filiereExistante.isPresent() && !filiereExistante.get().getId().equals(id)) {
            throw new FiliereAlreadyExistsException(name);
        }

        filiere.setName(name.trim());
        return filiereRepository.save(filiere);
    }

    public void deleteById(Long id) {
        Filiere filiere = this.findById(id);
        if (!filiere.getCursus().isEmpty()) {
            throw new FiliereInUseException(id);
        }
        filiereRepository.deleteById(id);
    }
}
