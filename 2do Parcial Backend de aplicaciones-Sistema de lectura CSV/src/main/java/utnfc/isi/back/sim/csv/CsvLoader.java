package utnfc.isi.back.sim.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvLoader {
    private CsvLoader() {}

    public static List<CsvLegoRow> read(String path) throws Exception {
        try (var fr = new FileReader(path, StandardCharsets.UTF_8)) {

            var parser = new CSVParserBuilder()
                    .withSeparator(';')             // separador correcto
                    .withQuoteChar('"')             // permite comillas en campos
                    .withIgnoreQuotations(false)    // no ignorar las comillas
                    .build();

            var reader = new CSVReaderBuilder(fr)
                    .withCSVParser(parser)
                    .build();

            return new CsvToBeanBuilder<CsvLegoRow>(reader)
                    .withType(CsvLegoRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build()
                    .parse();
        }
    }
}
