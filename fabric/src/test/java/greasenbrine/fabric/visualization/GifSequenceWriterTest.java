package greasenbrine.fabric.visualization;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.junit.jupiter.api.Test;

class GifSequenceWriterTest {

    private static BufferedImage frame(int shade) {
        BufferedImage img = new BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(shade, shade / 2, 255 - shade));
        g.fillRect(0, 0, 32, 24);
        g.setColor(Color.WHITE);
        g.fillOval(shade / 16, 4, 8, 8);
        g.dispose();
        return img;
    }

    private static byte[] encode(int frames) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GifSequenceWriter w = new GifSequenceWriter(out, 5, true)) {
            for (int i = 0; i < frames; i++) {
                w.writeFrame(frame(i * 40));
            }
            assertEquals(frames, w.frameCount());
        }
        return out.toByteArray();
    }

    @Test
    void producesGif89aWithRequestedFrameCount() throws IOException {
        byte[] bytes = encode(5);
        assertEquals("GIF89a", new String(bytes, 0, 6, StandardCharsets.US_ASCII));

        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("gif");
        ImageReader reader = readers.next();
        try (ImageInputStream in = ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes))) {
            reader.setInput(in);
            assertEquals(5, reader.getNumImages(true));
            BufferedImage first = reader.read(0);
            assertEquals(32, first.getWidth());
            assertEquals(24, first.getHeight());
        } finally {
            reader.dispose();
        }
    }

    @Test
    void containsLoopExtension() throws IOException {
        byte[] bytes = encode(2);
        String text = new String(bytes, StandardCharsets.ISO_8859_1);
        assertTrue(text.contains("NETSCAPE2.0"));
    }

    @Test
    void encodingIsDeterministic() throws IOException {
        assertArrayEquals(encode(3), encode(3));
    }

    @Test
    void rejectsNonPositiveDelay() {
        assertThrows(IllegalArgumentException.class,
            () -> new GifSequenceWriter(new ByteArrayOutputStream(), 0, true));
    }

    @Test
    void fixedPaletteProducesSmallerOutputForFlatFrames() throws IOException {
        ByteArrayOutputStream dithered = new ByteArrayOutputStream();
        ByteArrayOutputStream fixed = new ByteArrayOutputStream();
        try (GifSequenceWriter a = new GifSequenceWriter(dithered, 5, true);
             GifSequenceWriter b = new GifSequenceWriter(fixed, 5, true, 64)) {
            for (int i = 0; i < 4; i++) {
                a.writeFrame(frame(i * 40));
                b.writeFrame(frame(i * 40));
            }
        }
        assertTrue(fixed.size() < dithered.size(), fixed.size() + " vs " + dithered.size());
    }

    @Test
    void rejectsInvalidPaletteSize() {
        assertThrows(IllegalArgumentException.class,
            () -> new GifSequenceWriter(new ByteArrayOutputStream(), 5, true, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new GifSequenceWriter(new ByteArrayOutputStream(), 5, true, 300));
    }
}
