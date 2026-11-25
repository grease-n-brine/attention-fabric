package greasenbrine.fabric.visualization;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FrameSequenceRecorderTest {

    @TempDir
    Path tempDir;

    @Test
    void cameraCompletesOneOrbit() {
        Camera start = FrameSequenceRecorder.cameraAt(0, 100);
        Camera half = FrameSequenceRecorder.cameraAt(50, 100);
        assertEquals(0.0, start.yaw(), 1e-12);
        assertEquals(Math.PI, half.yaw(), 1e-12);
        assertEquals(FrameSequenceRecorder.WIDTH, start.width());
        assertEquals(FrameSequenceRecorder.HEIGHT, start.height());
    }

    @Test
    void fabricParametersEnableAllStructuralRules() {
        var p = FrameSequenceRecorder.fabricParameters();
        assertTrue(p.recruitmentEnabled());
        assertTrue(p.pruningEnabled());
        assertTrue(p.migrationEnabled());
        assertEquals(FrameSequenceRecorder.CAPACITY, p.capacity());
    }

    @Test
    void writesOneFramePerTickAndIsReproducible() throws IOException {
        Path a = tempDir.resolve("a");
        Path b = tempDir.resolve("b");
        int n = FrameSequenceRecorder.record(a, 3L);
        FrameSequenceRecorder.record(b, 3L);
        assertEquals(DemonstrationStimulus.TICKS, n);
        try (Stream<Path> files = Files.list(a)) {
            assertEquals(n, files.count());
        }
        Path first = a.resolve("frame_00000.png");
        Path last = a.resolve(String.format("frame_%05d.png", n - 1));
        assertTrue(Files.exists(first) && Files.exists(last));
        var imgA = ImageIO.read(a.resolve("frame_00400.png").toFile());
        var imgB = ImageIO.read(b.resolve("frame_00400.png").toFile());
        assertEquals(FrameSequenceRecorder.WIDTH, imgA.getWidth());
        assertArrayEquals(
            imgA.getRGB(0, 0, imgA.getWidth(), imgA.getHeight(), null, 0, imgA.getWidth()),
            imgB.getRGB(0, 0, imgB.getWidth(), imgB.getHeight(), null, 0, imgB.getWidth()));
    }
}
