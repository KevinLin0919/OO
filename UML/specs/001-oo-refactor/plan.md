# Implementation Plan: OopsUMLEditor 物件導向重構

**Branch**: `001-oo-refactor` | **Date**: 2026-06-10 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-oo-refactor/spec.md`

---

## Summary

將現有 Java Swing UML Editor 進行物件導向重構，在完整保留所有功能的前提下：
1. 抽出 `UMLModel` 實現 MVC 分層（Canvas 只負責繪製）
2. 新增 `ShapeFactory`/`LinkFactory` 介面實現 Factory Method Pattern
3. 新增 `ModelObserver` 介面實現 Observer Pattern（Model 通知 View）
4. 在程式碼與 Class Diagram 中明確標示六種設計模式

---

## Technical Context

**Language/Version**: Java 11+（與現有 build.bat 一致，無需更改編譯設定）
**Primary Dependencies**: Java Swing（JDK 內建），無外部依賴
**Storage**: N/A（無持久化需求）
**Testing**: 手動功能驗收測試（無自動化測試框架）
**Target Platform**: Windows Desktop（build.bat / run.bat），跨平台 Look & Feel
**Project Type**: Desktop GUI Application
**Performance Goals**: UI 互動流暢，無明顯卡頓（Swing EDT 單執行緒，不需多執行緒考量）
**Constraints**: 不引入任何外部 library；不更改 build.bat / run.bat；保持 `javac -sourcepath src` 可編譯
**Scale/Scope**: 單使用者桌面工具，約 20 個 Java 原始碼檔案，重構後約 28 個

---

## Constitution Check

*Constitution 目前為空白模板，尚未針對本專案設定原則，故無 gate 需要驗證。*

以下為本次重構自訂的品質門檻（非 constitution gate，作為自我檢核）：

| 檢核項目 | 狀態 |
|---------|------|
| `UMLModel` 無任何 `javax.swing` import | 需驗收 |
| `Canvas` 無 business logic 方法 | 需驗收 |
| `CreateObjectMode` / `CreateLinkMode` 無具體類別直接 `new` | 需驗收 |
| 所有原有功能操作正常 | 需驗收 |
| Class Diagram 與程式碼 100% 對應 | 需驗收 |

---

## Project Structure

### Documentation (this feature)

```text
specs/001-oo-refactor/
├── plan.md              # 本文件
├── research.md          # Phase 0：技術決策紀錄
├── data-model.md        # Phase 1：類別結構與介面設計
├── contracts/           # Phase 1：介面契約（ShapeFactory、LinkFactory、ModelObserver）
│   ├── ShapeFactory.md
│   ├── LinkFactory.md
│   └── ModelObserver.md
└── tasks.md             # Phase 2 輸出（由 /speckit.tasks 產生）
```

### Source Code (repository root)

```text
src/oops/
├── UMLEditor.java          # Controller（主視窗、選單）- 不變動結構
├── Canvas.java             # View（精簡：只繪製 + 滑鼠事件轉發）- 大幅修改
└── ToolPanel.java          # View（注入 Factory 給 Mode）- 小幅修改

src/oops/model/
├── UMLObject.java          # 抽象（Template Method）- 不變動
├── RectObject.java         # 不變動
├── OvalObject.java         # 不變動
├── CompositeObject.java    # Composite Pattern - 不變動
├── Port.java               # 不變動
├── ConnectionLine.java     # 抽象（Template Method）- 不變動
├── AssociationLine.java    # 不變動
├── GeneralizationLine.java # 不變動
├── CompositionLine.java    # 不變動
├── UMLModel.java           # ★NEW：Model，管所有狀態與 business logic
└── ModelObserver.java      # ★NEW：Observer 介面

src/oops/factory/           # ★NEW package
├── ShapeFactory.java       # 介面（Factory Pattern）
├── RectShapeFactory.java   # 具體工廠
├── OvalShapeFactory.java   # 具體工廠
├── LinkFactory.java        # 介面（Factory Pattern）
├── AssociationLinkFactory.java    # 具體工廠
├── GeneralizationLinkFactory.java # 具體工廠
└── CompositionLinkFactory.java    # 具體工廠

src/oops/mode/
├── Mode.java               # Strategy Pattern 介面 - 不變動
├── SelectMode.java         # 改用 UMLModel API 取代 Canvas 直接呼叫
├── CreateObjectMode.java   # 改吃 ShapeFactory 注入
└── CreateLinkMode.java     # 改吃 LinkFactory 注入
```

**Structure Decision**: 單一 Java 桌面專案，保留現有 `src/oops` 根結構，新增 `oops.model.UMLModel`、`oops.model.ModelObserver`，以及全新的 `oops.factory` package。

---

## Complexity Tracking

> 無 Constitution 違規，略過此節。
