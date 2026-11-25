package greasenbrine.fabric.visualization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RecorderTest {

    @TempDir
    Path tempDir;

    @Test
    void frameSequenceRecorderWritesOnePngPerTick() throws IOException {
        Path dir = tempDir.resolve("frames");
        int n = FrameSequenceRecorder.record(dir, 1L);
        assertEquals(DemonstrationStimulus.TICKS, n);
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(n, files.filter(p -> p.toString().endsWith(".png")).count());
        }
        assertTrue(Files.exists(dir.resolve("frame_00000.png")));
        assertTrue(Files.exists(dir.resolve(String.format("frame_%05d.png", n - 1))));
    }

    @Test
    void animationRecorderWritesLoopingGifWithStridedFrames() throws IOException {
        Path file = tempDir.resolve("out/demo.gif");
        int n = AnimationRecorder.record(file, 1L);
        assertEquals(DemonstrationStimulus.TICKS / AnimationRecorder.STRIDE, n);
        byte[] bytes = Files.readAllBytes(file);
        assertEquals("GIF89a", new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII));
        assertTrue(new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1).contains("NETSCAPE2.0"));
    }
}
