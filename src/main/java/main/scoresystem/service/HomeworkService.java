package main.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import main.scoresystem.entity.Homework;
import main.scoresystem.vo.PageVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface HomeworkService extends IService<Homework> {
    Homework createHomework(Homework homework);
    Homework updateHomework(String homeworkId, Homework homework);
    Homework uploadFile(String homeworkId, MultipartFile file);
    Resource downloadFile(String homeworkId);
    boolean checkHomeworkIdExists(String homeworkId);
    PageVO<Homework> pageQuery(int page, int size, String studentId, String homeworkId,
                                String homeworkName, String evaluator,
                                Double scoreRangeMin, Double scoreRangeMax);
}
