package utnfc.isi.back.sim.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CsvLegoRow {

    @CsvBindByName(column = "ages")
    private String ages;

    @CsvBindByName(column = "list_price")
    private Double listPrice;

    @CsvBindByName(column = "num_reviews")
    private Double numReviews;

    @CsvBindByName(column = "piece_count")
    private Double pieceCount;

    @CsvBindByName(column = "play_star_rating")
    private Double playStarRating;

    @CsvBindByName(column = "prod_desc")
    private String prodDesc;

    @CsvBindByName(column = "prod_id")
    private Double prodId;

    @CsvBindByName(column = "review_difficulty")
    private String reviewDifficulty;

    @CsvBindByName(column = "set_name")
    private String setName;

    @CsvBindByName(column = "star_rating")
    private Double starRating;

    @CsvBindByName(column = "theme_name")
    private String themeName;

    @CsvBindByName(column = "val_star_rating")
    private Double valStarRating;

    @CsvBindByName(column = "country")
    private String country;
}
