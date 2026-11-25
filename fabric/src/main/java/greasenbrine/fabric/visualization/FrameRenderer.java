package greasenbrine.fabric.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import greasenbrine.fabric.core.Silk;
import greasenbrine.fabric.core.SilkFabric;
import greasenbrine.fabric.structures.Point;

/**
 * Renders the state of a {@link SilkFabric} to an image: the bounding cube of
 * the state space, every unit as a disc whose radius and color follow its
 * excitation, directed connections as lines from parent to child, and a panel
 * showing the recent input signal.
 */
public final class FrameRenderer {

    private static final Color BACKGROUND = new Color(0x10, 0x12, 0x18);
    private static final Color CUBE = new Color(0x3a, 0x40, 0x50);
    private static final Color EDGE = new Color(0x7a, 0x86, 0xa0, 0xb0);
    private static final Color DORMANT = new Color(0x4a, 0x50, 0x60);
    private static final Color CALM = new Color(0x3c, 0x8c, 0xd8);
    private static final Color MID = new Color(0xe8, 0xd8, 0x4a);
    private static final Color EXCITED = new Color(0xf0, 0x5a, 0x28);
    private static final Color FIRED = Color.WHITE;
    private static final Color TEXT = new Color(0xd8, 0xdc, 0xe4);
    private static final Color SIGNAL = new Color(0x9a, 0xd0, 0x8a);
    private static final Color PULSE = new Color(0xe8, 0x50, 0x50);

    private final Camera camera;
    private final double bound;
    private final double excitationScale;

    public FrameRenderer(Camera camera, double bound, double excitationScale) {
        this.camera = camera;
        this.bound = bound;
        this.excitationScale = excitationScale;
    }

    /** Signal history to display beneath the scene; the last element is the current input. */
    public record SignalWindow(double[] values, boolean[] pulses) {
    }

    public BufferedImage render(SilkFabric fabric, long tick, SignalWindow window) {
        BufferedImage image = new BufferedImage(camera.width(), camera.height(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, camera.width(), camera.height());

        drawCube(g);
        drawEdges(g, fabric);
        drawUnits(g, fabric);
        drawHud(g, fabric, tick, window);

        g.dispose();
        return image;
    }

    private void drawCube(Graphics2D g) {
        double b = bound;
        double[][] corners = {
            {-b, -b, -b}, {b, -b, -b}, {b, b, -b}, {-b, b, -b},
            {-b, -b, b}, {b, -b, b}, {b, b, b}, {-b, b, b}
        };
        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        g.setColor(CUBE);
        g.setStroke(new BasicStroke(1.0f));
        for (int[] e : edges) {
            Camera.Projected a = camera.project(new Point(corners[e[0]]));
            Camera.Projected c = camera.project(new Point(corners[e[1]]));
            g.draw(new Line2D.Double(a.x(), a.y(), c.x(), c.y()));
        }
    }

    private void drawEdges(Graphics2D g, SilkFabric fabric) {
        g.setColor(EDGE);
        g.setStroke(new BasicStroke(1.2f));
        for (Silk parent : fabric.getUnits()) {
            if (!fabric.isAttached(parent.getId())) {
                continue;
            }
            Camera.Projected a = camera.project(parent.getPosition());
            for (Silk child : parent.getTeeth()) {
                Camera.Projected c = camera.project(child.getPosition());
                g.draw(new Line2D.Double(a.x(), a.y(), c.x(), c.y()));
            }
        }
    }

    private record Drawable(Silk unit, Camera.Projected projected, boolean attached, boolean fired) {
    }

    private void drawUnits(Graphics2D g, SilkFabric fabric) {
        List<Silk> fired = fabric.getFiredThisTick();
        List<Drawable> drawables = new ArrayList<>();
        for (Silk unit : fabric.getUnits()) {
            drawables.add(new Drawable(unit, camera.project(unit.getPosition()),
                fabric.isAttached(unit.getId()), fired.contains(unit)));
        }
        drawables.sort(Comparator.comparingDouble((Drawable d) -> d.projected().depth()).reversed());

        for (Drawable d : drawables) {
            double scale = camera.scaleAt(d.projected().depth()) / camera.scaleAt(camera.distance());
            double radius;
            Color fill;
            if (d.attached()) {
                double level = Math.min(1.0, d.unit().getExcitation() / excitationScale);
                radius = (4.0 + 9.0 * level) * scale;
                fill = level < 0.5 ? blend(CALM, MID, level * 2.0) : blend(MID, EXCITED, (level - 0.5) * 2.0);
            } else {
                radius = 2.5 * scale;
                fill = DORMANT;
            }
            Ellipse2D disc = new Ellipse2D.Double(
                d.projected().x() - radius, d.projected().y() - radius, 2 * radius, 2 * radius);
            g.setColor(fill);
            g.fill(disc);
            if (d.fired()) {
                g.setColor(FIRED);
                g.setStroke(new BasicStroke(1.8f));
                double r = radius + 3.0 * scale;
                g.draw(new Ellipse2D.Double(d.projected().x() - r, d.projected().y() - r, 2 * r, 2 * r));
            }
            if (d.unit().getId() == 0) {
                g.setColor(TEXT);
                g.setStroke(new BasicStroke(1.0f));
                double r = radius + 1.5;
                g.draw(new Ellipse2D.Double(d.projected().x() - r, d.projected().y() - r, 2 * r, 2 * r));
            }
        }
    }

    private void drawHud(Graphics2D g, SilkFabric fabric, long tick, SignalWindow window) {
        int w = camera.width();
        int h = camera.height();
        int margin = 16;

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g.setColor(TEXT);
        double current = window.values()[window.values().length - 1];
        String line1 = String.format(Locale.ROOT, "tick %5d   input %+6.2f   attached %2d/%2d   fired %2d",
            tick, current, fabric.activeCount(), fabric.getParameters().capacity(), fabric.getFiredThisTick().size());
        g.drawString(line1, margin, margin + 14);
        Silk root = fabric.getRoot();
        String line2 = String.format(Locale.ROOT, "root  surprise %5.2f   excitation %5.2f   baseline %5.2f",
            Math.abs(current - root.getExpectedInput()), root.getExcitation(), root.getBaseline());
        g.drawString(line2, margin, margin + 34);

        int panelH = 70;
        int panelY = h - margin - panelH;
        int panelX = margin;
        int panelW = w - 2 * margin;
        g.setColor(new Color(0x1a, 0x1e, 0x28));
        g.fillRect(panelX, panelY, panelW, panelH);
        g.setColor(CUBE);
        g.drawRect(panelX, panelY, panelW, panelH);

        double[] values = window.values();
        boolean[] pulses = window.pulses();
        double min = -1.5;
        double max = 4.5;
        int n = values.length;
        Path2D path = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            double x = panelX + (double) i / Math.max(1, n - 1) * panelW;
            double y = panelY + panelH - (values[i] - min) / (max - min) * panelH;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            if (pulses[i]) {
                g.setColor(PULSE);
                g.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
            }
        }
        g.setColor(SIGNAL);
        g.setStroke(new BasicStroke(1.5f));
        g.draw(path);
    }

    private static Color blend(Color a, Color b, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return new Color(
            (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
            (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
