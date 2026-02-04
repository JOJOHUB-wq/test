package ua.atherium.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

public class QuoteGenerator {

    public File generateQuote(BufferedImage avatar, String username, String text, String signature) {
        int width = 800;
        int padding = 40;
        int avatarSize = 100;

        int estimatedHeight = 300 + (text.length() / 50) * 30;

        BufferedImage image = new BufferedImage(width, estimatedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(new Color(20, 20, 20));
        g2d.fillRect(0, 0, width, estimatedHeight);

        if (avatar != null) {
            BufferedImage rounded = new BufferedImage(avatarSize, avatarSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rounded.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new Ellipse2D.Float(0, 0, avatarSize, avatarSize));
            g2.drawImage(avatar, 0, 0, avatarSize, avatarSize, null);
            g2.dispose();
            g2d.drawImage(rounded, padding, padding, null);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString(username, padding + avatarSize + 20, padding + 40);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2d.setColor(new Color(200, 200, 200));

        int x = padding + avatarSize + 20;
        int y = padding + 80;
        int maxWidth = width - x - padding;

        List<String> lines = wrapText(text, g2d.getFontMetrics(), maxWidth);
        for (String line : lines) {
            g2d.drawString(line, x, y);
            y += 30;
        }

        if (signature != null && !signature.isEmpty()) {
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 16));
            g2d.setColor(Color.GRAY);
            g2d.drawString(signature, width - padding - g2d.getFontMetrics().stringWidth(signature), y + 40);
            y += 40;
        }

        g2d.dispose();

        int finalHeight = y + padding;
        BufferedImage finalImage = image.getSubimage(0, 0, width, Math.min(finalHeight, estimatedHeight));

        try {
            File temp = File.createTempFile("quote_", ".png");
            ImageIO.write(finalImage, "png", temp);
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (metrics.stringWidth(currentLine + word) < maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word).append(" ");
            }
        }
        lines.add(currentLine.toString());
        return lines;
    }
}
