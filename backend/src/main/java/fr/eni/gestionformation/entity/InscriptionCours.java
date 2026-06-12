package fr.eni.gestionformation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cours_planifie_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"eleve", "coursPlanifie"})
@EqualsAndHashCode(exclude = {"eleve", "coursPlanifie"})
public class InscriptionCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User eleve;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cours_planifie_id", nullable = false)
    private CoursPlanifie coursPlanifie;

    @Builder.Default
    private LocalDate dateInscription = LocalDate.now();

}
