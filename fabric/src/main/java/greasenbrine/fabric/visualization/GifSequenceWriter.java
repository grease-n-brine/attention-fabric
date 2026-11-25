package greasenbrine.fabric.visualization;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/**
 * Writes an animated GIF using the image writer bundled with the JDK. Frames
 * are quantized to the default indexed palette before encoding.
 */
public final class GifSequenceWriter implements AutoCloseable {

    private final ImageWriter writer;
    private final ImageOutputStream output;
    private final IIOMetadata metadata;
    private final ImageWriteParam params;
    private int frames;

    /**
     * @param stream            destination stream, closed by {@link #close()}
     * @param delayCentiseconds display time per frame in hundredths of a second
     * @param loop              whether the animation repeats indefinitely
     */
    public GifSequenceWriter(OutputStream stream, int delayCentiseconds, boolean loop) throws IOException {
        if (delayCentiseconds < 1) {
            throw new IllegalArgumentException("delayCentiseconds must be at least 1");
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("gif");
        if (!writers.hasNext()) {
            throw new IOException("no GIF image writer available");
        }
        writer = writers.next();
        params = writer.getDefaultWriteParam();
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_INDEXED);
        metadata = writer.getDefaultImageMetadata(type, params);
        configure(metadata, delayCentiseconds, loop);

        output = ImageIO.createImageOutputStream(stream);
        writer.setOutput(output);
        writer.prepareWriteSequence(null);
    }

    public void writeFrame(BufferedImage frame) throws IOException {
        BufferedImage indexed = frame;
        if (frame.getType() != BufferedImage.TYPE_BYTE_INDEXED) {
            indexed = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
            Graphics2D g = indexed.createGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();
        }
        writer.writeToSequence(new IIOImage(indexed, null, metadata), params);
        frames++;
    }

    public int frameCount() {
        return frames;
    }

    @Override
    public void close() throws IOException {
        writer.endWriteSequence();
        output.close();
        writer.dispose();
    }

    private static void configure(IIOMetadata metadata, int delay, boolean loop) throws IOException {
        String format = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);

        IIOMetadataNode control = child(root, "GraphicControlExtension");
        control.setAttribute("disposalMethod", "none");
        control.setAttribute("userInputFlag", "FALSE");
        control.setAttribute("transparentColorFlag", "FALSE");
        control.setAttribute("delayTime", Integer.toString(delay));
        control.setAttribute("transparentColorIndex", "0");

        if (loop) {
            IIOMetadataNode extensions = child(root, "ApplicationExtensions");
            IIOMetadataNode netscape = new IIOMetadataNode("ApplicationExtension");
            netscape.setAttribute("applicationID", "NETSCAPE");
            netscape.setAttribute("authenticationCode", "2.0");
            netscape.setUserObject(new byte[]{1, 0, 0});
            extensions.appendChild(netscape);
        }

        metadata.setFromTree(format, root);
    }

    private static IIOMetadataNode child(IIOMetadataNode root, String name) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(name);
        root.appendChild(node);
        return node;
    }
}
