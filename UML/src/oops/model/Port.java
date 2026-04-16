package oops.model;

import java.awt.*;

/**
 * Port 代表基本物件邊緣上的連接點。
 * Rect 有 8 個 port（四角 + 四邊中點），Oval 有 4 個 port（上下左右）。
 * Port 用於：(1) 建立 Connection Link 的端點 (2) Resize 時的拖曳把手
 */
public class Port {

    // 8 個可能的 port 位置
    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    private static final int PORT_SIZE = 10; // port 顯示和點擊區域的大小 (10x10 像素)

    private final Position position;
    private final UMLObject owner;

    public Port(Position position, UMLObject owner) {
        this.position = position;
        this.owner = owner;
    }

    /** 根據 owner 目前的位置和大小，計算 port 的中心座標 */
    public Point getCenter() {
        int x = owner.getX();
        int y = owner.getY();
        int w = owner.getWidth();
        int h = owner.getHeight();

        switch (position) {
            case TOP_LEFT:      return new Point(x, y);
            case TOP_CENTER:    return new Point(x + w / 2, y);
            case TOP_RIGHT:     return new Point(x + w, y);
            case MIDDLE_LEFT:   return new Point(x, y + h / 2);
            case MIDDLE_RIGHT:  return new Point(x + w, y + h / 2);
            case BOTTOM_LEFT:   return new Point(x, y + h);
            case BOTTOM_CENTER: return new Point(x + w / 2, y + h);
            case BOTTOM_RIGHT:  return new Point(x + w, y + h);
            default:            return new Point(x, y);
        }
    }

    /** 判斷座標 (px, py) 是否落在 port 的點擊區域內 */
    public boolean contains(int px, int py) {
        Point center = getCenter();
        return Math.abs(px - center.x) <= PORT_SIZE / 2
            && Math.abs(py - center.y) <= PORT_SIZE / 2;
    }

    /** 繪製 port（黑色實心小方塊） */
    public void draw(Graphics2D g) {
        Point center = getCenter();
        g.setColor(Color.BLACK);
        g.fillRect(center.x - PORT_SIZE / 2, center.y - PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
    }

    public Position getPosition() { return position; }
    public UMLObject getOwner() { return owner; }
    public static int getPortSize() { return PORT_SIZE; }
}
