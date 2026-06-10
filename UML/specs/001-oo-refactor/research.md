# Research: OopsUMLEditor 物件導向重構

**Phase**: 0 | **Date**: 2026-06-10 | **Feature**: 001-oo-refactor

---

## Decision 1：Observer 通知策略（Push vs Pull）

**Decision**: 採用 **Push 通知**（`onModelChanged()` 無參數，Canvas 主動向 Model 取得資料重繪）

**Rationale**:
- Pull 模型（Canvas 呼叫 `model.getObjects()` 等）讓 Canvas 控制更新邏輯，更符合 Swing 的 `paintComponent` 工作方式
- `onModelChanged()` 設計為無參數，Canvas 收到通知後直接呼叫 `repaint()`，由 `paintComponent` 統一向 `UMLModel` 取資料繪製
- 避免 Push 帶資料（如傳入 `UMLObject`）造成 Canvas 需要處理增量更新的複雜性

**Alternatives considered**:
- 帶參數 Push：`onObjectAdded(UMLObject obj)` — 過度複雜，本專案規模不需要增量更新
- EventBus/PropertyChangeSupport：引入過多間接層，與課程內容不符

---

## Decision 2：ModelObserver 介面放置位置

**Decision**: 置於 `oops.model` package（`src/oops/model/ModelObserver.java`）

**Rationale**:
- Observer 介面屬於 Model 層對外的通知契約，與 Model 資料類別同 package 最直觀
- 避免新建 `oops.observer` package 只為一個介面（YAGNI）
- Canvas 實作此介面時 import `oops.model.ModelObserver` 語義清晰

**Alternatives considered**:
- 獨立 `oops.observer` package：適合大型系統，本專案過度設計
- 置於 `oops` root package：缺乏語義分類

---

## Decision 3：ShapeFactory / LinkFactory 放置位置

**Decision**: 新建 `oops.factory` package，所有 Factory 介面與具體實作置於此

**Rationale**:
- Factory 類別數量共 7 個（2 介面 + 5 具體實作），獨立 package 有利於 Class Diagram 呈現 Factory Pattern 的結構
- `oops.model` 不應包含建立邏輯；`oops.mode` 與建立行為無直接關係
- 評分 Class Diagram 時，獨立 package 讓模式邊界一目瞭然

**Alternatives considered**:
- 置於 `oops.model`：混淆了資料模型與工廠建立邏輯
- 置於 `oops.mode`：Factory 與 Mode 無繼承或組合關係，不合適

---

## Decision 4：SelectMode 如何取得 UMLModel 資料

**Decision**: `Mode.mousePressed(MouseEvent e, Canvas canvas)` 方法簽章不變，但 Canvas 提供 `getModel(): UMLModel` 方法，讓 Mode 透過 `canvas.getModel()` 存取資料

**Rationale**:
- Mode 目前已透過 `canvas` 參數操作畫布，維持此介面不需要修改 `Mode` 介面簽章
- 保持向後相容，`Mode` 介面不需要新增 `UMLModel` 參數
- `canvas.getModel()` 是最小侵入式的改法，SelectMode 原本的 `canvas.getObjects()`、`canvas.getSelectedObjects()` 等呼叫改為 `canvas.getModel().getObjects()` 等

**Alternatives considered**:
- Mode 直接持有 `UMLModel` 參照（建構子注入）：需大幅修改 ToolPanel 建立 Mode 的方式，且 Mode 與 Model 的耦合增加
- 修改 `Mode` 介面加入 `UMLModel` 參數：破壞介面，影響所有 Mode 實作

---

## Decision 5：Canvas.paintComponent 如何取得資料

**Decision**: Canvas 持有 `UMLModel model` 欄位；`paintComponent` 直接呼叫 `model.getObjects()` 和 `model.getLines()` 進行繪製

**Rationale**:
- 最直接、最可讀的方式，符合 MVC 中 View 向 Model 讀取資料的標準做法
- 無需引入任何快取或資料複製

---

## Decision 6：模式標示方式

**Decision**: 在每個核心類別宣告上方加入單行 `// [Pattern Name]` 註解，而非 Javadoc

**Rationale**:
- 單行註解簡潔，不影響 Javadoc 的用途（Javadoc 用於說明類別職責）
- 評分時一眼可見，不需要展開 Javadoc
- 格式：`// Strategy Pattern`、`// Composite Pattern`、`// Template Method Pattern` 等

---

## 無需研究的確認項目

| 項目 | 結論 |
|------|------|
| 是否引入測試框架 | 否，手動驗收即可（評分不要求自動化測試） |
| 是否需要 Singleton | 否（過度設計，本專案規模不需要） |
| 是否需要 Decorator | 否（Label 功能已足夠，強加反而降低可讀性） |
| 是否修改 build.bat | 否（新增 package 後 `-sourcepath src` 仍可自動找到） |
