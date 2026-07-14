# Unit 00: 共通基盤

## 概要

プロジェクトの骨格を作り、全 Unit が依存する共通部品（Entity / Repository / DTO / Enum / DB マイグレーション / 例外ハンドラ）をテスト付きで実装する。

## ユーザーストーリー

（直接的なストーリーはないが、全ストーリーの前提となる基盤）

## スコープ

### Backend

| 分類 | 対象 |
|------|------|
| プロジェクト初期化 | Spring Boot プロジェクト（Gradle）、依存定義 |
| Flyway | V1__create_tables.sql, V2__insert_master_data.sql |
| Entity | Employee, AttendanceRecord |
| Enum | Role (ADMIN, EMPLOYEE) |
| Repository | EmployeeRepository, AttendanceRecordRepository |
| DTO | LoginRequest, LoginResponse, AttendanceResponse, MonthlyAttendanceResponse, EmployeeMonthlyAttendance, EmployeeSummary, ErrorResponse |
| Value Object | WorkDurationCalculator |
| 例外 | GlobalExceptionHandler, EmployeeNotFoundException, AttendanceValidationException |
| 設定 | application.yml (H2, Flyway, Session) |
| テスト | Repository テスト（@DataJpaTest）、WorkDurationCalculator ユニットテスト |

### Frontend

| 分類 | 対象 |
|------|------|
| プロジェクト初期化 | Next.js (App Router)、Tailwind CSS |
| API クライアント | fetch ラッパー（withBasePath 対応）、型定義 |
| 共通コンポーネント | Header（ナビゲーション）、レイアウト |
| 設定 | next.config.js（proxy 設定: /api → backend:8080） |

## テーブル

- employees（社員マスタ）
- attendance_records（勤怠記録）

## API

なし（この Unit では API エンドポイントは作らない。Service interface の定義まで）

## テスト

| テスト | 内容 |
|--------|------|
| EmployeeRepositoryTest | findByUsername で正常取得、存在しないユーザーで empty |
| AttendanceRecordRepositoryTest | save → findByEmployeeIdAndDate、ユニーク制約違反 |
| WorkDurationCalculatorTest | 計算ルール全パターン（9:00-18:00, 9:00-12:00, 13:00-18:00, 10:00-15:00） |

## 完了条件

- [ ] `./gradlew test` が全テスト通過する
- [ ] Flyway マイグレーションが正常に実行される（H2 起動時に5名のマスタデータが入る）
- [ ] Frontend の `npm run dev` でページが表示される（Header のみでOK）
- [ ] API クライアントの型定義が揃っている

## 次のステップ

→ unit_01（認証+打刻）と unit_02（勤怠一覧）を並列で着手
