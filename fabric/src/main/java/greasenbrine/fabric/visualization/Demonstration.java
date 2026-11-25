package greasenbrine.fabric.visualization;

import java.awt.image.BufferedImage;
import java.io.IOException;

import greasenbrine.fabric.core.FabricParameters;
import greasenbrine.fabric.core.SilkFabric;
import greasenbrine.fabric.core.SilkParameters;

/**
 * Drives a fabric with the {@link DemonstrationStimulus} and renders frames
 * from an orbiting camera. Frames are delivered to a {@link FrameSink}, which
 * allows the same simulation to be written as a PNG sequence or an animation.
 */
public final class Demonstration {

    public static final int CAPACITY = 96;
    public static final double BOUND = 2.0;
    public static final double MIGRATION_STEP = 0.03;
    public static final double EXCITATION_SCALE = 3.0;
    public static final int WINDOW = 200;
    public static final int REFERENCE_WIDTH = 1280;
    public static final double REFERENCE_FOCAL = 560.0;

    private Demonstration() {
    }

    /** Receives rendered frames in tick order. */
    @FunctionalInterface
    public interface FrameSink {
        void accept(int tick, BufferedImage frame) throws IOException;
    }

    public static FabricParameters fabricParameters() {
        return new FabricParameters(CAPACITY, 3, true, true, 0.05, 0.05, true, MIGRATION_STEP, BOUND, false);
    }

    /** Camera for the given tick; the focal length scales with image width so framing is size independent. */
    public static Camera cameraAt(int tick, int totalTicks, int width, int height) {
        double yaw = 2.0 * Math.PI * tick / totalTicks;
        double focal = REFERENCE_FOCAL * width / REFERENCE_WIDTH;
        return new Camera(yaw, 0.35, 6.5, focal, width, height);
    }

    /**
     * Runs the simulation for every tick of the stimulus and renders every
     * {@code stride}-th tick at the given size.
     *
     * @return the number of frames delivered to the sink
     */
    public static int run(long seed, int width, int height, int stride, FrameSink sink) throws IOException {
        if (stride < 1) {
            throw new IllegalArgumentException("stride must be at least 1");
        }
        DemonstrationStimulus stimulus = new DemonstrationStimulus(seed);
        SilkFabric fabric = new SilkFabric(fabricParameters(), SilkParameters.defaults(), seed);
        int delivered = 0;
        for (int t = 0; t < stimulus.length(); t++) {
            fabric.step(stimulus.value(t));
            if (t % stride != 0) {
                continue;
            }
            Camera camera = cameraAt(t, stimulus.length(), width, height);
            FrameRenderer renderer = new FrameRenderer(camera, BOUND, EXCITATION_SCALE);
            sink.accept(t, renderer.render(fabric, t + 1, stimulus.window(t, WINDOW)));
            delivered++;
        }
        return delivered;
    }
}
