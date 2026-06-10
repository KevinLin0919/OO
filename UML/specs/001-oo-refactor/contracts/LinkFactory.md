# Contract: LinkFactory

**Pattern**: Factory Method
**Package**: `oops.factory`

## Interface Definition

```java
public interface LinkFactory {
    ConnectionLine create(UMLObject source, Port sourcePort,
                          UMLObject destination, Port destPort);
}
```

## Contract Rules

- `create()` 必須回傳非 null 的 `ConnectionLine` 實例
- `source` 與 `destination` 不得為同一物件（呼叫端在 `CreateLinkMode.mouseReleased` 已驗證）
- `sourcePort` 與 `destPort` 必須分別屬於 `source` 與 `destination`（呼叫端保證）

## Implementations

| 類別 | 回傳類型 | 箭頭樣式 |
|------|---------|---------|
| `AssociationLinkFactory` | `AssociationLine` | V 形箭頭 |
| `GeneralizationLinkFactory` | `GeneralizationLine` | 空心三角形 |
| `CompositionLinkFactory` | `CompositionLine` | 空心菱形 |

## Consumers

- `CreateLinkMode`（透過建構子注入）
