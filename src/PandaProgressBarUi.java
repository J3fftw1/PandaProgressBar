import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.util.ScalableIcon;
import com.intellij.ui.ColorHexUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import sun.swing.SwingUtilities2;

public class PandaProgressBarUi extends BasicProgressBarUI {

    private static final float ONE_OVER_SEVEN = 1f / 7;

    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
    public static ComponentUI createUI(JComponent component) {
        component.setBorder(JBUI.Borders.empty().asUIResource());
        return new PandaProgressBarUi();
    }

    @Override
    public Dimension getPreferredSize(JComponent component) {
        return new Dimension(super.getPreferredSize(component).width, JBUI.scale(20));
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        progressBar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                super.componentShown(event);
            }

            @Override
            public void componentHidden(ComponentEvent event) {
                super.componentHidden(event);
            }
        });
    }

    private volatile int offset = 0;
    private volatile int offset2 = 0;
    private volatile int velocity = 1;

    @Override
    protected void paintIndeterminate(Graphics graphics, JComponent component) {

        if (!(graphics instanceof Graphics2D)) return;

        Graphics2D graphics2D = (Graphics2D) graphics;

        Insets border = progressBar.getInsets(); // area for border
        int barRectWidth = progressBar.getWidth() - (border.right + border.left);
        int barRectHeight = progressBar.getHeight() - (border.top + border.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) return;

        graphics2D.setColor(new JBColor(Gray._240.withAlpha(50), Gray._128.withAlpha(50)));
        int width = component.getWidth();
        int height = component.getPreferredSize().height;
        if (isUnEven(component.getHeight() - height)) height++;

        LinearGradientPaint baseBambooPaint = new LinearGradientPaint(0, JBUI.scale(2), 0, height - JBUI.scale(6),
            new float[] {ONE_OVER_SEVEN * 1, ONE_OVER_SEVEN * 2, ONE_OVER_SEVEN * 3,
                ONE_OVER_SEVEN * 4, ONE_OVER_SEVEN * 5, ONE_OVER_SEVEN * 6, ONE_OVER_SEVEN * 7
            },
            new Color[] {ColorHexUtil.fromHex("#2c6705"), ColorHexUtil.fromHex("#377c07"),
                ColorHexUtil.fromHex("#538d1a"), ColorHexUtil.fromHex("#6e9a42"), ColorHexUtil.fromHex("#e2dbac"),
                ColorHexUtil.fromHex("#aec957"), ColorHexUtil.fromHex("#aec957")}
        );

        graphics2D.setPaint(baseBambooPaint);

        if (component.isOpaque()) graphics2D.fillRect(0, (component.getHeight() - height) / 2, width, height);

        graphics2D.setColor(new JBColor(Gray._165.withAlpha(50), Gray._88.withAlpha(50)));
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(graphics2D);
        graphics2D.translate(0, (component.getHeight() - height) / 2);
        Paint old = graphics2D.getPaint();
        graphics2D.setPaint(baseBambooPaint);

        final float scale1 = JBUI.scale(8);
        final float scale2 = JBUI.scale(9);
        final Area containingRoundRect = new Area(new RoundRectangle2D.Float(1f, 1f, width - 2f, height - 2f, scale1,
            scale1));
        graphics2D.fill(containingRoundRect);
        graphics2D.setPaint(old);
        offset = (offset + 1) % getPeriodLength();
        offset2 += velocity;
        if (offset2 <= 2) {
            offset2 = 2;
            velocity = 1;
        } else if (offset2 >= width - JBUI.scale(15)) {
            offset2 = width - JBUI.scale(15);
            velocity = -1;
        }
        Area area = new Area(new Rectangle2D.Float(0, 0, width, height));
        area.subtract(new Area(new RoundRectangle2D.Float(1f, 1f, width - 2f, height - 2f, scale1, scale1)));
        graphics2D.setPaint(Gray._128);
        if (component.isOpaque()) graphics2D.fill(area);

        area.subtract(new Area(new RoundRectangle2D.Float(0, 0, width, height, scale2, scale2)));

        Container parent = component.getParent();
        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();
        graphics2D.setPaint(background);
        if (component.isOpaque()) graphics2D.fill(area);

        Icon scaledIcon = velocity > 0 ? ((ScalableIcon) PandaIcons.PANDA_ICON) :
            ((ScalableIcon) PandaIcons.RPANDA_ICON);
        scaledIcon.paintIcon(progressBar, graphics2D, offset2 - JBUI.scale(10), -JBUI.scale(6));

        graphics2D.draw(new RoundRectangle2D.Float(1f, 1f, width - 2f - 1f, height - 2f - 1f, scale1, scale1));
        graphics2D.translate(0, -(component.getHeight() - height) / 2);

        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
                paintString(graphics2D, border.left, border.top, barRectWidth, barRectHeight, boxRect.x, boxRect.width);
            } else {
                paintString(graphics2D, border.left, border.top, barRectWidth, barRectHeight,
                    boxRect.y, boxRect.height
                );
            }
        }
        config.restore();
    }

    @Override
    protected void paintDeterminate(Graphics graphics, JComponent component) {
        if (!(graphics instanceof Graphics2D)) return;


        if (progressBar.getOrientation() != SwingConstants.HORIZONTAL
            || !component.getComponentOrientation().isLeftToRight()
        ) {
            super.paintDeterminate(graphics, component);
            return;
        }
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(graphics);
        Insets border = progressBar.getInsets(); // area for border
        int width = progressBar.getWidth();
        int height = progressBar.getPreferredSize().height;
        if (isUnEven(component.getHeight() - height)) height++;

        int barRectWidth = width - (border.right + border.left);
        int barRectHeight = height - (border.top + border.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) return;

        int amountFull = getAmountFull(border, barRectWidth, barRectHeight);

        Container parent = component.getParent();
        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();

        graphics.setColor(background);
        Graphics2D graphics2D = (Graphics2D) graphics;
        if (component.isOpaque()) graphics.fillRect(0, 0, width, height);

        final float scale1 = JBUI.scale(8);
        final float scale2 = JBUI.scale(9);
        final float scale3 = JBUI.scale(1);

        graphics2D.translate(0, (component.getHeight() - height) / 2);
        graphics2D.setColor(progressBar.getForeground());
        graphics2D.fill(new RoundRectangle2D.Float(0, 0, width - scale3, height - scale3, scale2, scale2));
        graphics2D.setColor(background);
        graphics2D.fill(new RoundRectangle2D.Float(scale3, scale3, width - 2f * scale3 - scale3,
            height - 2f * scale3 - scale3, scale1, scale1)
        );
        graphics2D.setPaint(new LinearGradientPaint(0, JBUI.scale(2), 0, height - JBUI.scale(6),
            new float[] {ONE_OVER_SEVEN * 1, ONE_OVER_SEVEN * 2, ONE_OVER_SEVEN * 3,
                ONE_OVER_SEVEN * 4, ONE_OVER_SEVEN * 5, ONE_OVER_SEVEN * 6, ONE_OVER_SEVEN * 7
            },
            new Color[] {ColorHexUtil.fromHex("#2c6705"), ColorHexUtil.fromHex("#377c07"),
                ColorHexUtil.fromHex("#538d1a"), ColorHexUtil.fromHex("#6e9a42"), ColorHexUtil.fromHex("#e2dbac"),
                ColorHexUtil.fromHex("#aec957"), ColorHexUtil.fromHex("#aec957")})
        );

        PandaIcons.PANDA_ICON.paintIcon(progressBar, graphics2D, amountFull - JBUI.scale(10), -JBUI.scale(6));
        graphics2D.fill(new RoundRectangle2D.Float(2f * scale3, 2f * scale3, amountFull - JBUI.scale(5), height -
            JBUI.scale(5), JBUI.scale(7), JBUI.scale(7))
        );
        graphics2D.translate(0, -(component.getHeight() - height) / 2);

        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            paintString(graphics, border.left, border.top,
                barRectWidth, barRectHeight,
                amountFull, border);
        }
        config.restore();
    }

    private void paintString(Graphics graphics, int x, int y, int w, int h, int fillStart, int amountFull) {
        if (!(graphics instanceof Graphics2D)) {
            return;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;
        String progressString = progressBar.getString();
        graphics2D.setFont(progressBar.getFont());
        Point renderLocation = getStringPlacement(graphics2D, progressString,
            x, y, w, h);
        Rectangle oldClip = graphics2D.getClipBounds();

        if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
            graphics2D.setColor(getSelectionBackground());
            SwingUtilities2.drawString(progressBar, graphics2D, progressString,
                renderLocation.x, renderLocation.y);
            graphics2D.setColor(getSelectionForeground());
            graphics2D.clipRect(fillStart, y, amountFull, h);
            SwingUtilities2.drawString(progressBar, graphics2D, progressString,
                renderLocation.x, renderLocation.y);
        } else { // VERTICAL
            graphics2D.setColor(getSelectionBackground());
            AffineTransform rotate =
                AffineTransform.getRotateInstance(Math.PI / 2);
            graphics2D.setFont(progressBar.getFont().deriveFont(rotate));
            renderLocation = getStringPlacement(graphics2D, progressString,
                x, y, w, h);
            SwingUtilities2.drawString(progressBar, graphics2D, progressString,
                renderLocation.x, renderLocation.y);
            graphics2D.setColor(getSelectionForeground());
            graphics2D.clipRect(x, fillStart, w, amountFull);
            SwingUtilities2.drawString(progressBar, graphics2D, progressString,
                renderLocation.x, renderLocation.y);
        }
        graphics2D.setClip(oldClip);
    }

    @Override
    protected int getBoxLength(int availableLength, int otherDimension) {
        return availableLength;
    }

    private int getPeriodLength() {
        return JBUI.scale(16);
    }

    private static boolean isUnEven(int value) {
        return value % 2 != 0;
    }
}

