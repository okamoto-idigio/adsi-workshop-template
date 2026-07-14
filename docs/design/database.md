# DB 設計

## 技術選定

- **DB**: H2 Database（ファイルモード）
- **マイグレーション**: Flyway
- **接続URL**: `jdbc:h2:file:./data/attendance`

## テーブル定義

### employees（社員マスタ）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 内部ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | ログイン用ユーザー名 |
| name | VARCHAR(100) | NOT NULL | 表示名 |
| role | VARCHAR(20) | NOT NULL | ADMIN / EMPLOYEE |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |

### attendance_records（勤怠記録）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 内部ID |
| employee_id | BIGINT | FK(employees.id), NOT NULL | 社員ID |
| work_date | DATE | NOT NULL | 勤務日 |
| clock_in_time | TIME | NULL | 出勤時刻 |
| clock_out_time | TIME | NULL | 退勤時刻 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |

**ユニーク制約**: `UK_attendance_employee_date (employee_id, work_date)`

## ER 図

```
┌────────────────────────┐          ┌─────────────────────────────┐
│       employees        │          │     attendance_records       │
├────────────────────────┤          ├─────────────────────────────┤
│ * id          BIGINT   │◀─┐      │ * id             BIGINT     │
│   username    VARCHAR  │   │      │   employee_id    BIGINT  FK │──┐
│   name        VARCHAR  │   └──────│   work_date      DATE       │  │
│   role        VARCHAR  │          │   clock_in_time  TIME       │  │
│   version     BIGINT   │          │   clock_out_time TIME       │  │
└────────────────────────┘          │   version        BIGINT     │  │
                                    └─────────────────────────────┘  │
                                              │                       │
                                              └───────────────────────┘
                                    UK: (employee_id, work_date)
```

## Flyway マイグレーション

### V1__create_tables.sql

```sql
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    clock_in_time TIME,
    clock_out_time TIME,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT uk_attendance_employee_date UNIQUE (employee_id, work_date)
);
```

### V2__insert_master_data.sql

```sql
INSERT INTO employees (username, name, role) VALUES
    ('yamada', '山田太郎', 'ADMIN'),
    ('tanaka', '田中花子', 'EMPLOYEE'),
    ('suzuki', '鈴木一郎', 'EMPLOYEE'),
    ('sato', '佐藤美咲', 'EMPLOYEE'),
    ('takahashi', '高橋健太', 'EMPLOYEE');
```

## インデックス

- `employees.username` — UNIQUE 制約により自動作成
- `attendance_records (employee_id, work_date)` — UNIQUE 制約により自動作成

ワークショップ規模（5名・数十レコード）では追加インデックス不要。
