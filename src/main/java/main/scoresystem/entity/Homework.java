package main.scoresystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("homework")
public class Homework {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;
    private String studentName;
    private String homeworkId;
    private String homeworkName;
    private String submissionContent;
    private String filePath;
    private String originalFileName;
    private BigDecimal rawScore;
    private BigDecimal weightedScore;
    private String evaluator;
    private LocalDateTime evaluationTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
