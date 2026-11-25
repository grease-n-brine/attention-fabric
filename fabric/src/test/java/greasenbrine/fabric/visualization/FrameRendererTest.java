package greasenbrine.fabric.visualization;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import greasenbrine.fabric.core.FabricParameters;
import greasenbrine.fabric.core.SilkFabric;
import greasenbrine.fabric.core.SilkParameters;

class FrameRendererTest {

    private static final Camera CAMERA = new Camera(0.3, 0.2, 7.0, 320.0, 320, 180);

    private static FrameRenderer.SignalWindow window(int n) {
        double[] v = new double[n];
        boolean[] p = new boolean[n];
        for (int i = 0; i < n; i++) {
            v[i] = Math.sin(i / 5.0);
        }
        p[n / 2] = true;
        return new FrameRenderer.SignalWindow(v, p);
    }

    private static SilkFabric fabric(long seed) {
        SilkFabric f = new SilkFabric(FabricParameters.defaults(12), SilkParameters.defaults(), seed);
        for (int i = 0; i < 30; i++) {
            f.step(i % 7 == 0 ? 3.0 : Math.sin(i / 5.0));
        }
        return f;
    }

    @Test
    void producesImageOfCameraSize() {
        BufferedImage img = new FrameRenderer(CAMERA, 2.0, 3.0).render(fabric(1L), 30, window(40));
        assertEquals(320, img.getWidth());
        assertEquals(180, img.getHeight());
    }

    @Test
    void imageIsNotUniform() {
        BufferedImage img = new FrameRenderer(CAMERA, 2.0, 3.0).render(fabric(1L), 30, window(40));
        int first = img.getRGB(0, 0);
        boolean differs = false;
        for (int y = 0; y < img.getHeight() && !differs; y += 3) {
            for (int x = 0; x < img.getWidth(); x += 3) {
                if (img.getRGB(x, y) != first) {
                    differs = true;
                    break;
                }
            }
        }
        assertTrue(differs);
    }

    @Test
    void renderingIsDeterministic() {
        FrameRenderer r = new FrameRenderer(CAMERA, 2.0, 3.0);
        BufferedImage a = r.render(fabric(4L), 30, window(40));
        BufferedImage b = r.render(fabric(4L), 30, window(40));
        int[] pa = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0, a.getWidth());
        int[] pb = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0, b.getWidth());
        assertArrayEquals(pa, pb);
    }

    @Test
    void differentCameraYawChangesImage() {
        SilkFabric f = fabric(2L);
        BufferedImage a = new FrameRenderer(CAMERA, 2.0, 3.0).render(f, 30, window(40));
        BufferedImage b = new FrameRenderer(CAMERA.withYaw(1.5), 2.0, 3.0).render(f, 30, window(40));
        int[] pa = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0, a.getWidth());
        int[] pb = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0, b.getWidth());
        boolean differs = false;
        for (int i = 0; i < pa.length; i++) {
            if (pa[i] != pb[i]) {
                differs = true;
                break;
            }
        }
        assertTrue(differs);
    }
}
