package utnfc.isi.back.sim.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "LEGO_SETS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegoSet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_set")
    @SequenceGenerator(name = "seq_set", sequenceName = "SEQ_LEGO_SET_ID", allocationSize = 1)
    @Column(name = "ID_SET")
    private Integer id;

    @Column(name = "PROD_ID", nullable = false)
    private Integer prodId;

    @Column(name = "SET_NAME", nullable = false, length = 200)
    private String setName;

    @Column(name = "PROD_DESC", length = 2048)
    private String prodDesc;

    @Column(name = "REVIEW_DIFFICULTY", length = 32)
    private String reviewDifficulty;

    @Column(name = "PIECE_COUNT")
    private Integer pieceCount;

    @Column(name = "STAR_RATING")
    private Double starRating; // DECIMAL(3,1) → Double funciona sin problemas en H2

    @Column(name = "LIST_PRICE", precision = 10, scale = 2)
    private BigDecimal listPrice;

    @ManyToOne(optional = false)
    @JoinColumn(name = "THEME_ID", nullable = false)
    private Theme theme;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AGE_GROUP_ID", nullable = false)
    private AgeGroup ageGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "COUNTRY_ID", nullable = false)
    private Country country;
}
