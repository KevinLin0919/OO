package oops.mode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

import oops.Canvas;
import oops.model.*;

/**
 * Select 模式，處理：
 *   - 點選物件（Use Case C）
 *   - 框選多個物件（Use Case C Case 2）
 *   - 移動物件（Use Case E）
 *   - 拖曳 port 來 resize 物件（Use Case F）
 *   - Hover 時顯示 port
 */
public class SelectMode implements Mode {

    private enum Action { NONE, MOVE, RESIZE, AREA_SELECT }

    private Action currentAction = Action.NONE;
    private int pressX, pressY;   // 滑鼠按下時的座標
    private int lastX, lastY;     // 上一次 drag 的座標（用於計算移動差值）
    private UMLObject targetObject;
    private Rectangle selectionRect;

    // Resize 專用
    private Port.Position resizePosition;
    private int origX, origY, origW, origH;  // resize 開始前的原始尺寸
    private int anchorX, anchorY;            // resize 時的固定錨點
    private boolean controlsX, controlsY;    // 此 port 控制哪些方向

    @Override
    public void mousePressed(MouseEvent e, Canvas canvas) {
        pressX = e.getX();
        pressY = e.getY();
        lastX = pressX;
        lastY = pressY;

        // === 優先檢查 port 點擊（觸發 Resize） ===
        // 只檢查目前有顯示 port 的物件（被 hover 或被 select 的基本物件）
        List<UMLObject> objects = canvas.getObjects();
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof CompositeObject) continue;        // Composite 無法 resize
            if (!obj.isHovered() && !obj.isSelected()) continue; // port 沒顯示就不檢查
            Port port = obj.getPortAt(pressX, pressY);
            if (port != null) {
                startResize(obj, port, canvas);
                return;
            }
        }

        // === 檢查是否點到物件（觸發 Move / Select） ===
        UMLObject clicked = canvas.getObjectAt(pressX, pressY);
        if (clicked != null) {
            currentAction = Action.MOVE;
            targetObject = clicked;

            if (!clicked.isSelected()) {
                // 點到未選取的物件 → 取消其他選取，只選這一個
                canvas.deselectAll();
                clicked.setSelected(true);
            }
            // 若物件已被選取（例如框選後），保留所有選取狀態 → 拖曳時整批移動

            canvas.bringToFront(clicked);
            canvas.repaint();
            return;
        }

        // === 點在空白處（觸發框選） ===
        currentAction = Action.AREA_SELECT;
        selectionRect = new Rectangle(pressX, pressY, 0, 0);
        canvas.deselectAll();
        canvas.repaint();
    }

    /** 初始化 resize 操作：計算錨點和控制方向 */
    private void startResize(UMLObject obj, Port port, Canvas canvas) {
        currentAction = Action.RESIZE;
        targetObject = obj;
        resizePosition = port.getPosition();
        origX = obj.getX();
        origY = obj.getY();
        origW = obj.getWidth();
        origH = obj.getHeight();

        // 根據拖曳的 port，決定固定錨點（對角位置）和可控制的方向
        controlsX = true;
        controlsY = true;
        switch (resizePosition) {
            case TOP_LEFT:
                anchorX = origX + origW; anchorY = origY + origH; break;
            case TOP_CENTER:
                anchorY = origY + origH; controlsX = false; break;
            case TOP_RIGHT:
                anchorX = origX; anchorY = origY + origH; break;
            case MIDDLE_LEFT:
                anchorX = origX + origW; controlsY = false; break;
            case MIDDLE_RIGHT:
                anchorX = origX; controlsY = false; break;
            case BOTTOM_LEFT:
                anchorX = origX + origW; anchorY = origY; break;
            case BOTTOM_CENTER:
                anchorY = origY; controlsX = false; break;
            case BOTTOM_RIGHT:
                anchorX = origX; anchorY = origY; break;
        }

        canvas.deselectAll();
        obj.setSelected(true);
        canvas.bringToFront(obj);
        canvas.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e, Canvas canvas) {
        int mx = e.getX();
        int my = e.getY();

        switch (currentAction) {
            case MOVE:
                // 計算差值，移動所有被選取的物件（支援框選後整批拖曳）
                int dx = mx - lastX;
                int dy = my - lastY;
                for (UMLObject obj : canvas.getSelectedObjects()) {
                    obj.move(dx, dy);
                    if (obj instanceof CompositeObject) {
                        ((CompositeObject) obj).updateBounds();
                    }
                }
                lastX = mx;
                lastY = my;
                canvas.repaint();
                break;

            case RESIZE:
                performResize(mx, my);
                canvas.repaint();
                break;

            case AREA_SELECT:
                // 更新框選矩形
                int sx = Math.min(pressX, mx);
                int sy = Math.min(pressY, my);
                int sw = Math.abs(mx - pressX);
                int sh = Math.abs(my - pressY);
                selectionRect = new Rectangle(sx, sy, sw, sh);
                canvas.repaint();
                break;

            default:
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e, Canvas canvas) {
        int mx = e.getX();
        int my = e.getY();

        if (currentAction == Action.AREA_SELECT && selectionRect != null) {
            // 選取完全落在框選矩形內的物件
            canvas.selectObjectsInRect(selectionRect);
            selectionRect = null;
        }

        if (currentAction == Action.RESIZE) {
            performResize(mx, my);
        }

        currentAction = Action.NONE;
        targetObject = null;
        canvas.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e, Canvas canvas) {
        // 更新 hover 狀態（讓滑鼠底下的物件顯示 port 或外框）
        UMLObject obj = canvas.getObjectAt(e.getX(), e.getY());
        canvas.setHoveredObject(obj);
        canvas.repaint();
    }

    @Override
    public void draw(Graphics2D g) {
        // 繪製框選矩形（藍色虛線 + 半透明填滿）
        if (currentAction == Action.AREA_SELECT && selectionRect != null) {
            g.setColor(new Color(0, 0, 200, 30));
            g.fillRect(selectionRect.x, selectionRect.y,
                    selectionRect.width, selectionRect.height);
            g.setColor(Color.BLUE);
            float[] dash = {5.0f};
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g.drawRect(selectionRect.x, selectionRect.y,
                    selectionRect.width, selectionRect.height);
            g.setStroke(oldStroke);
        }
    }

    /**
     * 執行 Resize 運算。
     * 核心思路：每個 port 都有一個對角錨點(anchor)，
     * resize 就是讓物件從 anchor 延伸到目前滑鼠位置。
     * 支援交叉反向拖曳 (cross-drag) 和最小尺寸 (20px) 限制。
     */
    private void performResize(int mx, int my) {
        int newX, newY, newW, newH;

        if (controlsX) {
            // 計算新寬度（取絕對值以支援 cross-drag）
            newW = Math.max(UMLObject.MIN_SIZE, Math.abs(mx - anchorX));
            // 決定新的 x：如果滑鼠在 anchor 左邊，物件從 anchor-newW 開始
            newX = (mx >= anchorX) ? anchorX : anchorX - newW;
        } else {
            newX = origX;
            newW = origW;
        }

        if (controlsY) {
            newH = Math.max(UMLObject.MIN_SIZE, Math.abs(my - anchorY));
            newY = (my >= anchorY) ? anchorY : anchorY - newH;
        } else {
            newY = origY;
            newH = origH;
        }

        targetObject.setBounds(newX, newY, newW, newH);
    }
}
