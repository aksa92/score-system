package main.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import main.scoresystem.common.exception.BusinessException;
import main.scoresystem.dto.BatchImportResultDTO;
import main.scoresystem.entity.Student;
import main.scoresystem.mapper.StudentMapper;
import main.scoresystem.service.StudentService;
import main.scoresystem.utils.ExcelUtils;
import main.scoresystem.vo.PageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Override
    public Student getByStudentId(String studentId) {
        return lambdaQuery().eq(Student::getStudentId, studentId).one();
    }

    @Override
    public PageVO<Student> pageQuery(int page, int size, String studentId, String studentName, String className) {
        IPage<Student> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentId)) {
            wrapper.eq(Student::getStudentId, studentId);
        }
        if (StringUtils.isNotBlank(studentName)) {
            wrapper.like(Student::getStudentName, studentName);
        }
        if (StringUtils.isNotBlank(className)) {
            wrapper.like(Student::getClassName, className);
        }
        wrapper.orderByDesc(Student::getCreatedTime);
        return PageVO.of(page(pageParam, wrapper), s -> s);
    }

    @Override
    public BatchImportResultDTO batchImport(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        try {
            List<List<Object>> rows = ExcelUtils.readExcel(file.getInputStream());
            for (int i = 1; i < rows.size(); i++) { // skip header
                List<Object> row = rows.get(i);
                try {
                    if (row.isEmpty() || row.get(0) == null) continue;
                    Student student = new Student();
                    student.setStudentId(row.get(0).toString());
                    student.setStudentName(row.get(1) != null ? row.get(1).toString() : "");
                    student.setClassName(row.get(2) != null ? row.get(2).toString() : null);
                    student.setGender(row.get(3) != null ? row.get(3).toString() : null);
                    student.setEmail(row.get(4) != null ? row.get(4).toString() : null);
                    student.setPhone(row.get(5) != null ? row.get(5).toString() : null);

                    if (getByStudentId(student.getStudentId()) != null) {
                        errors.add("第" + (i + 1) + "行: 学号 " + student.getStudentId() + " 已存在");
                        continue;
                    }
                    save(student);
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
}
