package fr.eni.gestionformation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "promotion")
@EqualsAndHashCode(exclude = "promotion")
public class Rythme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int semainesCentre;

    private int semainesEntreprise;

    @OneToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

}
