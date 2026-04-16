package oops.model;

import java.awt.*;

/**
 * 矩形物件，擁有 8 個 port（四個角落 + 四個邊中點）。
 */
public class RectObject extends UMLObject {

    public RectObject(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void initPorts() {
        // Rect 擁有全部 8 個 port
        ports.add(new Port(Port.Position.TOP_LEFT, this));
        ports.add(new Port(Port.Position.TOP_CENTER, this));
        ports.add(new Port(Port.Position.TOP_RIGHT, this));
        ports.add(new Port(Port.Position.MIDDLE_LEFT, this));
        ports.add(new Port(Port.Position.MIDDLE_RIGHT, this));
        ports.add(new Port(Port.Position.BOTTOM_LEFT, this));
        ports.add(new Port(Port.Position.BOTTOM_CENTER, this));
        ports.add(new Port(Port.Position.BOTTOM_RIGHT, this));
    }

    @Override
    public void draw(Graphics2D g) {
        // 填滿矩形（有 labelColor 就用 labelColor，否則用預設灰色）
        g.setColor(labelColor != null ? labelColor : new Color(200, 200, 200));
        g.fillRect(x, y, width, height);

        // 畫邊框
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        // 畫標籤文字（置中顯示）
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
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
