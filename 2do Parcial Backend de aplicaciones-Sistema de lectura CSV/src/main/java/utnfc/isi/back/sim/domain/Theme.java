package utnfc.isi.back.sim.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "THEMES")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_theme")
    @SequenceGenerator(name = "seq_theme", sequenceName = "SEQ_THEME_ID", allocationSize = 1)
    @Column(name = "ID_THEME")
    private Integer id;

    @Column(name = "NAME", nullable = false, unique = true, length = 120)
    private String name;
}
