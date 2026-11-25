package greasenbrine.fabric.visualization;

import greasenbrine.fabric.core.FabricParameters;
import greasenbrine.fabric.core.SilkFabric;
import greasenbrine.fabric.core.SilkParameters;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Drives a fabric with the demonstration stimulus and writes one PNG frame per
 * tick. The camera completes one orbit over the recording.
 *
 * <p>Usage: {@code FrameSequenceRecorder <output-directory> [seed]}</p>
 */
public final class FrameSequenceRecorder {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final int CAPACITY = 48;
    public static final double BOUND = 2.0;
    public static final double MIGRATION_STEP = 0.03;
    public static final double EXCITATION_SCALE = 3.0;
    public static final int WINDOW = 200;

    private FrameSequenceRecorder() {
    }

    public static FabricParameters fabricParameters() {
        return new FabricParameters(CAPACITY, 3, true, true, 0.05, 0.05, true, MIGRATION_STEP, BOUND, false);
    }

    public static Camera cameraAt(int tick, int totalTicks) {
        double yaw = 2.0 * Math.PI * tick / totalTicks;
        return new Camera(yaw, 0.35, 7.5, 420.0, WIDTH, HEIGHT);
    }

    public static int record(Path directory, long seed) throws IOException {
        Files.createDirectories(directory);
        DemonstrationStimulus stimulus = new DemonstrationStimulus(seed);
        SilkFabric fabric = new SilkFabric(fabricParameters(), SilkParameters.defaults(), seed);

        for (int t = 0; t < stimulus.length(); t++) {
            fabric.step(stimulus.value(t));
            FrameRenderer renderer = new FrameRenderer(cameraAt(t, stimulus.length()), BOUND, EXCITATION_SCALE);
            BufferedImage frame = renderer.render(fabric, t + 1, stimulus.window(t, WINDOW));
            Path file = directory.resolve(String.format(Locale.ROOT, "frame_%05d.png", t));
            ImageIO.write(frame, "png", file.toFile());
        }
        return stimulus.length();
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length > 0 ? args[0] : "media/frames");
        long seed = args.length > 1 ? Long.parseLong(args[1]) : 0L;
        int frames = record(output, seed);
        System.out.println(String.format(Locale.ROOT, "wrote %d frames to %s", frames, output.toAbsolutePath()));
    }
}
