package greasenbrine.fabric.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExperimentUtilitiesTest {

    @TempDir
    Path tempDir;

    @Test
    void csvWriterProducesHeaderAndFormattedRows() throws IOException {
        Path file = tempDir.resolve("nested/out.csv");
        try (CsvWriter csv = new CsvWriter(file, "a", "b", "c")) {
            csv.row(1, 2.5, "x");
            csv.row(3L, 0.1234567, true);
        }
        List<String> lines = Files.readAllLines(file);
        assertEquals(List.of("a,b,c", "1,2.500000,x", "3,0.123457,true"), lines);
    }

    @Test
    void statisticsMatchHandComputedValues() {
        double[] values = {2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0};
        assertEquals(5.0, Statistics.mean(values), 1e-12);
        assertEquals(2.138089935, Statistics.standardDeviation(values), 1e-9);
        assertEquals(0.0, Statistics.standardDeviation(new double[]{1.0}));
        assertTrue(Double.isNaN(Statistics.mean(new double[0])));
    }
}
