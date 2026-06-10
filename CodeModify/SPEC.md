# UML Editor — New Features Specification

> 專案路徑：`Project/src/`  
> 規格依據：`Code Modify 需求 (AI 協作).pdf`  
> 限制：**不得重構整個專案架構**，專注於新增功能。

---

## 專案架構速覽

```
bgWork/
  Core.java               — 型別判斷 (isLine / isClass / isFuncComponent)
  handler/
    CanvasPanelHandler.java — 畫布互動核心 (members = 物件列表，不含 line)
    FuncPanelHandler.java   — 工具列 (index 0-5，新增 6)
Listener/
  CPHActionListener.java  — Mouse 事件 (已實作 MouseMotionListener 但未掛載)
mod/instance/
  BasicClass.java / UseCase.java   — 物件
  AssociationLine / CompositionLine / GeneralizationLine — 線
Define/
  AreaDefine.java          — TOP=3, RIGHT=2, LEFT=1, BOTTOM=0
```

### 關鍵架構限制

| 事項 | 說明 |
|------|------|
| `members` | 只含 BasicClass / UseCase / GroupContainer，**線不在 members** |
| 線的存放 | 只在 `contextPanel.getComponents()` 中 |
| 單擊事件 | `mouseClicked` → `ActionPerformed(MouseEvent)` |
| 拖曳事件 | 不觸發 mouseClicked；`mouseReleased` → `ActionPerformed(DragPack)` |
| Line 欄位 | `from/to/fromSide/toSide` 為 package-private，需加 public 方法 |

---

## Feature 列表

| # | 功能 | 規格文件 |
|---|------|----------|
| 1 | Dependency Line（虛線箭頭） | [spec/feature1_dependency_line.md](spec/feature1_dependency_line.md) |
| 2 | Port 紅點提示（line 模式下） | [spec/feature2_port_hint.md](spec/feature2_port_hint.md) |
| 3 | 拖曳建立指定大小物件 | [spec/feature3_drag_create.md](spec/feature3_drag_create.md) |
| 4 | Select 模式點擊 port highlight 線 | [spec/feature4_port_highlight.md](spec/feature4_port_highlight.md) |

---

## 實作任務清單

詳見 [TASKS.md](TASKS.md)
