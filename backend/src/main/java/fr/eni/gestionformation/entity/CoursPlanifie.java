package fr.eni.gestionformation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"promotion", "cours", "formateur"})
@EqualsAndHashCode(exclude = {"promotion", "cours", "formateur"})
public class CoursPlanifie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "promotion_id", nullable = true)
    private Promotion promotion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private int ordre;

    @Enumerated(EnumType.STRING)
    private CoursPlanifieStatut statut;

    @ManyToOne(optional = true)
    @JoinColumn(name = "formateur_id", nullable = true)
    private User formateur;

    @Column(nullable = true)
    private String salle;

}
