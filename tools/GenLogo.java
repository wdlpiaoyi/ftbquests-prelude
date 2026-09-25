import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Generates the mod logo (src/main/resources/logo.png), an original drawing: an open quest book in
 * front of a rising sun ("Prelude" = before the journey begins).
 *
 * <p>Run with any JDK 17+: {@code java tools/GenLogo.java src/main/resources/logo.png}
 */
public final class GenLogo {

    private GenLogo() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        int size = 128;
        // How far the centre (the gutter) sits below the outer page edges, for the top and bottom
        // edges. The centre being lower is what makes it read as an open book rather than a tent.
        int dipTop = args.length > 1 ? Integer.parseInt(args[1]) : 4;
        int dipBot = args.length > 2 ? Integer.parseInt(args[2]) : 9;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Shape badge = new RoundRectangle2D.Float(0.5f, 0.5f, size - 1f, size - 1f, 26f, 26f);
        g.setClip(badge);

        // Dawn sky.
        g.setPaint(new GradientPaint(0f, 0f, new Color(0x0E, 0x16, 0x2C),
                0f, size * 0.86f, new Color(0xC7, 0x5B, 0x3A)));
        g.fillRect(0, 0, size, size);

        int sunX = size / 2;
        int sunY = (int) (size * 0.80f);

        // Halo.
        g.setPaint(new RadialGradientPaint(new Point2D.Float(sunX, sunY), 48f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(0xFF, 0xD2, 0x8A, 210),
                        new Color(0xFF, 0xA0, 0x50, 80),
                        new Color(0xFF, 0xA0, 0x50, 0)}));
        g.fillRect(0, 0, size, size);

        // Rays.
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(0xFF, 0xE2, 0xA8, 110));
        for (int angle = 200; angle <= 340; angle += 20) {
            double r = Math.toRadians(angle);
            g.drawLine((int) (sunX + Math.cos(r) * 26), (int) (sunY + Math.sin(r) * 26),
                    (int) (sunX + Math.cos(r) * 40), (int) (sunY + Math.sin(r) * 40));
        }

        // Sun disc.
        g.setColor(new Color(0xFF, 0xE6, 0xA8));
        g.fill(new Ellipse2D.Float(sunX - 16f, sunY - 16f, 32f, 32f));

        // Ground.
        g.setColor(new Color(0x0B, 0x0F, 0x1C));
        g.fill(new Rectangle2D.Float(0f, size * 0.855f, size, size));
        g.setColor(new Color(0x35, 0x3F, 0x60));
        g.fill(new Rectangle2D.Float(0f, size * 0.855f, size, 1.5f));

        drawBook(g, size, dipTop, dipBot);

        // Stars.
        g.setColor(new Color(0xFF, 0xF3, 0xD0, 210));
        star(g, 30f, 30f, 2.0f);
        star(g, 97f, 24f, 1.6f);
        star(g, 109f, 47f, 1.2f);
        star(g, 22f, 57f, 1.2f);

        g.setClip(null);

        // Border.
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(new Color(0x08, 0x0A, 0x12));
        g.draw(badge);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0x6C, 0x7A, 0xB0, 140));
        g.draw(new RoundRectangle2D.Float(2.5f, 2.5f, size - 5f, size - 5f, 22f, 22f));

        g.dispose();

        File out = new File(args.length > 0 ? args[0] : "logo.png");
        ImageIO.write(image, "PNG", out);
        System.out.println("Wrote " + out.getAbsolutePath());
    }

    private static void drawBook(Graphics2D g, int size, int dipTop, int dipBot) {
        int cx = size / 2;
        int topY = 62;
        int botY = 98;

        int coverTop = topY - 3;
        int coverBot = botY + 3;
        int outerX = 44;

        int[] coverX = {cx - outerX - 2, cx, cx + outerX + 2, cx + outerX + 2, cx, cx - outerX - 2};
        int[] coverY = {coverTop, coverTop + dipTop, coverTop, coverBot, coverBot + dipBot, coverBot};
        Polygon cover = new Polygon(coverX, coverY, 6);

        // Drop shadow.
        g.setColor(new Color(0, 0, 0, 90));
        g.translate(0, 3);
        g.fill(cover);
        g.translate(0, -3);

        // Cover.
        g.setColor(new Color(0xC9, 0xA2, 0x2A));
        g.fill(cover);

        // Pages: the spine (centre) side dips down into the gutter.
        Polygon left = new Polygon(
                new int[]{cx - outerX, cx - 3, cx - 3, cx - outerX},
                new int[]{coverTop + 4, coverTop + 4 + dipTop, coverBot - 1 + dipBot, coverBot - 1}, 4);
        Polygon right = new Polygon(
                new int[]{cx + 3, cx + outerX, cx + outerX, cx + 3},
                new int[]{coverTop + 4 + dipTop, coverTop + 4, coverBot - 1, coverBot - 1 + dipBot}, 4);
        g.setColor(new Color(0xF6, 0xF2, 0xE7));
        g.fill(left);
        g.fill(right);

        // Spine.
        g.setColor(new Color(0x2B, 0x2F, 0x3E));
        g.fill(new Polygon(
                new int[]{cx - 2, cx + 2, cx + 2, cx - 2},
                new int[]{coverTop + 2 + dipTop, coverTop + 2 + dipTop,
                        coverBot + dipBot + 0, coverBot + dipBot + 0}, 4));

        // Text lines, following the page slope.
        g.setColor(new Color(0x9A, 0xA2, 0xB4));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 4; i++) {
            int yOuter = coverTop + 18 + i * 6;
            int yInner = yOuter + (int) (dipTop * 0.9);
            g.drawLine(cx - 34, yOuter, cx - 8, yInner);
            g.drawLine(cx + 8, yInner, cx + 34, yOuter);
        }

        // Outline.
        g.setColor(new Color(0x14, 0x17, 0x22));
        g.setStroke(new BasicStroke(2f));
        g.draw(cover);
    }

    private static void star(Graphics2D g, float x, float y, float r) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(x, y - 2 * r);
        path.lineTo(x + r, y - r);
        path.lineTo(x + 2 * r, y);
        path.lineTo(x + r, y + r);
        path.lineTo(x, y + 2 * r);
        path.lineTo(x - r, y + r);
        path.lineTo(x - 2 * r, y);
        path.lineTo(x - r, y - r);
        path.closePath();
        g.fill(path);
    }
}
