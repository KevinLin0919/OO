package oops.mode;

import java.awt.*;
import java.awt.event.MouseEvent;

import oops.Canvas;
import oops.model.*;

/**
 * 建立物件模式（Use Case A）。
 * 當使用者選擇 Rect 或 Oval 按鈕後，在畫布上點擊（放開滑鼠）即可建立物件。
 * 模式會持續保持，使用者可以連續建立多個物件，直到手動切換其他模式。
 */
public class CreateObjectMode implements Mode {

    private final boolean isRect;

    public CreateObjectMode(boolean isRect) {
        this.isRect = isRect;
    }

    @Override
    public void mousePressed(MouseEvent e, Canvas canvas) {
        // 按下時不做事，等到放開再建立物件
    }

    @Override
    public void mouseDragged(MouseEvent e, Canvas canvas) {
        // 拖曳時不做事
    }

    @Override
    public void mouseReleased(MouseEvent e, Canvas canvas) {
        // 在滑鼠放開的位置建立物件（物件中心對齊滑鼠位置）
        int defaultW = 150, defaultH = 100;
        int objX = e.getX() - defaultW / 2;
        int objY = e.getY() - defaultH / 2;

        UMLObject obj = isRect
                ? new RectObject(objX, objY, defaultW, defaultH)
                : new OvalObject(objX, objY, defaultW, defaultH);
        canvas.addObject(obj);
        canvas.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e, Canvas canvas) {
        // 顯示十字游標，提示使用者目前是建立物件模式
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void draw(Graphics2D g) {
        // 不需要繪製額外的視覺元素
    }
}
