package greasenbrine.fabric.visualization;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Writes the demonstration as a looping animated GIF.
 *
 * <p>Usage: {@code AnimationRecorder <output-file> [seed]}</p>
 */
public final class AnimationRecorder {

    public static final int WIDTH = 640;
    public static final int HEIGHT = 360;
    public static final int STRIDE = 2;
    public static final int DELAY_CENTISECONDS = 7;
    public static final int PALETTE_SIZE = 128;

    private AnimationRecorder() {
    }

    public static int record(Path file, long seed) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file));
             GifSequenceWriter gif = new GifSequenceWriter(out, DELAY_CENTISECONDS, true, PALETTE_SIZE)) {
            return Demonstration.run(seed, WIDTH, HEIGHT, STRIDE, (tick, frame) -> gif.writeFrame(frame));
        }
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length > 0 ? args[0] : "media/attention-fabric.gif");
        long seed = args.length > 1 ? Long.parseLong(args[1]) : 0L;
        int frames = record(output, seed);
        System.out.println(String.format(Locale.ROOT, "wrote %d frames to %s (%d bytes)",
            frames, output.toAbsolutePath(), Files.size(output)));
    }
}
