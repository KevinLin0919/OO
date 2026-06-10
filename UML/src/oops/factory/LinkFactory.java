package oops.factory;

import oops.model.ConnectionLine;
import oops.model.Port;
import oops.model.UMLObject;

// Factory Method Pattern
/**
 * LinkFactory 是 Factory Method Pattern 的 Product Factory 介面。
 * 每個實作類別負責建立一種具體 ConnectionLine，
 * 讓 CreateLinkMode 不依賴具體連線類別（AssociationLine、GeneralizationLine、CompositionLine）。
 */
public interface LinkFactory {
    ConnectionLine create(UMLObject source, Port sourcePort,
                          UMLObject destination, Port destPort);
}
