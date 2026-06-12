package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.exception.FiliereAlreadyExistsException;
import fr.eni.gestionformation.exception.FiliereInUseException;
import fr.eni.gestionformation.exception.FiliereNotFoundException;
import fr.eni.gestionformation.repository.FiliereRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FiliereService {
    final private FiliereRepository filiereRepository;

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
         if (this.existsByName(filiere.getName())){
            throw new FiliereAlreadyExistsException(filiere.getName());
         }
        return filiereRepository.save(filiere);
     }

     public Filiere update(Long id, String name) {
        Filiere filiere = this.findById(id);
        filiereRepository.findByName(name)
                .filter(f -> !f.getId().equals(id))
                .ifPresent(_ -> {
                    throw new FiliereAlreadyExistsException(name);
                });
        filiere.setName(name);
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
