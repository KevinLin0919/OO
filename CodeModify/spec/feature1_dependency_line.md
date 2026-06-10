# Feature 1 — Dependency Line（虛線箭頭）

## 需求描述

新增第 4 種連線類型 **Dependency Line**，以虛線箭頭呈現，行為與現有 AssociationLine / GeneralizationLine / CompositionLine 一致。

---

## 視覺規格

| 項目 | 規格 |
|------|------|
| 線段樣式 | 虛線（dashed），使用 `BasicStroke` 設定 dash pattern |
| 箭頭位置 | 位於 **to 端**（目標物件側） |
| 箭頭樣式 | 開放三角箭頭（hollow arrow），與 GeneralizationLine 相同 |
| 選取狀態 | 顯示兩端的黑色小方塊（與其他 line 一致） |

### Dash Pattern 參考

```java
float[] dashPattern = { 8f, 4f };
Graphics2D g2d = (Graphics2D) g;
g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, 10f, dashPattern, 0f));
```

---

## 工具列規格

- **按鈕位置：** index 6（排在 Use Case 按鈕之後）
- **圖示檔：** `icon/dependency_line.jpg`（需複製現有圖示作為暫時替代）
- **FuncPanelHandler.getIcon(6)** 必須回傳非 null，使 init() 迴圈能建立按鈕

---

## 連線行為規格

與現有 line 相同：
1. 在 Dependency Line 模式下拖曳（from → to）
2. from 和 to 物件必須**不同**
3. from / to 都不能是 contextPanel 本身
4. `members.size() >= 2` 才允許建立
5. 根據拖曳起迄座標的相對位置，透過 `AreaDefine.getArea()` 決定各端的 port 側（TOP/RIGHT/LEFT/BOTTOM）

---

## 影響的檔案與修改摘要

### 新增檔案

| 檔案 | 說明 |
|------|------|
| `mod/instance/DependencyLine.java` | 新 line 類型，實作 IFuncComponent + ILinePainter |

### 修改檔案

| 檔案 | 修改點 |
|------|--------|
| `bgWork/Core.java` | 新增 `isDependencyLine()`；`isLine()` 加 case 回傳 3；`isFuncComponent()` 加 case 回傳 6 |
| `bgWork/handler/FuncPanelHandler.java` | `getIcon(6)` 回傳 dependency_line 圖示；`getFunc(6)` 回傳 `new DependencyLine(...)` |
| `bgWork/handler/CanvasPanelHandler.java` | `ActionPerformed(DragPack)` 的 line case 加入 `case 6`；`addLine()` switch 加 `case 3`；`setSelectAllType()` 加 `case 6` |

---

## DependencyLine 類別規格

```
package mod.instance

class DependencyLine extends JPanel implements IFuncComponent, ILinePainter
  fields:
    JPanel from, to
    int fromSide, toSide
    Point fp, tp
    int arrowSize = 6
    int panelExtendSize = 10
    boolean isSelect = false
    int selectBoxSize = 5
    CanvasPanelHandler cph

  methods:
    paintComponent(Graphics g)        — 畫虛線 + 箭頭 + select 標記
    paintArrow(Graphics g, Point pt)  — 開放三角箭頭（同 GeneralizationLine）
    setConnect(DragPack dPack)        — 設定 from/to 及 side
    renewConnect()                    — 更新 fp/tp，呼叫 reSize()
    reSize()                          — 更新 JPanel 邊界
    getConnectPoint(JPanel jp, int side) — 取得連接點絕對坐標
    paintSelect(Graphics gra)         — 畫選取標記
    setSelect(boolean) / isSelect()   — 選取狀態
    isConnectedToPort(JPanel obj, int side) — Feature 4 用，判斷是否連到指定 port
    setHighlight(boolean) / isHighlight()   — Feature 4 用，highlight 狀態
```

---

## 驗收標準

- [ ] 工具列出現第 6 個按鈕
- [ ] 切換到 Dependency 模式後可拖曳建立虛線箭頭
- [ ] 虛線清晰可見，箭頭指向 to 端
- [ ] 選取後顯示兩端黑色方塊
- [ ] 同一物件兩端不能建立（與現有 line 行為一致）
