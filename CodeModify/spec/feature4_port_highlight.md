# Feature 4 — Select 模式點擊 Port 時 Highlight 相連線

## 需求描述

在 **Select 模式（index 0）** 下，點擊物件的 port 時，與該 port 相連的所有線段會被 highlight（高亮顯示）。

---

## 行為規格

| 操作 | 行為 |
|------|------|
| 點擊 port（有連線） | 與此 port 相連的線 → highlight |
| 點擊 port（無連線） | 無任何動作 |
| 點擊線本身 | **不觸發**此功能（維持現有行為） |
| 點擊其他地方 | 取消所有 highlight |

> **重點：** 只有點擊 port（邊緣中心點附近）才觸發，點擊線不觸發。

---

## Port 偵測邏輯

與 Feature 2 的 port 位置定義相同：

| Port | 座標 |
|------|------|
| TOP    | `(x + w/2, y)` |
| RIGHT  | `(x + w,   y + h/2)` |
| LEFT   | `(x,       y + h/2)` |
| BOTTOM | `(x + w/2, y + h)` |

**偵測條件：** 點擊座標距 port 點距離 ≤ `PORT_CLICK_RADIUS`（建議 8px）

---

## 線段存取方式

> **關鍵：** `members` 只含物件（BasicClass/UseCase/GroupContainer），線段**不在 members 中**，  
> 需從 `contextPanel.getComponents()` 遍歷取得。

```java
Component[] components = contextPanel.getComponents();
for (Component c : components) {
    if (c instanceof AssociationLine) { ... }
    else if (c instanceof CompositionLine) { ... }
    else if (c instanceof GeneralizationLine) { ... }
    else if (c instanceof DependencyLine) { ... }   // Feature 1 新增
}
```

---

## 需要在各 Line 類別新增的方法

### isConnectedToPort(JPanel obj, int side)

判斷此線是否連接到指定物件的指定 port 側：

```java
public boolean isConnectedToPort(JPanel obj, int side) {
    return (from == obj && fromSide == side)
        || (to   == obj && toSide   == side);
}
```

> 需加到：AssociationLine、CompositionLine、GeneralizationLine、DependencyLine

### setHighlight(boolean) / isHighlight()

管理 highlight 狀態；highlight 以不同顏色（建議藍色 `Color.BLUE`）繪製線段：

```java
boolean isHighlight = false;

public void setHighlight(boolean h) { this.isHighlight = h; }
public boolean isHighlight()        { return isHighlight; }
```

在 `paintComponent()` 中加入 highlight 繪製邏輯：

```java
if (isHighlight) {
    g.setColor(Color.BLUE);
    // 重繪線段（虛線/實線依類型）
    g.drawLine(fpPrime.x, fpPrime.y, tpPrime.x, tpPrime.y);
    // 也畫箭頭（藍色）
}
```

> 或另一做法：在設定 highlight 時改變線段顏色欄位。  
> 建議以獨立 `isHighlight` flag 控制，避免與 `isSelect` 混淆。

---

## 實作設計 — selectByClick 修改

```java
void selectByClick(MouseEvent e) {
    Point clickPt = e.getPoint();

    // 1. 清除所有 highlight（先清除）
    clearAllLineHighlights();

    // 2. 偵測是否點擊到 port
    boolean hitPort = false;
    for (int i = 0; i < members.size(); i++) {
        JPanel obj = members.elementAt(i);
        int portSide = getClickedPortSide(obj, clickPt);
        if (portSide != -1) {
            // 找出所有連到此 port 的線並 highlight
            highlightLinesConnectedTo(obj, portSide);
            hitPort = true;
            break;
        }
    }
    if (hitPort) return;   // 點擊到 port → 不走物件選取邏輯

    // 3. 走原有物件選取邏輯（原有程式碼）
    boolean isSelect = false;
    selectComp = new Vector<>();
    for (int i = 0; i < members.size(); i++) {
        // ... 原有邏輯不動 ...
    }
    repaintComp();
}
```

### 輔助方法

```java
// 取得點擊點命中的 port side，未命中回傳 -1
int getClickedPortSide(JPanel obj, Point click) {
    final int RADIUS = 8;
    Point loc  = obj.getLocation();
    Dimension sz = obj.getSize();
    AreaDefine ad = new AreaDefine();
    Point[] ports = {
        new Point(loc.x + sz.width / 2, loc.y),
        new Point(loc.x + sz.width,     loc.y + sz.height / 2),
        new Point(loc.x,                loc.y + sz.height / 2),
        new Point(loc.x + sz.width / 2, loc.y + sz.height)
    };
    int[] sides = { ad.TOP, ad.RIGHT, ad.LEFT, ad.BOTTOM };
    for (int i = 0; i < ports.length; i++) {
        if (click.distance(ports[i]) <= RADIUS) {
            return sides[i];
        }
    }
    return -1;
}

// 清除所有線的 highlight
void clearAllLineHighlights() {
    for (Component c : contextPanel.getComponents()) {
        if (c instanceof AssociationLine)
            ((AssociationLine) c).setHighlight(false);
        else if (c instanceof CompositionLine)
            ((CompositionLine) c).setHighlight(false);
        else if (c instanceof GeneralizationLine)
            ((GeneralizationLine) c).setHighlight(false);
        else if (c instanceof DependencyLine)
            ((DependencyLine) c).setHighlight(false);
    }
}

// highlight 連到指定 port 的線
void highlightLinesConnectedTo(JPanel obj, int side) {
    boolean anyConnected = false;
    for (Component c : contextPanel.getComponents()) {
        if (c instanceof AssociationLine) {
            AssociationLine line = (AssociationLine) c;
            if (line.isConnectedToPort(obj, side)) {
                line.setHighlight(true);
                anyConnected = true;
            }
        }
        // ... 同樣處理 CompositionLine、GeneralizationLine、DependencyLine
    }
    if (anyConnected) repaintComp();
}
```

---

## 影響的檔案與修改摘要

| 檔案 | 修改點 |
|------|--------|
| `mod/instance/AssociationLine.java` | 新增 `isConnectedToPort()`、`setHighlight()`、`isHighlight()`；修改 `paintComponent()` 支援 highlight 顏色 |
| `mod/instance/CompositionLine.java` | 同上 |
| `mod/instance/GeneralizationLine.java` | 同上 |
| `mod/instance/DependencyLine.java` | 建立時一併加入（Feature 1） |
| `bgWork/handler/CanvasPanelHandler.java` | 修改 `selectByClick()`；新增 `getClickedPortSide()`、`clearAllLineHighlights()`、`highlightLinesConnectedTo()` |

---

## 驗收標準

- [ ] Select 模式下點擊 BasicClass 的 TOP port → 連到該 port 的線變藍色（或其他 highlight 顏色）
- [ ] 點擊沒有連線的 port → 無任何反應
- [ ] 點擊線本身 → 不觸發 highlight（維持原 select 行為）
- [ ] 點擊畫布空白處 → 所有 highlight 清除
- [ ] 多條線連到同一 port → 全部 highlight
- [ ] UseCase 的 port 同樣有效
