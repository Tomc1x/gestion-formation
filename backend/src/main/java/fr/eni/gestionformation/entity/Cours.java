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
@ToString(exclude = {"cursus", "formateurs"})
@EqualsAndHashCode(exclude = {"cursus", "formateurs"})
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "cursus_id")
    private Cursus cursus;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "cours_formateurs",
        joinColumns = @JoinColumn(name = "cours_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> formateurs = new ArrayList<>();

}
