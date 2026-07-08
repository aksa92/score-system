package main.scoresystem.dto;

import lombok.Data;

@Data
public class QueryDTO {
    private int page = 1;
    private int size = 10;
    private String studentId;
    private String studentName;
    private String className;
    private Integer year;
    private Integer month;
    private Integer weekNumber;
    private String homeworkId;
    private String homeworkName;
    private String evaluator;
    private Double scoreRangeMin;
    private Double scoreRangeMax;
}
