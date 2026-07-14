# Unit 01: 認証 + 打刻

## 概要

ユーザー名でのログインと、出勤・退勤の打刻機能を実装する。

## ユーザーストーリー

| ID | ストーリー |
|----|-----------|
| US-01 | 社員として、ユーザー名を入力してログインしたい |
| US-02 | 社員として、出勤ボタンを押して出勤時刻を記録したい |
| US-03 | 社員として、退勤ボタンを押して退勤時刻を記録したい |

## 依存

- **unit_00_foundation**: Entity, Repository, DTO, WorkDurationCalculator

## スコープ

### Backend

| 分類 | 対象 |
|------|------|
| Service | AuthService (interface + impl) |
| Service | AttendanceService (interface + impl) — 打刻部分のみ |
| Controller | AuthController (POST /api/auth/login) |
| Controller | AttendanceController (POST /api/attendance/clock-in, clock-out, GET /api/attendance/status) |
| セッション | HttpSession にログインユーザー情報を保存 |
| セキュリティ | 未ログイン時の 401 レスポンス |

### Frontend

| 分類 | 対象 |
|------|------|
| 画面 | ログイン画面 (`/login`) |
| 画面 | 打刻画面 (`/`) |
| コンポーネント | ClockButton（出退勤ボタン） |
| 状態管理 | ログインユーザー情報のコンテキスト or cookie 管理 |

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/auth/login | ログイン |
| POST | /api/attendance/clock-in | 出勤打刻 |
| POST | /api/attendance/clock-out | 退勤打刻 |
| GET | /api/attendance/status | 当日の打刻状態取得 |

## バリデーションルール

- ログイン: 未登録ユーザー名 → 401
- 出勤: 同日に既に出勤済み → 400
- 退勤: 未出勤 → 400、同日に既に退勤済み → 400

## テスト

### Backend

| テスト | 内容 |
|--------|------|
| AuthServiceTest | 正常ログイン、存在しないユーザーで例外 |
| AttendanceServiceTest（打刻） | 出勤成功、出勤重複エラー、退勤成功、未出勤で退勤エラー、退勤重複エラー |
| AuthControllerTest (@WebMvcTest) | POST /api/auth/login の正常・エラーレスポンス |
| AttendanceControllerTest (@WebMvcTest) | clock-in/clock-out の正常・エラーレスポンス、未ログイン時 401 |

### Frontend

| テスト | 内容 |
|--------|------|
| ログイン画面 | ユーザー名入力 → ログイン成功でリダイレクト、エラー表示 |
| 打刻画面 | ボタン状態の切り替え、打刻成功時の状態更新 |

## 完了条件

- [ ] ユーザー名でログインできる
- [ ] 出勤・退勤が打刻できる
- [ ] バリデーションエラーが適切に返される
- [ ] 全テストが通過する
