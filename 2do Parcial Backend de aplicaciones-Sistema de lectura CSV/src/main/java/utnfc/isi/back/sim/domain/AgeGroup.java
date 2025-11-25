package utnfc.isi.back.sim.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AGE_GROUPS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgeGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_age_group")
    @SequenceGenerator(name = "seq_age_group", sequenceName = "SEQ_AGE_GROUP_ID", allocationSize = 1)
    @Column(name = "ID_AGE_GROUP")
    private Integer id;

    @Column(name = "CODE", nullable = false, unique = true)
    private String code;

    // --- Campos derivados (no persistidos en BD)
    @Transient
    private Integer minAge;

    @Transient
    private Integer maxAge;

    @PostLoad
    @PostPersist
    private void deriveRange() {
        if (code == null) return;

        if (code.endsWith("+")) {
            minAge = Integer.parseInt(code.replace("+", ""));
            maxAge = null;
        } else if (code.contains("-")) {
            var parts = code.split("-");
            minAge = Integer.parseInt(parts[0]);
            maxAge = Integer.parseInt(parts[1]);
        } else {
            minAge = maxAge = Integer.parseInt(code);
        }
    }

    public boolean matchesAge(int age) {
        if (minAge == null && maxAge == null) return false;
        if (maxAge == null) return age >= minAge;
        if (minAge.equals(maxAge)) return age == minAge;
        return age >= minAge && age <= maxAge;
    }
}
