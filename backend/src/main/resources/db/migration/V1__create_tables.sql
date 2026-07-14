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
