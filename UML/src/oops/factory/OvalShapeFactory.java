package oops.factory;

import java.awt.Graphics2D;

import oops.model.OvalObject;
import oops.model.UMLObject;

// Factory Method Pattern - Concrete Factory
public class OvalShapeFactory implements ShapeFactory {
    @Override
    public UMLObject create(int x, int y, int w, int h) {
        return new OvalObject(x, y, w, h);
    }

    @Override
    public void drawPreview(Graphics2D g, int x, int y, int w, int h) {
        g.drawOval(x, y, w, h);
    }
}
