package oops.model;

import java.awt.*;

/**
 * 橢圓物件，擁有 4 個 port（上、右、下、左）。
 */
public class OvalObject extends UMLObject {

    public OvalObject(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void initPorts() {
        // Oval 只有 4 個 port（上下左右）
        ports.add(new Port(Port.Position.TOP_CENTER, this));
        ports.add(new Port(Port.Position.MIDDLE_RIGHT, this));
        ports.add(new Port(Port.Position.BOTTOM_CENTER, this));
        ports.add(new Port(Port.Position.MIDDLE_LEFT, this));
    }

    @Override
    public void draw(Graphics2D g) {
        // 填滿橢圓
        g.setColor(labelColor != null ? labelColor : new Color(200, 200, 200));
        g.fillOval(x, y, width, height);

        // 畫邊框
        g.setColor(Color.BLACK);
        g.drawOval(x, y, width, height);

        // 畫標籤文字（置中）
        if (!labelName.isEmpty()) {
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            int textW = fm.stringWidth(labelName);
            int textH = fm.getAscent();
            g.drawString(labelName, x + (width - textW) / 2, y + (height + textH) / 2);
        }

        // 被選取或被 hover 時顯示 port
        if (selected || hovered) {
            drawPorts(g);
        }
    }

    @Override
    public boolean contains(int px, int py) {
        // 橢圓包含判定：(px-cx)^2/rx^2 + (py-cy)^2/ry^2 <= 1
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;
        if (rx == 0 || ry == 0) return false;
        double dx = (px - cx) / rx;
        double dy = (py - cy) / ry;
        return dx * dx + dy * dy <= 1.0;
    }
}
