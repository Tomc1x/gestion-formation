package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.exception.CursusNotFoundException;
import fr.eni.gestionformation.repository.CursusCoursRepository;
import fr.eni.gestionformation.repository.CursusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursusServiceTest {

    @Mock
    CursusRepository cursusRepository;

    @Mock
    CursusCoursRepository cursusCoursRepository;

    @Mock
    CoursService coursService;

    @Mock
    PromotionService promotionService;

    @InjectMocks
    CursusService cursusService;

    @Test
    void update_validNameAndFiliere_updatesCursus() {
        Filiere filiere = Filiere.builder().id(1L).name("Developpement").build();
        Cursus cursus = Cursus.builder().id(1L).name("Ancien nom").filiere(null).build();
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(cursusRepository.findByName("Nouveau nom")).thenReturn(Optional.empty());
        when(cursusRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cursus result = cursusService.update(1L, "Nouveau nom", 1L, filiere);

        assertThat(result.getName()).isEqualTo("Nouveau nom");
        assertThat(result.getFiliere()).isEqualTo(filiere);
    }

    @Test
    void update_nullFiliereId_clearsFiliere() {
        Filiere filiere = Filiere.builder().id(1L).name("Developpement").build();
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").filiere(filiere).build();
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(cursusRepository.findByName("Cursus")).thenReturn(Optional.of(cursus));
        when(cursusRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cursus result = cursusService.update(1L, "Cursus", null, null);

        assertThat(result.getFiliere()).isNull();
    }

    @Test
    void update_nameAlreadyUsedByAnotherCursus_throwsIllegalArgument() {
        Cursus cursus = Cursus.builder().id(1L).name("Ancien nom").build();
        Cursus autreCursus = Cursus.builder().id(2L).name("Nom pris").build();
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(cursusRepository.findByName("Nom pris")).thenReturn(Optional.of(autreCursus));

        assertThrows(IllegalArgumentException.class, () -> cursusService.update(1L, "Nom pris", null, null));
    }

    @Test
    void update_sameNameAsItself_doesNotThrow() {
        Cursus cursus = Cursus.builder().id(1L).name("Meme nom").build();
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(cursusRepository.findByName("Meme nom")).thenReturn(Optional.of(cursus));
        when(cursusRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cursus result = cursusService.update(1L, "Meme nom", null, null);

        assertThat(result.getName()).isEqualTo("Meme nom");
    }

    @Test
    void update_unknownId_throwsNotFound() {
        when(cursusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CursusNotFoundException.class, () -> cursusService.update(99L, "Nom", null, null));
    }
}
