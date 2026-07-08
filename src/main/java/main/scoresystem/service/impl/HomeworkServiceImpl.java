package main.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import main.scoresystem.common.constants.WeightConstants;
import main.scoresystem.common.exception.BusinessException;
import main.scoresystem.entity.Homework;
import main.scoresystem.entity.Student;
import main.scoresystem.mapper.HomeworkMapper;
import main.scoresystem.service.HomeworkService;
import main.scoresystem.service.StudentService;
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
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements HomeworkService {

    @Value("${app.upload.base-path:./uploads}")
    private String uploadBasePath;

    private final StudentService studentService;

    public HomeworkServiceImpl(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public Homework createHomework(Homework homework) {
        Student student = studentService.getByStudentId(homework.getStudentId());
        if (student == null) {
            throw new BusinessException(400, "学号不存在: " + homework.getStudentId());
        }

        boolean exists = lambdaQuery().eq(Homework::getHomeworkId, homework.getHomeworkId()).exists();
        if (exists) {
            throw new BusinessException(400, "作业编号已存在: " + homework.getHomeworkId());
        }

        homework.setStudentName(student.getStudentName());
        calculateWeightedScore(homework);
        save(homework);
        return homework;
    }

    @Override
    public Homework updateHomework(String homeworkId, Homework homework) {
        Homework existing = lambdaQuery().eq(Homework::getHomeworkId, homeworkId).one();
        if (existing == null) {
            throw new BusinessException(404, "作业记录不存在: " + homeworkId);
        }

        if (homework.getHomeworkName() != null) existing.setHomeworkName(homework.getHomeworkName());
        if (homework.getSubmissionContent() != null) existing.setSubmissionContent(homework.getSubmissionContent());
        if (homework.getRawScore() != null) {
            existing.setRawScore(homework.getRawScore());
            calculateWeightedScore(existing);
        }
        if (homework.getEvaluator() != null) {
            existing.setEvaluator(homework.getEvaluator());
            existing.setEvaluationTime(LocalDateTime.now());
        }
        updateById(existing);
        return existing;
    }

    @Override
    public Homework uploadFile(String homeworkId, MultipartFile file) {
        Homework homework = lambdaQuery().eq(Homework::getHomeworkId, homeworkId).one();
        if (homework == null) {
            throw new BusinessException(404, "作业记录不存在: " + homeworkId);
        }
        if (homework.getFilePath() != null) {
            FileUtils.deleteFile(homework.getFilePath());
        }
        String subDir = "homework/" + homework.getStudentId();
        String filePath = FileUtils.validateAndSaveFile(file, uploadBasePath, subDir);
        homework.setFilePath(filePath);
        homework.setOriginalFileName(file.getOriginalFilename());
        updateById(homework);
        return homework;
    }

    @Override
    public Resource downloadFile(String homeworkId) {
        Homework homework = lambdaQuery().eq(Homework::getHomeworkId, homeworkId).one();
        if (homework == null || homework.getFilePath() == null) {
            throw new BusinessException(404, "文件不存在");
        }
        java.io.File file = new java.io.File(homework.getFilePath());
        if (!file.exists()) {
            throw new BusinessException(404, "文件已被删除");
        }
        return new FileSystemResource(file);
    }

    @Override
    public boolean checkHomeworkIdExists(String homeworkId) {
        return lambdaQuery().eq(Homework::getHomeworkId, homeworkId).exists();
    }

    @Override
    public PageVO<Homework> pageQuery(int page, int size, String studentId, String homeworkId,
                                       String homeworkName, String evaluator,
                                       Double scoreRangeMin, Double scoreRangeMax) {
        IPage<Homework> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentId)) wrapper.eq(Homework::getStudentId, studentId);
        if (StringUtils.isNotBlank(homeworkId)) wrapper.eq(Homework::getHomeworkId, homeworkId);
        if (StringUtils.isNotBlank(homeworkName)) wrapper.like(Homework::getHomeworkName, homeworkName);
        if (StringUtils.isNotBlank(evaluator)) wrapper.eq(Homework::getEvaluator, evaluator);
        if (scoreRangeMin != null) wrapper.ge(Homework::getRawScore, scoreRangeMin);
        if (scoreRangeMax != null) wrapper.le(Homework::getRawScore, scoreRangeMax);
        wrapper.orderByDesc(Homework::getCreatedTime);
        return PageVO.of(page(pageParam, wrapper), h -> h);
    }

    private void calculateWeightedScore(Homework homework) {
        if (homework.getRawScore() != null) {
            homework.setWeightedScore(homework.getRawScore().multiply(WeightConstants.HOMEWORK_WEIGHT));
        }
    }
}
