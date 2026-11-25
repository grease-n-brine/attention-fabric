package greasenbrine.fabric.visualization;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed indexed palette built from the most frequent colors of a reference
 * image. Colors absent from the palette map to the nearest entry by Euclidean
 * distance in RGB space; no dithering is applied, which keeps flat regions flat
 * and compresses well under LZW.
 */
public final class Palette {

    public static final int MAX_SIZE = 256;

    private final int[] colors;
    private final IndexColorModel model;
    private final Map<Integer, Byte> cache = new HashMap<>();

    private Palette(int[] colors) {
        this.colors = colors;
        byte[] r = new byte[colors.length];
        byte[] g = new byte[colors.length];
        byte[] b = new byte[colors.length];
        for (int i = 0; i < colors.length; i++) {
            r[i] = (byte) ((colors[i] >> 16) & 0xff);
            g[i] = (byte) ((colors[i] >> 8) & 0xff);
            b[i] = (byte) (colors[i] & 0xff);
        }
        this.model = new IndexColorModel(8, colors.length, r, g, b);
    }

    /** Builds a palette of at most {@code size} entries from the most frequent colors in {@code image}. */
    public static Palette fromImage(BufferedImage image, int size) {
        if (size < 2 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must lie in [2, " + MAX_SIZE + "]");
        }
        Map<Integer, Integer> counts = new HashMap<>();
        int w = image.getWidth();
        int h = image.getHeight();
        int[] row = new int[w];
        for (int y = 0; y < h; y++) {
            image.getRGB(0, y, w, 1, row, 0, w);
            for (int rgb : row) {
                counts.merge(rgb & 0xffffff, 1, Integer::sum);
            }
        }
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> {
            int c = Integer.compare(b.getValue(), a.getValue());
            return c != 0 ? c : Integer.compare(a.getKey(), b.getKey());
        });
        int n = Math.min(size, entries.size());
        int[] colors = new int[n];
        for (int i = 0; i < n; i++) {
            colors[i] = entries.get(i).getKey();
        }
        return new Palette(colors);
    }

    public int size() {
        return colors.length;
    }

    public int color(int index) {
        return colors[index];
    }

    public IndexColorModel colorModel() {
        return model;
    }

    /** Index of the palette entry nearest to {@code rgb}. */
    public int index(int rgb) {
        int key = rgb & 0xffffff;
        Byte cached = cache.get(key);
        if (cached != null) {
            return cached & 0xff;
        }
        int best = 0;
        long bestDistance = Long.MAX_VALUE;
        int r = (key >> 16) & 0xff;
        int g = (key >> 8) & 0xff;
        int b = key & 0xff;
        for (int i = 0; i < colors.length; i++) {
            int dr = r - ((colors[i] >> 16) & 0xff);
            int dg = g - ((colors[i] >> 8) & 0xff);
            int db = b - (colors[i] & 0xff);
            long d = (long) dr * dr + (long) dg * dg + (long) db * db;
            if (d < bestDistance) {
                bestDistance = d;
                best = i;
                if (d == 0) {
                    break;
                }
            }
        }
        cache.put(key, (byte) best);
        return best;
    }

    /** Converts {@code image} to an indexed image using this palette. */
    public BufferedImage apply(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, model);
        WritableRaster raster = out.getRaster();
        int[] row = new int[w];
        int[] indices = new int[w];
        for (int y = 0; y < h; y++) {
            image.getRGB(0, y, w, 1, row, 0, w);
            for (int x = 0; x < w; x++) {
                indices[x] = index(row[x]);
            }
            raster.setSamples(0, y, w, 1, 0, indices);
        }
        return out;
    }
}
