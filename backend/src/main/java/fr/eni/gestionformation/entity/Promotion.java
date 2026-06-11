package fr.eni.gestionformation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "rythme")
@EqualsAndHashCode(exclude = "rythme")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "cursus_id")
    private Cursus cursus;

    private LocalDate dateDebut;

    @OneToOne(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Rythme rythme;

}
