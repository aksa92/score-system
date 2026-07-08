package main.scoresystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;
    private String studentName;
    private String className;
    private String gender;
    private String email;
    private String phone;
    private Integer enrolmentYear;

    @TableLogic
    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
