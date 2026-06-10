package oops.factory;

import oops.model.AssociationLine;
import oops.model.ConnectionLine;
import oops.model.Port;
import oops.model.UMLObject;

// Factory Method Pattern - Concrete Factory
public class AssociationLinkFactory implements LinkFactory {
    @Override
    public ConnectionLine create(UMLObject source, Port sourcePort,
                                 UMLObject destination, Port destPort) {
        return new AssociationLine(source, sourcePort, destination, destPort);
    }
}
