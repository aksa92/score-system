package main.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import main.scoresystem.entity.WeeklyReport;
import main.scoresystem.vo.PageVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface WeeklyReportService extends IService<WeeklyReport> {
    WeeklyReport createWithFile(WeeklyReport weeklyReport, MultipartFile file);
    WeeklyReport uploadFile(Long id, MultipartFile file);
    WeeklyReport evaluate(Long id, BigDecimal rawScore, String evaluator);
    Resource downloadFile(Long id);
    void deleteWithFile(Long id);
    PageVO<WeeklyReport> pageQuery(int page, int size, String studentId, Integer year, Integer weekNumber,
                                    String evaluator, Double scoreRangeMin, Double scoreRangeMax);
}
