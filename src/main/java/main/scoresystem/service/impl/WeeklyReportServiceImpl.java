package main.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import main.scoresystem.common.constants.WeightConstants;
import main.scoresystem.common.exception.BusinessException;
import main.scoresystem.entity.Student;
import main.scoresystem.entity.WeeklyReport;
import main.scoresystem.mapper.WeeklyReportMapper;
import main.scoresystem.service.StudentService;
import main.scoresystem.service.WeeklyReportService;
import main.scoresystem.utils.FileUtils;
import main.scoresystem.vo.PageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WeeklyReportServiceImpl extends ServiceImpl<WeeklyReportMapper, WeeklyReport> implements WeeklyReportService {

    @Value("${app.upload.base-path:./uploads}")
    private String uploadBasePath;

    private final StudentService studentService;

    public WeeklyReportServiceImpl(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public WeeklyReport createWithFile(WeeklyReport weeklyReport, MultipartFile file) {
        Student student = studentService.getByStudentId(weeklyReport.getStudentId());
        if (student == null) {
            throw new BusinessException(400, "学号不存在: " + weeklyReport.getStudentId());
        }

        boolean exists = lambdaQuery()
                .eq(WeeklyReport::getStudentId, weeklyReport.getStudentId())
                .eq(WeeklyReport::getYear, weeklyReport.getYear())
                .eq(WeeklyReport::getWeekNumber, weeklyReport.getWeekNumber())
                .exists();
        if (exists) {
            throw new BusinessException(400, "该学生本周的周报记录已存在");
        }

        weeklyReport.setStudentName(student.getStudentName());
        if (file != null && !file.isEmpty()) {
            String subDir = "weekly-reports/" + weeklyReport.getYear() + "/" + weeklyReport.getStudentId();
            String filePath = FileUtils.validateAndSaveFile(file, uploadBasePath, subDir);
            weeklyReport.setWordFilePath(filePath);
            weeklyReport.setOriginalFileName(file.getOriginalFilename());
        }
        calculateWeightedScore(weeklyReport);
        save(weeklyReport);
        return weeklyReport;
    }

    @Override
    public WeeklyReport uploadFile(Long id, MultipartFile file) {
        WeeklyReport report = getById(id);
        if (report == null) {
            throw new BusinessException(404, "周报记录不存在");
        }
        if (report.getWordFilePath() != null) {
            FileUtils.deleteFile(report.getWordFilePath());
        }
        String subDir = "weekly-reports/" + report.getYear() + "/" + report.getStudentId();
        String filePath = FileUtils.validateAndSaveFile(file, uploadBasePath, subDir);
        report.setWordFilePath(filePath);
        report.setOriginalFileName(file.getOriginalFilename());
        updateById(report);
        return report;
    }

    @Override
    public WeeklyReport evaluate(Long id, BigDecimal rawScore, String evaluator) {
        WeeklyReport report = getById(id);
        if (report == null) {
            throw new BusinessException(404, "周报记录不存在");
        }
        report.setRawScore(rawScore);
        report.setEvaluator(evaluator);
        report.setEvaluationTime(LocalDateTime.now());
        calculateWeightedScore(report);
        updateById(report);
        return report;
    }

    @Override
    public Resource downloadFile(Long id) {
        WeeklyReport report = getById(id);
        if (report == null || report.getWordFilePath() == null) {
            throw new BusinessException(404, "文件不存在");
        }
        java.io.File file = new java.io.File(report.getWordFilePath());
        if (!file.exists()) {
            throw new BusinessException(404, "文件已被删除");
        }
        return new FileSystemResource(file);
    }

    @Override
    public void deleteWithFile(Long id) {
        WeeklyReport report = getById(id);
        if (report != null) {
            if (report.getWordFilePath() != null) {
                FileUtils.deleteFile(report.getWordFilePath());
            }
            removeById(id);
        }
    }

    @Override
    public PageVO<WeeklyReport> pageQuery(int page, int size, String studentId, Integer year, Integer weekNumber,
                                           String evaluator, Double scoreRangeMin, Double scoreRangeMax) {
        IPage<WeeklyReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<WeeklyReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentId)) wrapper.eq(WeeklyReport::getStudentId, studentId);
        if (year != null) wrapper.eq(WeeklyReport::getYear, year);
        if (weekNumber != null) wrapper.eq(WeeklyReport::getWeekNumber, weekNumber);
        if (StringUtils.isNotBlank(evaluator)) wrapper.eq(WeeklyReport::getEvaluator, evaluator);
        if (scoreRangeMin != null) wrapper.ge(WeeklyReport::getRawScore, scoreRangeMin);
        if (scoreRangeMax != null) wrapper.le(WeeklyReport::getRawScore, scoreRangeMax);
        wrapper.orderByDesc(WeeklyReport::getYear, WeeklyReport::getWeekNumber);
        return PageVO.of(page(pageParam, wrapper), w -> w);
    }

    private void calculateWeightedScore(WeeklyReport report) {
        if (report.getRawScore() != null) {
            report.setWeightedScore(report.getRawScore().multiply(WeightConstants.WEEKLY_REPORT_WEIGHT));
        }
    }
}
