package main.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import main.scoresystem.common.constants.WeightConstants;
import main.scoresystem.common.exception.BusinessException;
import main.scoresystem.entity.*;
import main.scoresystem.mapper.ComprehensiveScoreMapper;
import main.scoresystem.service.*;
import main.scoresystem.utils.ExcelUtils;
import main.scoresystem.vo.PageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ComprehensiveScoreServiceImpl extends ServiceImpl<ComprehensiveScoreMapper, ComprehensiveScore>
        implements ComprehensiveScoreService {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final WeeklyReportService weeklyReportService;
    private final HomeworkService homeworkService;

    public ComprehensiveScoreServiceImpl(StudentService studentService,
                                          AttendanceService attendanceService,
                                          WeeklyReportService weeklyReportService,
                                          HomeworkService homeworkService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.weeklyReportService = weeklyReportService;
        this.homeworkService = homeworkService;
    }

    @Override
    @Transactional
    public int calculateAll() {
        List<Student> students = studentService.lambdaQuery().eq(Student::getIsActive, 1).list();
        for (Student student : students) {
            calculateSingleStudent(student);
        }
        recalculateRankings();
        return students.size();
    }

    @Override
    @Transactional
    public ComprehensiveScore calculateByStudentId(String studentId) {
        Student student = studentService.getByStudentId(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在: " + studentId);
        }
        ComprehensiveScore score = calculateSingleStudent(student);
        recalculateRankings();
        return score;
    }

    @Override
    @Transactional
    public void recalculateRankings() {
        List<ComprehensiveScore> list = lambdaQuery()
                .orderByDesc(ComprehensiveScore::getTotalWeightedScore)
                .orderByAsc(ComprehensiveScore::getStudentId)
                .list();

        int rank = 1;
        for (ComprehensiveScore cs : list) {
            cs.setRanking(rank++);
            updateById(cs);
        }
    }

    @Override
    public PageVO<ComprehensiveScore> pageQuery(int page, int size, String studentId, String className,
                                                 Double scoreRangeMin, Double scoreRangeMax) {
        IPage<ComprehensiveScore> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ComprehensiveScore> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentId)) {
            wrapper.eq(ComprehensiveScore::getStudentId, studentId);
        }
        if (StringUtils.isNotBlank(className)) {
            wrapper.like(ComprehensiveScore::getStudentName, className);
        }
        if (scoreRangeMin != null) wrapper.ge(ComprehensiveScore::getTotalWeightedScore, scoreRangeMin);
        if (scoreRangeMax != null) wrapper.le(ComprehensiveScore::getTotalWeightedScore, scoreRangeMax);
        wrapper.orderByAsc(ComprehensiveScore::getRanking);
        return PageVO.of(page(pageParam, wrapper), c -> c);
    }

    @Override
    public void exportExcel(HttpServletResponse response, String className, Double scoreRangeMin, Double scoreRangeMax) {
        List<ComprehensiveScore> list = lambdaQuery()
                .orderByAsc(ComprehensiveScore::getRanking)
                .list();

        String[] headers = {"排名", "学号", "姓名", "考勤原始分", "考勤权重分(20%)",
                "周报原始分", "周报权重分(30%)", "作业原始分", "作业权重分(50%)", "综合总分"};
        List<String[]> data = new ArrayList<>();
        for (ComprehensiveScore cs : list) {
            data.add(new String[]{
                    String.valueOf(cs.getRanking()),
                    cs.getStudentId(), cs.getStudentName(),
                    formatScore(cs.getAttendanceRawScore()), formatScore(cs.getAttendanceWeightedScore()),
                    formatScore(cs.getWeeklyReportRawScore()), formatScore(cs.getWeeklyReportWeightedScore()),
                    formatScore(cs.getHomeworkRawScore()), formatScore(cs.getHomeworkWeightedScore()),
                    formatScore(cs.getTotalWeightedScore())
            });
        }
        try {
            ExcelUtils.exportExcel(response, "综合评分排名", headers, data);
        } catch (IOException e) {
            throw new BusinessException(500, "导出Excel失败");
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        List<ComprehensiveScore> list = list();
        Map<String, Object> stats = new LinkedHashMap<>();
        if (list.isEmpty()) {
            stats.put("totalStudents", 0);
            return stats;
        }

        DoubleSummaryStatistics summary = list.stream()
                .filter(c -> c.getTotalWeightedScore() != null)
                .mapToDouble(c -> c.getTotalWeightedScore().doubleValue())
                .summaryStatistics();

        stats.put("totalStudents", (int) summary.getCount());
        stats.put("averageScore", BigDecimal.valueOf(summary.getAverage()).setScale(2, RoundingMode.HALF_UP));
        stats.put("maxScore", BigDecimal.valueOf(summary.getMax()).setScale(2, RoundingMode.HALF_UP));
        stats.put("minScore", BigDecimal.valueOf(summary.getMin()).setScale(2, RoundingMode.HALF_UP));

        long[] buckets = new long[5];
        for (ComprehensiveScore cs : list) {
            double score = cs.getTotalWeightedScore() != null ? cs.getTotalWeightedScore().doubleValue() : 0;
            if (score >= 90) buckets[0]++;
            else if (score >= 80) buckets[1]++;
            else if (score >= 70) buckets[2]++;
            else if (score >= 60) buckets[3]++;
            else buckets[4]++;
        }
        stats.put("score90to100", buckets[0]);
        stats.put("score80to89", buckets[1]);
        stats.put("score70to79", buckets[2]);
        stats.put("score60to69", buckets[3]);
        stats.put("scoreBelow60", buckets[4]);

        return stats;
    }

    private ComprehensiveScore calculateSingleStudent(Student student) {
        BigDecimal attendanceAvg = getAttendanceAvg(student.getStudentId());
        BigDecimal weeklyAvg = getWeeklyReportAvg(student.getStudentId());
        BigDecimal homeworkAvg = getHomeworkAvg(student.getStudentId());

        BigDecimal attendanceWeighted = attendanceAvg.multiply(WeightConstants.ATTENDANCE_WEIGHT);
        BigDecimal weeklyWeighted = weeklyAvg.multiply(WeightConstants.WEEKLY_REPORT_WEIGHT);
        BigDecimal homeworkWeighted = homeworkAvg.multiply(WeightConstants.HOMEWORK_WEIGHT);
        BigDecimal total = attendanceWeighted.add(weeklyWeighted).add(homeworkWeighted);

        ComprehensiveScore score = lambdaQuery()
                .eq(ComprehensiveScore::getStudentId, student.getStudentId())
                .one();
        if (score == null) {
            score = new ComprehensiveScore();
            score.setStudentId(student.getStudentId());
        }

        score.setStudentName(student.getStudentName());
        score.setAttendanceRawScore(attendanceAvg);
        score.setAttendanceWeightedScore(attendanceWeighted);
        score.setWeeklyReportRawScore(weeklyAvg);
        score.setWeeklyReportWeightedScore(weeklyWeighted);
        score.setHomeworkRawScore(homeworkAvg);
        score.setHomeworkWeightedScore(homeworkWeighted);
        score.setTotalWeightedScore(total);
        score.setCalculationTime(LocalDateTime.now());

        saveOrUpdate(score);
        return score;
    }

    private BigDecimal getAttendanceAvg(String studentId) {
        List<Attendance> list = attendanceService.lambdaQuery()
                .eq(Attendance::getStudentId, studentId).list();
        if (list.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (Attendance a : list) {
            if (a.getRawScore() != null) { sum = sum.add(a.getRawScore()); count++; }
        }
        return count == 0 ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getWeeklyReportAvg(String studentId) {
        List<WeeklyReport> list = weeklyReportService.lambdaQuery()
                .eq(WeeklyReport::getStudentId, studentId).list();
        if (list.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (WeeklyReport w : list) {
            if (w.getRawScore() != null) { sum = sum.add(w.getRawScore()); count++; }
        }
        return count == 0 ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getHomeworkAvg(String studentId) {
        List<Homework> list = homeworkService.lambdaQuery()
                .eq(Homework::getStudentId, studentId).list();
        if (list.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (Homework h : list) {
            if (h.getRawScore() != null) { sum = sum.add(h.getRawScore()); count++; }
        }
        return count == 0 ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private String formatScore(BigDecimal score) {
        return score != null ? score.setScale(2, RoundingMode.HALF_UP).toString() : "";
    }
}
