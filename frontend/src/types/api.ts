export type Role = "ADMIN" | "EMPLOYEE";

export interface LoginRequest {
  username: string;
}

export interface LoginResponse {
  id: number;
  username: string;
  name: string;
  role: Role;
}

export interface AttendanceResponse {
  date: string;
  clockInTime: string | null;
  clockOutTime: string | null;
  workDurationMinutes: number | null;
}

export interface MonthlyAttendanceResponse {
  year: number;
  month: number;
  records: AttendanceResponse[];
}

export interface EmployeeMonthlyAttendance {
  employeeId: number;
  employeeName: string;
  records: AttendanceResponse[];
}

export interface EmployeeSummary {
  id: number;
  name: string;
}

export interface ErrorResponse {
  message: string;
  code: string;
}
