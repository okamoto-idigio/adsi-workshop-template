# Unit 02: 勤怠一覧

## 概要

社員の月次勤怠一覧（自分）と、管理者向けの全社員勤怠一覧を実装する。

## ユーザーストーリー

| ID | ストーリー |
|----|-----------|
| US-04 | 社員として、自分の月次勤怠一覧を確認したい |
| US-05 | 管理者として、全社員の月次勤怠一覧を確認したい |

## 依存

- **unit_00_foundation**: Entity, Repository, DTO, WorkDurationCalculator
- **unit_01 との接点**: セッションに保存されたログインユーザー情報を読むだけ（unit_01 の実装を待たずに MockSession でテスト可能）

## スコープ

### Backend

| 分類 | 対象 |
|------|------|
| Service | AttendanceService (interface + impl) — 一覧取得部分 |
| Controller | AttendanceController (GET /api/attendance/me, GET /api/attendance/all) |
| Controller | EmployeeController (GET /api/employees) |
| 認可 | /api/attendance/all, /api/employees は ADMIN ロールのみ → 403 |

### Frontend

| 分類 | 対象 |
|------|------|
| 画面 | 勤怠一覧 (`/attendance`) |
| 画面 | 管理者一覧 (`/admin/attendance`) |
| コンポーネント | AttendanceTable（勤怠テーブル） |
| コンポーネント | MonthSelector（月選択） |
| コンポーネント | EmployeeSelector（社員選択ドロップダウン） |

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/attendance/me?year=&month= | 自分の月次勤怠 |
| GET | /api/attendance/all?year=&month=&employeeId= | 全社員の月次勤怠（管理者用） |
| GET | /api/employees | 社員一覧（管理者用、ドロップダウン用） |

## 認可ルール

- /api/attendance/me: ログイン済みユーザー全員
- /api/attendance/all: ADMIN ロールのみ（EMPLOYEE → 403）
- /api/employees: ADMIN ロールのみ（EMPLOYEE → 403）

## テスト

### Backend

| テスト | 内容 |
|--------|------|
| AttendanceServiceTest（一覧） | 月次データ取得、勤務時間計算が正しいこと、データなし月は空リスト |
| AttendanceControllerTest (@WebMvcTest) | GET /me 正常、GET /all 正常（ADMIN）、GET /all で EMPLOYEE → 403 |
| EmployeeControllerTest (@WebMvcTest) | GET /employees 正常（ADMIN）、EMPLOYEE → 403 |

### Frontend

| テスト | 内容 |
|--------|------|
| 勤怠一覧画面 | 月選択で API 呼び出し、テーブル表示 |
| 管理者一覧画面 | 社員選択＋月選択でフィルタリング、権限なし時のリダイレクト |
| MonthSelector | 前月・翌月ボタンで値が変わる |
| EmployeeSelector | 社員一覧表示、選択時にコールバック発火 |

## 完了条件

- [ ] 自分の月次勤怠一覧が正しく表示される（勤務時間計算含む）
- [ ] 管理者が全社員の勤怠を閲覧できる
- [ ] 社員選択でフィルタリングできる
- [ ] 一般社員が管理者画面にアクセスすると 403 になる
- [ ] 全テストが通過する
