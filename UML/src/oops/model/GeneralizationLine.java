package oops.model;

import java.awt.*;

/**
 * Generalization 連線：在終點繪製一個空心三角形箭頭 (◁)。
 */
public class GeneralizationLine extends ConnectionLine {

    public GeneralizationLine(UMLObject source, Port sourcePort,
                              UMLObject destination, Port destPort) {
        super(source, sourcePort, destination, destPort);
    }

    @Override
    protected void drawArrowHead(Graphics2D g, Point start, Point end) {
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        int size = 20;

        // 三角形的三個頂點：尖端(end) + 兩翼
        int x1 = (int) (end.x - size * Math.cos(angle - Math.PI / 6));
        int y1 = (int) (end.y - size * Math.sin(angle - Math.PI / 6));
        int x2 = (int) (end.x - size * Math.cos(angle + Math.PI / 6));
        int y2 = (int) (end.y - size * Math.sin(angle + Math.PI / 6));

        Polygon triangle = new Polygon();
        triangle.addPoint(end.x, end.y);
        triangle.addPoint(x1, y1);
        triangle.addPoint(x2, y2);

        // 空心三角形：白色填滿 + 黑色外框
        g.setColor(Color.WHITE);
        g.fillPolygon(triangle);
        g.setColor(Color.BLACK);
        g.drawPolygon(triangle);
    }
}
