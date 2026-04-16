package oops.mode;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import oops.Canvas;

/**
 * Mode 介面（Strategy Pattern）。
 * 不同的編輯模式（Select、建立連線）各自實作對滑鼠事件的回應方式。
 * Canvas 持有一個 Mode 的參考，將滑鼠事件委派給目前的 Mode 處理。
 */
public interface Mode {
    void mousePressed(MouseEvent e, Canvas canvas);
    void mouseDragged(MouseEvent e, Canvas canvas);
    void mouseReleased(MouseEvent e, Canvas canvas);
    void mouseMoved(MouseEvent e, Canvas canvas);

    /** 繪製模式特有的暫時性視覺元素（如框選矩形、拖曳中的暫時連線） */
    default void draw(Graphics2D g) {}
}
