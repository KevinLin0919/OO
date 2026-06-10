package oops.mode;

import java.awt.*;
import java.awt.event.MouseEvent;

import oops.Canvas;
import oops.factory.ShapeFactory;
import oops.model.*;

// Strategy Pattern
/**
 * 建立物件模式（Use Case A — 點擊按鈕後的畫布拖曳路徑）。
 * 採用 Factory Method Pattern：透過注入的 ShapeFactory 建立物件，
 * 不直接依賴具體物件類別（RectObject、OvalObject）。
 *
 * 互動流程：
 *   1. mousePressed  → 記錄拖曳起點
 *   2. mouseDragged  → 即時顯示預覽外框
 *   3. mouseReleased → 以拖曳範圍建立物件，呼叫 onCreated 自動回到原模式
 *
 * 大小規則：
 *   - 以 (起點, 終點) 的最小外框決定位置和大小
 *   - 最小尺寸限制：UMLObject.MIN_SIZE（20 px），避免建立過小的物件
 */
public class CreateObjectMode implements Mode {

    private final ShapeFactory factory;
    private final Runnable onCreated;

    private int startX, startY;
    private int currX, currY;
    private boolean dragging = false;

    public CreateObjectMode(ShapeFactory factory, Runnable onCreated) {
        this.factory = factory;
        this.onCreated = onCreated;
    }

    @Override
    public void mousePressed(MouseEvent e, Canvas canvas) {
        startX = e.getX();
        startY = e.getY();
        currX  = startX;
        currY  = startY;
        dragging = true;
    }

    @Override
    public void mouseDragged(MouseEvent e, Canvas canvas) {
        if (!dragging) return;
        currX = e.getX();
        currY = e.getY();
        canvas.getModel().refresh();
    }

    @Override
    public void mouseReleased(MouseEvent e, Canvas canvas) {
        if (!dragging) return;
        dragging = false;

        int x = Math.min(startX, e.getX());
        int y = Math.min(startY, e.getY());
        int w = Math.max(UMLObject.MIN_SIZE, Math.abs(e.getX() - startX));
        int h = Math.max(UMLObject.MIN_SIZE, Math.abs(e.getY() - startY));

        UMLObject obj = factory.create(x, y, w, h);
        canvas.getModel().addObject(obj);

        onCreated.run();
    }

    @Override
    public void mouseMoved(MouseEvent e, Canvas canvas) {
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void draw(Graphics2D g) {
        if (!dragging) return;

        int x = Math.min(startX, currX);
        int y = Math.min(startY, currY);
        int w = Math.abs(currX - startX);
        int h = Math.abs(currY - startY);

        Stroke old = g.getStroke();
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[]{6f, 3f}, 0f));

        factory.drawPreview(g, x, y, w, h);
        g.setStroke(old);
    }
}
