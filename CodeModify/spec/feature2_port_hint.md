# Feature 2 — Port 紅點提示（Line 模式下）

## 需求描述

在**任何建立 line 的模式**（index 1, 2, 3, 6）下，滑鼠靠近任一物件的 port 時，該 port 顯示紅色小圓點作為提示；滑鼠離開 port 範圍後圓點消失。

---

## Port 位置定義

每個物件（BasicClass / UseCase）有 4 個 port，皆為**物件邊緣中心點**：

| Port | 座標（相對 contextPanel） |
|------|--------------------------|
| TOP    | `(x + w/2, y)` |
| RIGHT  | `(x + w,   y + h/2)` |
| LEFT   | `(x,       y + h/2)` |
| BOTTOM | `(x + w/2, y + h)` |

> `x, y` = `obj.getLocation()`，`w, h` = `obj.getSize()`

---

## 觸發條件

| 條件 | 說明 |
|------|------|
| 觸發模式 | `core.getCurrentFuncIndex()` 為 1, 2, 3, 6 |
| 觸發距離 | 滑鼠座標距任一 port 點 ≤ `PORT_HINT_RADIUS`（建議 10px） |
| 消失條件 | 滑鼠離開 port 範圍（距離 > threshold）或切換到非 line 模式 |

---

## 實作設計

### 1. 啟用 MouseMotionListener

`CPHActionListener` 已實作 `MouseMotionListener`，但目前未掛載。  
需在 `CanvasPanelHandler.initContextPanel()` 補上：

```java
CPHActionListener listener = new CPHActionListener(this);
contextPanel.addMouseListener(listener);
contextPanel.addMouseMotionListener(listener);   // 新增這行
```

### 2. 新增 hoverPoint 欄位

在 `CanvasPanelHandler` 新增：

```java
Point portHoverPoint = null;   // 目前滑鼠位置（line 模式下追蹤）
```

新增 public 方法：

```java
public void setPortHoverPoint(Point p) {
    portHoverPoint = p;
    contextPanel.repaint();
}
```

### 3. mouseMoved 回呼

`CPHActionListener.mouseMoved()` 補上：

```java
@Override
public void mouseMoved(MouseEvent e) {
    if (handler instanceof CanvasPanelHandler) {
        ((CanvasPanelHandler) handler).setPortHoverPoint(e.getPoint());
    }
}
```

### 4. contextPanel 改為匿名 JPanel（覆寫 paintComponent）

在 `initContextPanel()` 中將 `new JPanel()` 改為匿名子類，在 super 呼叫之後額外繪製紅點：

```java
contextPanel = new JPanel() {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPortHints(g);
    }
};
```

### 5. drawPortHints 方法

新增私有方法：

```java
private void drawPortHints(Graphics g) {
    int funcIdx = core.getCurrentFuncIndex();
    // 僅在 line 模式（1,2,3,6）下且有 hover 點時繪製
    if (funcIdx < 1 || (funcIdx > 3 && funcIdx != 6)) return;
    if (portHoverPoint == null) return;

    final int RADIUS = 10;
    final int DOT_R  = 5;
    g.setColor(Color.RED);

    for (JPanel obj : members) {
        Point loc  = obj.getLocation();
        Dimension sz = obj.getSize();
        Point[] ports = {
            new Point(loc.x + sz.width / 2, loc.y),                    // TOP
            new Point(loc.x + sz.width,     loc.y + sz.height / 2),    // RIGHT
            new Point(loc.x,                loc.y + sz.height / 2),    // LEFT
            new Point(loc.x + sz.width / 2, loc.y + sz.height)         // BOTTOM
        };
        for (Point port : ports) {
            double dist = portHoverPoint.distance(port);
            if (dist <= RADIUS) {
                g.fillOval(port.x - DOT_R, port.y - DOT_R,
                           DOT_R * 2, DOT_R * 2);
            }
        }
    }
}
```

---

## 影響的檔案與修改摘要

| 檔案 | 修改點 |
|------|--------|
| `bgWork/handler/CanvasPanelHandler.java` | 新增 `portHoverPoint` 欄位、`setPortHoverPoint()`、`drawPortHints()`；contextPanel 改匿名 JPanel；initContextPanel 補 addMouseMotionListener |
| `Listener/CPHActionListener.java` | `mouseMoved()` 呼叫 `setPortHoverPoint()` |

---

## 驗收標準

- [ ] 切換到 Association / Generalization / Composition / Dependency 模式
- [ ] 滑鼠靠近物件邊緣中心 ≤ 10px 時，出現紅色圓點
- [ ] 滑鼠離開後，紅點消失
- [ ] 在 Select 模式（index 0）或物件模式（4, 5）下，**不顯示**紅點
- [ ] 多個物件的 port 同時可顯示（各自獨立判斷）
