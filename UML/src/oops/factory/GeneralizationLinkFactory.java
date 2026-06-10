package oops.factory;

import oops.model.ConnectionLine;
import oops.model.GeneralizationLine;
import oops.model.Port;
import oops.model.UMLObject;

// Factory Method Pattern - Concrete Factory
public class GeneralizationLinkFactory implements LinkFactory {
    @Override
    public ConnectionLine create(UMLObject source, Port sourcePort,
                                 UMLObject destination, Port destPort) {
        return new GeneralizationLine(source, sourcePort, destination, destPort);
    }
}
