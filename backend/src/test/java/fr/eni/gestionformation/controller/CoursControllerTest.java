package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.CoursResponse;
import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.service.CoursService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoursControllerTest {

    @Mock
    CoursService coursService;

    @InjectMocks
    CoursController coursController;

    @Test
    void getById_withChainOfPrerequis_returnsRecursiveCoursResponse() {
        // Chain A -> B -> C (A requires B, B requires C, C has no prerequis)
        Cours coursC = Cours.builder().id(3L).name("C").build();
        Cours coursB = Cours.builder().id(2L).name("B").build();
        coursB.setPrerequis(new HashSet<>(Set.of(coursC)));
        Cours coursA = Cours.builder().id(1L).name("A").build();
        coursA.setPrerequis(new HashSet<>(Set.of(coursB)));

        when(coursService.findById(1L)).thenReturn(coursA);

        CoursResponse response = coursController.getById(1L).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPrerequis()).hasSize(1);

        CoursResponse bResponse = response.getPrerequis().getFirst();
        assertThat(bResponse.getId()).isEqualTo(2L);
        assertThat(bResponse.getPrerequis()).hasSize(1);

        CoursResponse cResponse = bResponse.getPrerequis().getFirst();
        assertThat(cResponse.getId()).isEqualTo(3L);
        assertThat(cResponse.getPrerequis()).isEmpty();
    }
}
