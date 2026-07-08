package main.scoresystem.common.constants;

import java.math.BigDecimal;

public final class WeightConstants {

    public static final BigDecimal ATTENDANCE_WEIGHT = new BigDecimal("0.2");
    public static final BigDecimal WEEKLY_REPORT_WEIGHT = new BigDecimal("0.3");
    public static final BigDecimal HOMEWORK_WEIGHT = new BigDecimal("0.5");
    public static final BigDecimal DEFAULT_MAX_SCORE = new BigDecimal("100");

    private WeightConstants() {}
}
