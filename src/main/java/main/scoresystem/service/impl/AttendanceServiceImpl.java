package main.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import main.scoresystem.common.constants.WeightConstants;
import main.scoresystem.common.exception.BusinessException;
import main.scoresystem.dto.BatchImportResultDTO;
import main.scoresystem.entity.Attendance;
import main.scoresystem.entity.Student;
import main.scoresystem.mapper.AttendanceMapper;
import main.scoresystem.service.AttendanceService;
import main.scoresystem.service.StudentService;
import main.scoresystem.utils.ExcelUtils;
import main.scoresystem.vo.PageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    private final StudentService studentService;

    public AttendanceServiceImpl(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public Attendance createAttendance(Attendance attendance) {
        Student student = studentService.getByStudentId(attendance.getStudentId());
        if (student == null) {
            throw new BusinessException(400, "学号不存在: " + attendance.getStudentId());
        }

        Attendance existing = lambdaQuery()
                .eq(Attendance::getStudentId, attendance.getStudentId())
                .eq(Attendance::getYear, attendance.getYear())
                .eq(Attendance::getMonth, attendance.getMonth())
                .one();
        if (existing != null) {
            throw new BusinessException(400, "该学生本月的考勤记录已存在");
        }

        attendance.setStudentName(student.getStudentName());
        calculateWeightedScore(attendance);
        save(attendance);
        return attendance;
    }

    @Override
    public Attendance updateAttendance(Long id, Attendance attendance) {
        Attendance existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "考勤记录不存在");
        }
        if (attendance.getAttendanceDays() != null) existing.setAttendanceDays(attendance.getAttendanceDays());
        if (attendance.getAbsenceDays() != null) existing.setAbsenceDays(attendance.getAbsenceDays());
        if (attendance.getRawScore() != null) existing.setRawScore(attendance.getRawScore());
        if (attendance.getRemarks() != null) existing.setRemarks(attendance.getRemarks());
        calculateWeightedScore(existing);
        updateById(existing);
        return existing;
    }

    @Override
    public PageVO<Attendance> pageQuery(int page, int size, String studentId, Integer year, Integer month,
                                         Double scoreRangeMin, Double scoreRangeMax) {
        IPage<Attendance> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentId)) {
            wrapper.eq(Attendance::getStudentId, studentId);
        }
        if (year != null) wrapper.eq(Attendance::getYear, year);
        if (month != null) wrapper.eq(Attendance::getMonth, month);
        if (scoreRangeMin != null) wrapper.ge(Attendance::getRawScore, scoreRangeMin);
        if (scoreRangeMax != null) wrapper.le(Attendance::getRawScore, scoreRangeMax);
        wrapper.orderByDesc(Attendance::getYear, Attendance::getMonth);
        return PageVO.of(page(pageParam, wrapper), a -> a);
    }

    @Override
    public BatchImportResultDTO batchImport(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        try {
            List<List<Object>> rows = ExcelUtils.readExcel(file.getInputStream());
            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                try {
                    if (row.isEmpty() || row.get(0) == null) continue;
                    Attendance attendance = new Attendance();
                    attendance.setStudentId(row.get(0).toString());
                    attendance.setYear(getIntValue(row.get(1)));
                    attendance.setMonth(getIntValue(row.get(2)));
                    attendance.setAttendanceDays(getIntValue(row.get(3)));
                    attendance.setAbsenceDays(getIntValue(row.get(4)));
                    attendance.setRawScore(row.get(5) != null ? BigDecimal.valueOf(getDoubleValue(row.get(5))) : null);
                    if (row.size() > 6) attendance.setRemarks(row.get(6) != null ? row.get(6).toString() : null);
                    createAttendance(attendance);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BusinessException(500, "Excel文件解析失败");
        }
        return new BatchImportResultDTO(successCount, errors.size(), errors);
    }

    @Override
    public void exportExcel(HttpServletResponse response, String studentId, Integer year, Integer month) {
        List<Attendance> list = lambdaQuery()
                .eq(StringUtils.isNotBlank(studentId), Attendance::getStudentId, studentId)
                .eq(year != null, Attendance::getYear, year)
                .eq(month != null, Attendance::getMonth, month)
                .orderByDesc(Attendance::getYear, Attendance::getMonth)
                .list();

        String[] headers = {"学号", "姓名", "年份", "月份", "出勤天数", "缺勤天数", "原始分数", "权重分数", "备注"};
        List<String[]> data = new ArrayList<>();
        for (Attendance a : list) {
            data.add(new String[]{
                    a.getStudentId(), a.getStudentName(),
                    String.valueOf(a.getYear()), String.valueOf(a.getMonth()),
                    String.valueOf(a.getAttendanceDays()), String.valueOf(a.getAbsenceDays()),
                    a.getRawScore() != null ? a.getRawScore().toString() : "",
                    a.getWeightedScore() != null ? a.getWeightedScore().toString() : "",
                    a.getRemarks()
            });
        }
        try {
            ExcelUtils.exportExcel(response, "考勤数据", headers, data);
        } catch (IOException e) {
            throw new BusinessException(500, "导出Excel失败");
        }
    }

    private void calculateWeightedScore(Attendance attendance) {
        if (attendance.getRawScore() != null) {
            attendance.setWeightedScore(attendance.getRawScore().multiply(WeightConstants.ATTENDANCE_WEIGHT));
        }
    }

    private int getIntValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString().trim());
    }

    private double getDoubleValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.parseDouble(obj.toString().trim());
    }
}
