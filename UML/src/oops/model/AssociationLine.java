package oops.model;

import java.awt.*;

/**
 * Association 連線：在終點繪製一個簡單的 V 形箭頭 (←)。
 */
public class AssociationLine extends ConnectionLine {

    public AssociationLine(UMLObject source, Port sourcePort,
                           UMLObject destination, Port destPort) {
        super(source, sourcePort, destination, destPort);
    }

    @Override
    protected void drawArrowHead(Graphics2D g, Point start, Point end) {
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        int size = 15;

        // 計算箭頭兩翼的端點
        int x1 = (int) (end.x - size * Math.cos(angle - Math.PI / 6));
        int y1 = (int) (end.y - size * Math.sin(angle - Math.PI / 6));
        int x2 = (int) (end.x - size * Math.cos(angle + Math.PI / 6));
        int y2 = (int) (end.y - size * Math.sin(angle + Math.PI / 6));

        g.setColor(Color.BLACK);
        g.drawLine(end.x, end.y, x1, y1);
        g.drawLine(end.x, end.y, x2, y2);
    }
}
