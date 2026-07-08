package main.scoresystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("weekly_report")
public class WeeklyReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;
    private String studentName;
    private Integer year;
    private Integer weekNumber;
    private String wordFilePath;
    private String originalFileName;
    private BigDecimal rawScore;
    private BigDecimal weightedScore;
    private String evaluator;
    private LocalDateTime evaluationTime;
    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
