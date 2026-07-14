package com.example.attendance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkDurationCalculatorTest {

    private final WorkDurationCalculator calculator = new WorkDurationCalculator();

    @Test
    @DisplayName("9:00-18:00: 休憩60分を控除し480分")
    void calculate_fullDay_returnsEightHours() {
        int minutes = calculator.calculateWorkMinutes(
                LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertThat(minutes).isEqualTo(480);
    }

    @Test
    @DisplayName("9:00-12:00: 休憩時間をまたがないため控除なし180分")
    void calculate_morningOnly_noBreakDeduction() {
        int minutes = calculator.calculateWorkMinutes(
                LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertThat(minutes).isEqualTo(180);
    }

    @Test
    @DisplayName("13:00-18:00: 休憩時間をまたがないため控除なし300分")
    void calculate_afternoonOnly_noBreakDeduction() {
        int minutes = calculator.calculateWorkMinutes(
                LocalTime.of(13, 0), LocalTime.of(18, 0));

        assertThat(minutes).isEqualTo(300);
    }

    @Test
    @DisplayName("10:00-15:00: 休憩60分を控除し240分")
    void calculate_spanningLunch_deductsBreak() {
        int minutes = calculator.calculateWorkMinutes(
                LocalTime.of(10, 0), LocalTime.of(15, 0));

        assertThat(minutes).isEqualTo(240);
    }

    @Test
    @DisplayName("休憩控除: 出勤が13:00未満かつ退勤が12:00超なら60分")
    void getBreakMinutes_spanningLunch_returns60() {
        int breakMinutes = calculator.getBreakMinutes(
                LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertThat(breakMinutes).isEqualTo(60);
    }

    @Test
    @DisplayName("休憩控除: 退勤が12:00以前なら0分")
    void getBreakMinutes_beforeNoon_returnsZero() {
        int breakMinutes = calculator.getBreakMinutes(
                LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertThat(breakMinutes).isEqualTo(0);
    }
}
