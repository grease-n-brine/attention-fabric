package greasenbrine.fabric.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PaletteTest {

    private static BufferedImage swatch() {
        BufferedImage img = new BufferedImage(40, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 20, 10);
        g.setColor(Color.RED);
        g.fillRect(20, 0, 15, 10);
        g.setColor(Color.BLUE);
        g.fillRect(35, 0, 5, 10);
        g.dispose();
        return img;
    }

    @Test
    void ordersEntriesByFrequency() {
        Palette p = Palette.fromImage(swatch(), 256);
        assertEquals(3, p.size());
        assertEquals(0x000000, p.color(0));
        assertEquals(0xff0000, p.color(1));
        assertEquals(0x0000ff, p.color(2));
    }

    @Test
    void truncatesToRequestedSize() {
        Palette p = Palette.fromImage(swatch(), 2);
        assertEquals(2, p.size());
        assertEquals(0x000000, p.color(0));
        assertEquals(0xff0000, p.color(1));
    }

    @Test
    void mapsUnknownColorsToNearestEntry() {
        Palette p = Palette.fromImage(swatch(), 256);
        assertEquals(1, p.index(0xee1010));
        assertEquals(2, p.index(0x1010ee));
        assertEquals(0, p.index(0x101010));
    }

    @Test
    void applyPreservesExactColors() {
        Palette p = Palette.fromImage(swatch(), 256);
        BufferedImage indexed = p.apply(swatch());
        assertEquals(BufferedImage.TYPE_BYTE_INDEXED, indexed.getType());
        assertEquals(0xff000000, indexed.getRGB(5, 5));
        assertEquals(0xffff0000, indexed.getRGB(25, 5));
        assertEquals(0xff0000ff, indexed.getRGB(37, 5));
    }

    @Test
    void rejectsInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> Palette.fromImage(swatch(), 1));
        assertThrows(IllegalArgumentException.class, () -> Palette.fromImage(swatch(), 257));
    }

    @Test
    void handlesImagesWithMoreColorsThanEntries() {
        BufferedImage gradient = new BufferedImage(256, 1, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 256; x++) {
            gradient.setRGB(x, 0, (x << 16) | (x << 8) | x);
        }
        Palette p = Palette.fromImage(gradient, 16);
        assertEquals(16, p.size());
        BufferedImage out = p.apply(gradient);
        for (int x = 0; x < 256; x++) {
            int rgb = out.getRGB(x, 0) & 0xffffff;
            boolean inPalette = false;
            for (int i = 0; i < p.size(); i++) {
                if (p.color(i) == rgb) {
                    inPalette = true;
                }
            }
            assertTrue(inPalette);
        }
    }
}
