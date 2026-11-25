package greasenbrine.fabric.visualization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import greasenbrine.fabric.structures.Point;
import org.junit.jupiter.api.Test;

class CameraTest {

    private static final Camera CAMERA = new Camera(0.0, 0.0, 6.0, 400.0, 800, 600);

    @Test
    void originProjectsToImageCenter() {
        Camera.Projected p = CAMERA.project(new Point(new double[]{0.0, 0.0, 0.0}));
        assertEquals(400.0, p.x(), 1e-9);
        assertEquals(300.0, p.y(), 1e-9);
        assertEquals(6.0, p.depth(), 1e-9);
    }

    @Test
    void positiveXMovesRightAndPositiveYMovesUp() {
        Camera.Projected p = CAMERA.project(new Point(new double[]{1.0, 1.0, 0.0}));
        assertTrue(p.x() > 400.0);
        assertTrue(p.y() < 300.0);
    }

    @Test
    void nearerPointsAppearLarger() {
        Camera.Projected near = CAMERA.project(new Point(new double[]{1.0, 0.0, -2.0}));
        Camera.Projected far = CAMERA.project(new Point(new double[]{1.0, 0.0, 2.0}));
        assertTrue(near.x() - 400.0 > far.x() - 400.0);
        assertTrue(near.depth() < far.depth());
    }

    @Test
    void quarterTurnYawSwapsAxes() {
        Camera turned = CAMERA.withYaw(Math.PI / 2.0);
        Camera.Projected p = turned.project(new Point(new double[]{0.0, 0.0, 1.0}));
        Camera.Projected q = CAMERA.project(new Point(new double[]{1.0, 0.0, 0.0}));
        assertEquals(q.x(), p.x(), 1e-9);
        assertEquals(q.depth(), p.depth(), 1e-9);
    }

    @Test
    void rejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new Camera(0, 0, 0.0, 400, 800, 600));
        assertThrows(IllegalArgumentException.class, () -> new Camera(0, 0, 6.0, 400, 0, 600));
    }
}
