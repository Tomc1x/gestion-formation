package fr.eni.gestionformation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"formateurs", "prerequis"})
@EqualsAndHashCode(exclude = {"formateurs", "prerequis"})
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "cours_formateurs",
        joinColumns = @JoinColumn(name = "cours_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> formateurs = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "cours_prerequis",
        joinColumns = @JoinColumn(name = "cours_id"),
        inverseJoinColumns = @JoinColumn(name = "prerequis_id")
    )
    private Set<Cours> prerequis = new HashSet<>();

}
