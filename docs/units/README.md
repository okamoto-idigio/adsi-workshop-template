# Unit of Work — 依存図と Phase

## 全体構成

```
Phase A: 共通基盤（1人で実装 → 2人が利用）
    │
    ├─────────────────────┐
    ▼                     ▼
Phase B-1: 認証＋打刻     Phase B-2: 勤怠一覧
  (担当者A)                (担当者B)
    │                     │
    └─────────────────────┘
              │
              ▼
Phase C: 統合テスト（合流）
```

## Phase 割り当て

| Phase | Unit | 担当 | 依存先 | 説明 |
|-------|------|------|--------|------|
| A | unit_00_foundation | 共同 | なし | プロジェクト骨格・DB・Entity・共通部品 |
| B-1 | unit_01_auth_clock | 担当者A | unit_00 | ログイン + 出退勤打刻 |
| B-2 | unit_02_attendance_list | 担当者B | unit_00 | 月次勤怠一覧（自分 + 管理者） |
| C | 統合テスト | 共同 | unit_01, unit_02 | E2E シナリオ |

## 並列実装のポイント

- Phase A で Entity / Repository / DTO / Enum / Flyway を確定する
- Phase B-1 と B-2 は **互いに依存しない**（同じ Entity・Repository を使うが実装は独立）
- B-1 の AuthService が返す「ログインユーザー」は session に保存 → B-2 はそれを読むだけ
- Phase B で合意が必要なのは **セッション属性の形状**（LoginResponse 相当）のみ → Phase A で定義済み

## 技術スタック

- Backend: Java 21 / Spring Boot 3.x
- Frontend: TypeScript / Next.js 14 (App Router)
- DB: H2 (ファイルモード) / Flyway
- テスト: JUnit 5 / Vitest + Testing Library
