package oops.mode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

import oops.Canvas;
import oops.model.*;

// Strategy Pattern
/**
 * Select 模式，處理：
 *   - 點選物件（Use Case C）
 *   - 框選多個物件（Use Case C Case 2）
 *   - 移動物件（Use Case E）
 *   - 拖曳 port 來 resize 物件（Use Case F）
 *   - Hover 時顯示 port
 *
 * 所有物件資料操作透過 canvas.getModel() 存取 UMLModel。
 */
public class SelectMode implements Mode {

    private enum Action { NONE, MOVE, RESIZE, AREA_SELECT }

    private Action currentAction = Action.NONE;
    private int pressX, pressY;
    private int lastX, lastY;
    private UMLObject targetObject;
    private Rectangle selectionRect;

    // Resize 專用
    private Port.Position resizePosition;
    private int origX, origY, origW, origH;
    private int anchorX, anchorY;
    private boolean controlsX, controlsY;

    @Override
    public void mousePressed(MouseEvent e, Canvas canvas) {
        UMLModel model = canvas.getModel();
        pressX = e.getX();
        pressY = e.getY();
        lastX = pressX;
        lastY = pressY;

        // 優先檢查 port 點擊（觸發 Resize）
        List<UMLObject> objects = model.getObjects();
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof CompositeObject) continue;
            if (!obj.isHovered() && !obj.isSelected()) continue;
            Port port = obj.getPortAt(pressX, pressY);
            if (port != null) {
                startResize(obj, port, model);
                return;
            }
        }

        // 檢查是否點到物件（觸發 Move / Select）
        UMLObject clicked = model.getObjectAt(pressX, pressY);
        if (clicked != null) {
            currentAction = Action.MOVE;
            targetObject = clicked;

            if (!clicked.isSelected()) {
                model.deselectAll();
                clicked.setSelected(true);
            }

            model.bringToFront(clicked);
            return;
        }

        // 點在空白處（觸發框選）
        currentAction = Action.AREA_SELECT;
        selectionRect = new Rectangle(pressX, pressY, 0, 0);
        model.deselectAll();
    }

    private void startResize(UMLObject obj, Port port, UMLModel model) {
        currentAction = Action.RESIZE;
        targetObject = obj;
        resizePosition = port.getPosition();
        origX = obj.getX();
        origY = obj.getY();
        origW = obj.getWidth();
        origH = obj.getHeight();

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

        model.deselectAll();
        obj.setSelected(true);
        model.bringToFront(obj);
    }

    @Override
    public void mouseDragged(MouseEvent e, Canvas canvas) {
        UMLModel model = canvas.getModel();
        int mx = e.getX();
        int my = e.getY();

        switch (currentAction) {
            case MOVE:
                int dx = mx - lastX;
                int dy = my - lastY;
                for (UMLObject obj : model.getSelectedObjects()) {
                    obj.move(dx, dy);
                    if (obj instanceof CompositeObject) {
                        ((CompositeObject) obj).updateBounds();
                    }
                }
                lastX = mx;
                lastY = my;
                model.refresh();
                break;

            case RESIZE:
                performResize(mx, my);
                model.refresh();
                break;

            case AREA_SELECT:
                int sx = Math.min(pressX, mx);
                int sy = Math.min(pressY, my);
                int sw = Math.abs(mx - pressX);
                int sh = Math.abs(my - pressY);
                selectionRect = new Rectangle(sx, sy, sw, sh);
                model.refresh();
                break;

            default:
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e, Canvas canvas) {
        UMLModel model = canvas.getModel();
        int mx = e.getX();
        int my = e.getY();

        if (currentAction == Action.AREA_SELECT && selectionRect != null) {
            model.selectObjectsInRect(selectionRect);
            selectionRect = null;
        }

        if (currentAction == Action.RESIZE) {
            performResize(mx, my);
        }

        currentAction = Action.NONE;
        targetObject = null;
        model.refresh();
    }

    @Override
    public void mouseMoved(MouseEvent e, Canvas canvas) {
        UMLObject obj = canvas.getModel().getObjectAt(e.getX(), e.getY());
        canvas.getModel().setHoveredObject(obj);
    }

    @Override
    public void draw(Graphics2D g) {
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

    private void performResize(int mx, int my) {
        int newX, newY, newW, newH;

        if (controlsX) {
            newW = Math.max(UMLObject.MIN_SIZE, Math.abs(mx - anchorX));
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
