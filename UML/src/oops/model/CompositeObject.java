package oops.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 群組物件（Composite），由多個基本物件組合而成。
 * Composite 是樹狀結構，也就是說 Composite 裡面可以再包含 Composite。
 * 它的範圍（bounding box）是能完全包含所有子物件的最小矩形。
 *
 * 注意：Composite 沒有 port，不能被 resize，也不能被 link 連接。
 */
public class CompositeObject extends UMLObject {

    private final List<UMLObject> children = new ArrayList<>();

    public CompositeObject(List<UMLObject> children) {
        super(0, 0, 0, 0);
        this.children.addAll(children);
        updateBounds();
    }

    @Override
    protected void initPorts() {
        // Composite 沒有 port
    }

    /** 重新計算 bounding box（根據所有子物件的位置和大小） */
    public void updateBounds() {
        if (children.isEmpty()) return;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (UMLObject child : children) {
            minX = Math.min(minX, child.getX());
            minY = Math.min(minY, child.getY());
            maxX = Math.max(maxX, child.getX() + child.getWidth());
            maxY = Math.max(maxY, child.getY() + child.getHeight());
        }
        this.x = minX;
        this.y = minY;
        this.width = maxX - minX;
        this.height = maxY - minY;
    }

    @Override
    public void draw(Graphics2D g) {
        // 先繪製所有子物件
        for (UMLObject child : children) {
            child.draw(g);
        }

        // 被選取或被 hover 時，畫藍色虛線外框
        if (selected || hovered) {
            g.setColor(Color.BLUE);
            float[] dash = {5.0f};
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g.drawRect(x - 2, y - 2, width + 4, height + 4);
            g.setStroke(oldStroke);
        }
    }

    @Override
    public boolean contains(int px, int py) {
        return px >= x - 2 && px <= x + width + 2 && py >= y - 2 && py <= y + height + 2;
    }

    /** 移動 Composite 時，所有子物件也一起移動 */
    @Override
    public void move(int dx, int dy) {
        for (UMLObject child : children) {
            child.move(dx, dy);
        }
        updateBounds();
    }

    public List<UMLObject> getChildren() {
        return children;
    }

    /** 遞迴取得所有葉節點（基本物件），用於 link 的連接判定 */
    public List<UMLObject> getAllBasicObjects() {
        List<UMLObject> result = new ArrayList<>();
        for (UMLObject child : children) {
            if (child instanceof CompositeObject) {
                result.addAll(((CompositeObject) child).getAllBasicObjects());
            } else {
                result.add(child);
            }
        }
        return result;
    }
}
