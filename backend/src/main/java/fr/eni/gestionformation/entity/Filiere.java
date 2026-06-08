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
@ToString(exclude = "cursus")
@EqualsAndHashCode(exclude = "cursus")
public class Filiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "filiere")
    private List<Cursus> cursus = new ArrayList<>();

}
