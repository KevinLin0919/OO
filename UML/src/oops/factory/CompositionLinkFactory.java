package oops.factory;

import oops.model.CompositionLine;
import oops.model.ConnectionLine;
import oops.model.Port;
import oops.model.UMLObject;

// Factory Method Pattern - Concrete Factory
public class CompositionLinkFactory implements LinkFactory {
    @Override
    public ConnectionLine create(UMLObject source, Port sourcePort,
                                 UMLObject destination, Port destPort) {
        return new CompositionLine(source, sourcePort, destination, destPort);
    }
}
