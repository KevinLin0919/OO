package oops.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有 UML 物件的抽象基底類別。
 * 子類別包含 RectObject、OvalObject、CompositeObject。
 * 每個物件都有位置(x,y)、大小(width,height)、選取狀態、hover 狀態、以及標籤(label)。
 */
public abstract class UMLObject {

    protected int x, y, width, height;
    protected boolean selected;
    protected boolean hovered;
    protected String labelName = "";
    protected Color labelColor = null;  // null 代表使用預設顏色 (灰色)
    protected List<Port> ports = new ArrayList<>();

    public static final int MIN_SIZE = 20; // 物件的最小寬度和高度

    public UMLObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        initPorts();
    }

    /** 子類別實作此方法，初始化各自的 port */
    protected abstract void initPorts();

    /** 子類別實作此方法，繪製物件 */
    public abstract void draw(Graphics2D g);

    /** 子類別實作此方法，判斷座標是否在物件內 */
    public abstract boolean contains(int px, int py);

    /** 找到座標 (px, py) 落在哪個 port 上，沒有則回傳 null */
    public Port getPortAt(int px, int py) {
        for (Port port : ports) {
            if (port.contains(px, py)) {
                return port;
            }
        }
        return null;
    }

    /** 找到離 (px, py) 最近的 port */
    public Port getNearestPort(int px, int py) {
        Port nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Port port : ports) {
            Point center = port.getCenter();
            double dist = Math.hypot(px - center.x, py - center.y);
            if (dist < minDist) {
                minDist = dist;
                nearest = port;
            }
        }
        return nearest;
    }

    /** 繪製所有 port */
    public void drawPorts(Graphics2D g) {
        for (Port port : ports) {
            port.draw(g);
        }
    }

    /** 移動物件 (相對偏移量) */
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /** 設定物件的位置和大小，自動套用最小尺寸限制 */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(MIN_SIZE, width);
        this.height = Math.max(MIN_SIZE, height);
    }

    // ======== Getters & Setters ========

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public boolean isHovered() { return hovered; }
    public void setHovered(boolean hovered) { this.hovered = hovered; }

    public String getLabelName() { return labelName; }
    public void setLabelName(String name) { this.labelName = name; }

    public Color getLabelColor() { return labelColor; }
    public void setLabelColor(Color color) { this.labelColor = color; }

    public List<Port> getPorts() { return ports; }
}
