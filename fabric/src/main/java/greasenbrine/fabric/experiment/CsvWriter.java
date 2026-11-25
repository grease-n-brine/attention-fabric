package greasenbrine.fabric.experiment;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Minimal comma-separated writer producing locale-independent numeric output.
 */
public final class CsvWriter implements AutoCloseable {

    private final Writer writer;

    public CsvWriter(Path path, String... header) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        this.writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        row((Object[]) header);
    }

    public void row(Object... values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(format(values[i]));
        }
        sb.append('\n');
        writer.write(sb.toString());
    }

    private static String format(Object value) {
        if (value instanceof Double d) {
            return String.format(Locale.ROOT, "%.6f", d);
        }
        if (value instanceof Float f) {
            return String.format(Locale.ROOT, "%.6f", f);
        }
        return String.valueOf(value);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
