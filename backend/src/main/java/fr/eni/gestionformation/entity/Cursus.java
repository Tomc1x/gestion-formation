package fr.eni.gestionformation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "cursusCours")
@EqualsAndHashCode(exclude = "cursusCours")
public class Cursus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @Builder.Default
    @OneToMany(mappedBy = "cursus")
    @OrderBy("ordre ASC")
    private List<CursusCours> cursusCours = new ArrayList<>();

}
