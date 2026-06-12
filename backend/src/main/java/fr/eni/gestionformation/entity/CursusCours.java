package fr.eni.gestionformation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cursus_id", "cours_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"cursus", "cours"})
@EqualsAndHashCode(exclude = {"cursus", "cours"})
public class CursusCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cursus_id")
    private Cursus cursus;

    @ManyToOne
    @JoinColumn(name = "cours_id")
    private Cours cours;

    private int ordre;
}
