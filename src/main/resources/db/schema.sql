-- =============================================
-- 学生综合评分管理系统 - 数据库建表脚本
-- Database: score_system
-- =============================================

CREATE DATABASE IF NOT EXISTS score_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE score_system;

-- 1. 学生信息表
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id              BIGINT          AUTO_INCREMENT  PRIMARY KEY   COMMENT '主键ID',
    student_id      VARCHAR(20)     NOT NULL        UNIQUE        COMMENT '学号（唯一标识）',
    student_name    VARCHAR(100)    NOT NULL                      COMMENT '学生姓名',
    class_name      VARCHAR(100)    DEFAULT NULL                  COMMENT '班级',
    gender          VARCHAR(10)     DEFAULT NULL                  COMMENT '性别',
    email           VARCHAR(100)    DEFAULT NULL                  COMMENT '邮箱',
    phone           VARCHAR(20)     DEFAULT NULL                  COMMENT '手机号',
    enrolment_year  INT             DEFAULT NULL                  COMMENT '入学年份',
    is_active       TINYINT(1)      DEFAULT 1                     COMMENT '状态：1-启用 0-禁用',
    created_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_name (student_name),
    INDEX idx_class_name (class_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表';

-- 2. 考勤评分表
DROP TABLE IF EXISTS attendance;
CREATE TABLE attendance (
    id              BIGINT          AUTO_INCREMENT  PRIMARY KEY   COMMENT '主键ID',
    student_id      VARCHAR(20)     NOT NULL                      COMMENT '学号',
    student_name    VARCHAR(100)    NOT NULL                      COMMENT '学生姓名',
    year            INT             NOT NULL                      COMMENT '年份',
    month           INT             NOT NULL                      COMMENT '月份(1-12)',
    attendance_days INT             DEFAULT 0                     COMMENT '出勤天数',
    absence_days    INT             DEFAULT 0                     COMMENT '缺勤天数',
    raw_score       DECIMAL(5,2)    DEFAULT NULL                  COMMENT '原始考勤分数（默认满分100）',
    weighted_score  DECIMAL(5,2)    DEFAULT NULL                  COMMENT '权重折算后分数（原始分×0.2）',
    remarks         VARCHAR(500)    DEFAULT NULL                  COMMENT '备注',
    created_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_attendance_student (student_id),
    INDEX idx_attendance_period (year, month),
    UNIQUE KEY uk_student_period (student_id, year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤评分表';

-- 3. 周报评分表
DROP TABLE IF EXISTS weekly_report;
CREATE TABLE weekly_report (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id          VARCHAR(20)     NOT NULL                    COMMENT '学号',
    student_name        VARCHAR(100)    NOT NULL                    COMMENT '学生姓名',
    year                INT             NOT NULL                    COMMENT '年份',
    week_number         INT             NOT NULL                    COMMENT '周数(1-53)',
    word_file_path      VARCHAR(500)    DEFAULT NULL                COMMENT 'Word文件存储路径',
    original_file_name  VARCHAR(255)    DEFAULT NULL                COMMENT '文件原始名称',
    raw_score           DECIMAL(5,2)    DEFAULT NULL                COMMENT '原始周报分数（默认满分100）',
    weighted_score      DECIMAL(5,2)    DEFAULT NULL                COMMENT '权重折算后分数（原始分×0.3）',
    evaluator           VARCHAR(100)    DEFAULT NULL                COMMENT '评分人',
    evaluation_time     DATETIME        DEFAULT NULL                COMMENT '评分时间',
    remarks             VARCHAR(500)    DEFAULT NULL                COMMENT '备注',
    created_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_wr_student (student_id),
    INDEX idx_wr_week (year, week_number),
    UNIQUE KEY uk_student_week (student_id, year, week_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报评分表';

-- 4. 作业评分表
DROP TABLE IF EXISTS homework;
CREATE TABLE homework (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id          VARCHAR(20)     NOT NULL                    COMMENT '学号',
    student_name        VARCHAR(100)    NOT NULL                    COMMENT '学生姓名',
    homework_id         VARCHAR(50)     NOT NULL UNIQUE             COMMENT '作业编号（全局唯一）',
    homework_name       VARCHAR(200)    NOT NULL                    COMMENT '作业名称',
    submission_content  TEXT            DEFAULT NULL                COMMENT '作业提交内容',
    file_path           VARCHAR(500)    DEFAULT NULL                COMMENT '文件存储路径',
    original_file_name  VARCHAR(255)    DEFAULT NULL                COMMENT '文件原始名称',
    raw_score           DECIMAL(5,2)    DEFAULT NULL                COMMENT '原始作业分数（默认满分100）',
    weighted_score      DECIMAL(5,2)    DEFAULT NULL                COMMENT '权重折算后分数（原始分×0.5）',
    evaluator           VARCHAR(100)    DEFAULT NULL                COMMENT '评分人',
    evaluation_time     DATETIME        DEFAULT NULL                COMMENT '评分时间',
    created_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_hw_student (student_id),
    INDEX idx_hw_id (homework_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业评分表';

-- 5. 综合评分汇总表
DROP TABLE IF EXISTS comprehensive_score;
CREATE TABLE comprehensive_score (
    id                          BIGINT          AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id                  VARCHAR(20)     NOT NULL UNIQUE             COMMENT '学号',
    student_name                VARCHAR(100)    NOT NULL                    COMMENT '学生姓名',
    attendance_raw_score        DECIMAL(5,2)    DEFAULT NULL                COMMENT '考勤原始平均分',
    attendance_weighted_score   DECIMAL(5,2)    DEFAULT NULL                COMMENT '考勤权重折算分',
    weekly_report_raw_score     DECIMAL(5,2)    DEFAULT NULL                COMMENT '周报原始平均分',
    weekly_report_weighted_score DECIMAL(5,2)   DEFAULT NULL                COMMENT '周报权重折算分',
    homework_raw_score          DECIMAL(5,2)    DEFAULT NULL                COMMENT '作业原始平均分',
    homework_weighted_score     DECIMAL(5,2)    DEFAULT NULL                COMMENT '作业权重折算分',
    total_weighted_score        DECIMAL(5,2)    DEFAULT NULL                COMMENT '综合总分',
    ranking                     INT             DEFAULT NULL                COMMENT '排名',
    calculation_time            DATETIME        DEFAULT NULL                COMMENT '最近计算时间',
    created_time                DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time                DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_cs_student (student_id),
    INDEX idx_cs_total (total_weighted_score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='综合评分汇总表';
