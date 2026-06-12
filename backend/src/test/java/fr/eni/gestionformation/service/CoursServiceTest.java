package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.exception.CycleDetectedException;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import fr.eni.gestionformation.repository.CoursRepository;
import fr.eni.gestionformation.repository.CursusCoursRepository;
import fr.eni.gestionformation.repository.InscriptionCoursRepository;
import fr.eni.gestionformation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoursServiceTest {

    @Mock
    CoursRepository coursRepository;

    @Mock
    @SuppressWarnings("unused")
    UserRepository userRepository;

    @Mock
    @SuppressWarnings("unused")
    CursusCoursRepository cursusCoursRepository;

    @Mock
    @SuppressWarnings("unused")
    CoursPlanifieRepository coursPlanifieRepository;

    @Mock
    @SuppressWarnings("unused")
    InscriptionCoursRepository inscriptionCoursRepository;

    @InjectMocks
    CoursService coursService;

    @Test
    void updateNomEtDuree_modifieNomEtDuree() {
        Cours cours = Cours.builder().id(1L).name("Ancien nom").dureeJours(2).build();
        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));
        when(coursRepository.save(any(Cours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cours result = coursService.updateNomEtDuree(1L, "Nouveau nom", 5);

        assertThat(result.getName()).isEqualTo("Nouveau nom");
        assertThat(result.getDureeJours()).isEqualTo(5);
    }

    @Test
    void setPrerequis_selfPrerequisite_throwsCycleDetected() {
        Cours coursA = Cours.builder().id(1L).name("A").build();
        when(coursRepository.findById(1L)).thenReturn(Optional.of(coursA));
        when(coursRepository.findAllById(List.of(1L))).thenReturn(List.of(coursA));

        assertThrows(CycleDetectedException.class, () -> coursService.setPrerequis(1L, List.of(1L)));
    }

    @Test
    void setPrerequis_directCycle_throwsCycleDetected() {
        // B already has A as prerequis. Trying to set A's prerequis to B should create a cycle A->B->A
        Cours coursA = Cours.builder().id(1L).name("A").build();
        Cours coursB = Cours.builder().id(2L).name("B").build();
        coursB.setPrerequis(new HashSet<>(Set.of(coursA)));

        when(coursRepository.findById(1L)).thenReturn(Optional.of(coursA));
        when(coursRepository.findAllById(List.of(2L))).thenReturn(List.of(coursB));
        when(coursRepository.findById(2L)).thenReturn(Optional.of(coursB));

        assertThrows(CycleDetectedException.class, () -> coursService.setPrerequis(1L, List.of(2L)));
    }

    @Test
    void setPrerequis_indirectCycle_throwsCycleDetected() {
        // B requires C, C requires A. Trying to set A's prerequis to B creates A->B->C->A
        Cours coursA = Cours.builder().id(1L).name("A").build();
        Cours coursB = Cours.builder().id(2L).name("B").build();
        Cours coursC = Cours.builder().id(3L).name("C").build();

        coursC.setPrerequis(new HashSet<>(Set.of(coursA)));
        coursB.setPrerequis(new HashSet<>(Set.of(coursC)));

        when(coursRepository.findById(1L)).thenReturn(Optional.of(coursA));
        when(coursRepository.findAllById(List.of(2L))).thenReturn(List.of(coursB));
        when(coursRepository.findById(2L)).thenReturn(Optional.of(coursB));
        when(coursRepository.findById(3L)).thenReturn(Optional.of(coursC));

        assertThrows(CycleDetectedException.class, () -> coursService.setPrerequis(1L, List.of(2L)));
    }

    @Test
    void setPrerequis_validPrerequis_savesCours() {
        Cours coursA = Cours.builder().id(1L).name("A").build();
        Cours coursB = Cours.builder().id(2L).name("B").build();

        when(coursRepository.findById(1L)).thenReturn(Optional.of(coursA));
        when(coursRepository.findAllById(List.of(2L))).thenReturn(List.of(coursB));
        lenient().when(coursRepository.findById(2L)).thenReturn(Optional.of(coursB));
        when(coursRepository.save(any(Cours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cours result = coursService.setPrerequis(1L, List.of(2L));

        assertThat(result.getPrerequis()).containsExactly(coursB);
    }
}
