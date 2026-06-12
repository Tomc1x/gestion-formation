package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.exception.FiliereAlreadyExistsException;
import fr.eni.gestionformation.exception.FiliereInUseException;
import fr.eni.gestionformation.exception.FiliereNotFoundException;
import fr.eni.gestionformation.repository.FiliereRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiliereServiceTest {

    @Mock
    FiliereRepository filiereRepository;

    @InjectMocks
    FiliereService filiereService;

    @Test
    void update_validName_updatesFiliere() {
        Filiere filiere = Filiere.builder().id(1L).name("Ancien nom").build();
        when(filiereRepository.findById(1L)).thenReturn(Optional.of(filiere));
        when(filiereRepository.findByName("Nouveau nom")).thenReturn(Optional.empty());
        when(filiereRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Filiere result = filiereService.update(1L, "Nouveau nom");

        assertThat(result.getName()).isEqualTo("Nouveau nom");
    }

    @Test
    void update_nameAlreadyUsedByAnotherFiliere_throwsAlreadyExists() {
        Filiere filiere = Filiere.builder().id(1L).name("Ancien nom").build();
        Filiere autreFiliere = Filiere.builder().id(2L).name("Nom pris").build();
        when(filiereRepository.findById(1L)).thenReturn(Optional.of(filiere));
        when(filiereRepository.findByName("Nom pris")).thenReturn(Optional.of(autreFiliere));

        assertThrows(FiliereAlreadyExistsException.class, () -> filiereService.update(1L, "Nom pris"));
    }

    @Test
    void update_sameNameAsItself_doesNotThrow() {
        Filiere filiere = Filiere.builder().id(1L).name("Meme nom").build();
        when(filiereRepository.findById(1L)).thenReturn(Optional.of(filiere));
        when(filiereRepository.findByName("Meme nom")).thenReturn(Optional.of(filiere));
        when(filiereRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Filiere result = filiereService.update(1L, "Meme nom");

        assertThat(result.getName()).isEqualTo("Meme nom");
    }

    @Test
    void update_unknownId_throwsNotFound() {
        when(filiereRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FiliereNotFoundException.class, () -> filiereService.update(99L, "Nom"));
    }

    @Test
    void deleteById_noCursus_deletesFiliere() {
        Filiere filiere = Filiere.builder().id(1L).name("Filiere").cursus(List.of()).build();
        when(filiereRepository.findById(1L)).thenReturn(Optional.of(filiere));

        filiereService.deleteById(1L);

        verify(filiereRepository).deleteById(1L);
    }

    @Test
    void deleteById_withCursus_throwsFiliereInUse() {
        Cursus cursus = Cursus.builder().id(1L).build();
        Filiere filiere = Filiere.builder().id(1L).name("Filiere").cursus(List.of(cursus)).build();
        when(filiereRepository.findById(1L)).thenReturn(Optional.of(filiere));

        assertThrows(FiliereInUseException.class, () -> filiereService.deleteById(1L));

        verify(filiereRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_unknownId_throwsNotFound() {
        when(filiereRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FiliereNotFoundException.class, () -> filiereService.deleteById(99L));
    }
}
