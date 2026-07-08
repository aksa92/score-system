package main.scoresystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("comprehensive_score")
public class ComprehensiveScore {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;
    private String studentName;
    private BigDecimal attendanceRawScore;
    private BigDecimal attendanceWeightedScore;
    private BigDecimal weeklyReportRawScore;
    private BigDecimal weeklyReportWeightedScore;
    private BigDecimal homeworkRawScore;
    private BigDecimal homeworkWeightedScore;
    private BigDecimal totalWeightedScore;
    private Integer ranking;
    private LocalDateTime calculationTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
