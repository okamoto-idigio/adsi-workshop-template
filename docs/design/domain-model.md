# ドメインモデル設計

## Entity

### Employee（社員）

社員マスタ。ログインと勤怠記録の主体。

| フィールド | 型 | 制約 | 説明 |
|-----------|-----|------|------|
| id | Long | PK, 自動採番 | 内部ID |
| username | String | UNIQUE, NOT NULL | ログイン用ユーザー名 |
| name | String | NOT NULL | 表示名 |
| role | Role(enum) | NOT NULL | ADMIN / EMPLOYEE |
| version | Long | @Version | 楽観ロック |

### AttendanceRecord（勤怠記録）

1日1レコード。出勤・退勤の打刻を保持する。

| フィールド | 型 | 制約 | 説明 |
|-----------|-----|------|------|
| id | Long | PK, 自動採番 | 内部ID |
| employeeId | Long | FK(Employee), NOT NULL | 社員ID |
| date | LocalDate | NOT NULL | 勤務日 |
| clockInTime | LocalTime | NULL許容 | 出勤時刻 |
| clockOutTime | LocalTime | NULL許容 | 退勤時刻 |
| version | Long | @Version | 楽観ロック |

**ユニーク制約**: (employeeId, date) — 1社員1日1レコード

## Value Object

### WorkDuration（勤務時間）

計算結果を表す値オブジェクト（DB 非永続化、計算時に生成）。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| totalMinutes | int | 実労働時間（分） |
| breakMinutes | int | 休憩控除時間（分） |

**計算ルール**:
```
休憩控除 = (clockInTime < 13:00 AND clockOutTime > 12:00) ? 60分 : 0分
勤務時間 = (clockOutTime - clockInTime) - 休憩控除
```

### Role（ロール）

```java
public enum Role {
    ADMIN,    // 管理者
    EMPLOYEE  // 一般社員
}
```

## 関連図

```
┌──────────────┐       1    *  ┌────────────────────┐
│   Employee   │──────────────▶│  AttendanceRecord  │
│              │               │                    │
│ - username   │               │ - date             │
│ - name       │               │ - clockInTime      │
│ - role       │               │ - clockOutTime     │
└──────────────┘               └────────────────────┘
                                        │
                                        │ 計算で生成
                                        ▼
                               ┌────────────────────┐
                               │   WorkDuration     │
                               │ (Value Object)     │
                               │ - totalMinutes     │
                               │ - breakMinutes     │
                               └────────────────────┘
```

## Repository

| Repository | 主な操作 |
|-----------|---------|
| EmployeeRepository | findByUsername(username), findAll() |
| AttendanceRecordRepository | findByEmployeeIdAndDate(employeeId, date), findByEmployeeIdAndMonth(employeeId, yearMonth), findByMonth(yearMonth) |

## Service

| Service | 責務 |
|---------|------|
| AuthService | ユーザー名でログイン認証、セッション管理 |
| AttendanceService | 打刻（出勤・退勤）、バリデーション、勤怠一覧取得 |
| WorkDurationCalculator | 勤務時間の計算ロジック |

## パッケージ構成

```
com.example.attendance
├── controller/
│   ├── AuthController
│   └── AttendanceController
├── service/
│   ├── AuthService (interface)
│   ├── AuthServiceImpl
│   ├── AttendanceService (interface)
│   └── AttendanceServiceImpl
├── repository/
│   ├── EmployeeRepository
│   └── AttendanceRecordRepository
├── entity/
│   ├── Employee
│   ├── AttendanceRecord
│   └── Role (enum)
├── dto/
│   ├── LoginRequest
│   ├── LoginResponse
│   ├── AttendanceResponse
│   └── MonthlyAttendanceResponse
├── domain/
│   └── WorkDurationCalculator
└── exception/
    ├── GlobalExceptionHandler
    ├── EmployeeNotFoundException
    └── AttendanceValidationException
```
