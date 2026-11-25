package greasenbrine.fabric.visualization;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DemonstrationTest {

    @TempDir
    Path tempDir;

    @Test
    void cameraCompletesOneOrbitAndScalesWithWidth() {
        Camera start = Demonstration.cameraAt(0, 100, 1280, 720);
        Camera half = Demonstration.cameraAt(50, 100, 1280, 720);
        Camera small = Demonstration.cameraAt(0, 100, 640, 360);
        assertEquals(0.0, start.yaw(), 1e-12);
        assertEquals(Math.PI, half.yaw(), 1e-12);
        assertEquals(start.focal() / 2.0, small.focal(), 1e-12);
    }

    @Test
    void fabricParametersEnableAllStructuralRules() {
        var p = Demonstration.fabricParameters();
        assertTrue(p.recruitmentEnabled());
        assertTrue(p.pruningEnabled());
        assertTrue(p.migrationEnabled());
        assertEquals(Demonstration.CAPACITY, p.capacity());
    }

    @Test
    void strideControlsDeliveredFrameCount() throws IOException {
        List<Integer> ticks = new ArrayList<>();
        int n = Demonstration.run(0L, 160, 90, 60, (tick, frame) -> ticks.add(tick));
        assertEquals(DemonstrationStimulus.TICKS / 60, n);
        assertEquals(n, ticks.size());
        assertEquals(0, ticks.get(0));
        assertEquals(60, ticks.get(1));
        assertThrows(IllegalArgumentException.class,
            () -> Demonstration.run(0L, 160, 90, 0, (tick, frame) -> { }));
    }

    @Test
    void frameSequenceIsReproducible() throws IOException {
        Path a = tempDir.resolve("a");
        Path b = tempDir.resolve("b");
        Files.createDirectories(a);
        Files.createDirectories(b);
        Demonstration.run(3L, 320, 180, 90, (tick, frame) ->
            ImageIO.write(frame, "png", a.resolve(tick + ".png").toFile()));
        Demonstration.run(3L, 320, 180, 90, (tick, frame) ->
            ImageIO.write(frame, "png", b.resolve(tick + ".png").toFile()));
        try (Stream<Path> files = Files.list(a)) {
            assertEquals(DemonstrationStimulus.TICKS / 90, files.count());
        }
        BufferedImage ia = ImageIO.read(a.resolve("450.png").toFile());
        BufferedImage ib = ImageIO.read(b.resolve("450.png").toFile());
        assertEquals(320, ia.getWidth());
        assertArrayEquals(
            ia.getRGB(0, 0, ia.getWidth(), ia.getHeight(), null, 0, ia.getWidth()),
            ib.getRGB(0, 0, ib.getWidth(), ib.getHeight(), null, 0, ib.getWidth()));
    }

    @Test
    void pulsesRecruitUnitsAndPoolRemainsBounded() throws IOException {
        int[] attachedBefore = new int[1];
        int[] attachedAfter = new int[1];
        greasenbrine.fabric.core.SilkFabric fabric = new greasenbrine.fabric.core.SilkFabric(
            Demonstration.fabricParameters(), greasenbrine.fabric.core.SilkParameters.defaults(), 0L);
        DemonstrationStimulus stimulus = new DemonstrationStimulus(0L);
        for (int t = 0; t < stimulus.length(); t++) {
            fabric.step(stimulus.value(t));
            if (t == DemonstrationStimulus.ISOLATED_PULSE_TICK - 1) {
                attachedBefore[0] = fabric.activeCount();
            }
            if (t == DemonstrationStimulus.ISOLATED_PULSE_TICK + 5) {
                attachedAfter[0] = fabric.activeCount();
            }
        }
        assertTrue(attachedBefore[0] < Demonstration.CAPACITY / 4, "background alone attached " + attachedBefore[0]);
        assertTrue(attachedAfter[0] > attachedBefore[0]);
        assertTrue(fabric.activeCount() <= Demonstration.CAPACITY);
    }
}
