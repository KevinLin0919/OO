package oops.model;

import java.awt.*;

/**
 * Composition 連線：在終點繪製一個空心菱形箭頭 (◇)。
 */
public class CompositionLine extends ConnectionLine {

    public CompositionLine(UMLObject source, Port sourcePort,
                           UMLObject destination, Port destPort) {
        super(source, sourcePort, destination, destPort);
    }

    @Override
    protected void drawArrowHead(Graphics2D g, Point start, Point end) {
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        int size = 12;

        // 菱形四個頂點：尖端(tip)、左翼、後端(back)、右翼
        int leftX  = (int) (end.x - size * Math.cos(angle - Math.PI / 4));
        int leftY  = (int) (end.y - size * Math.sin(angle - Math.PI / 4));
        int rightX = (int) (end.x - size * Math.cos(angle + Math.PI / 4));
        int rightY = (int) (end.y - size * Math.sin(angle + Math.PI / 4));
        int backX  = (int) (end.x - 2 * size * Math.cos(angle));
        int backY  = (int) (end.y - 2 * size * Math.sin(angle));

        Polygon diamond = new Polygon();
        diamond.addPoint(end.x, end.y);   // 尖端
        diamond.addPoint(leftX, leftY);    // 左翼
        diamond.addPoint(backX, backY);    // 後端
        diamond.addPoint(rightX, rightY);  // 右翼

        // 空心菱形：白色填滿 + 黑色外框
        g.setColor(Color.WHITE);
        g.fillPolygon(diamond);
        g.setColor(Color.BLACK);
        g.drawPolygon(diamond);
    }
}
