package main.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import main.scoresystem.entity.ComprehensiveScore;
import main.scoresystem.vo.PageVO;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface ComprehensiveScoreService extends IService<ComprehensiveScore> {
    int calculateAll();
    ComprehensiveScore calculateByStudentId(String studentId);
    void recalculateRankings();
    PageVO<ComprehensiveScore> pageQuery(int page, int size, String studentId, String className,
                                          Double scoreRangeMin, Double scoreRangeMax);
    void exportExcel(HttpServletResponse response, String className, Double scoreRangeMin, Double scoreRangeMax);
    Map<String, Object> getStatistics();
}
