package greasenbrine.fabric.visualization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Writes the demonstration as one PNG per tick at full resolution.
 *
 * <p>Usage: {@code FrameSequenceRecorder <output-directory> [seed]}</p>
 */
public final class FrameSequenceRecorder {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private FrameSequenceRecorder() {
    }

    public static int record(Path directory, long seed) throws IOException {
        Files.createDirectories(directory);
        return Demonstration.run(seed, WIDTH, HEIGHT, 1, (tick, frame) -> {
            Path file = directory.resolve(String.format(Locale.ROOT, "frame_%05d.png", tick));
            ImageIO.write(frame, "png", file.toFile());
        });
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length > 0 ? args[0] : "media/frames");
        long seed = args.length > 1 ? Long.parseLong(args[1]) : 0L;
        int frames = record(output, seed);
        System.out.println(String.format(Locale.ROOT, "wrote %d frames to %s", frames, output.toAbsolutePath()));
    }
}
