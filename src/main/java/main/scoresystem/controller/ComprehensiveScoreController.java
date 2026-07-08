package main.scoresystem.controller;

import jakarta.servlet.http.HttpServletResponse;
import main.scoresystem.common.response.R;
import main.scoresystem.entity.ComprehensiveScore;
import main.scoresystem.service.ComprehensiveScoreService;
import main.scoresystem.vo.PageVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comprehensive")
public class ComprehensiveScoreController {

    private final ComprehensiveScoreService comprehensiveScoreService;

    public ComprehensiveScoreController(ComprehensiveScoreService comprehensiveScoreService) {
        this.comprehensiveScoreService = comprehensiveScoreService;
    }

    @PostMapping("/calculate")
    public R<Map<String, Object>> calculateAll() {
        int count = comprehensiveScoreService.calculateAll();
        return R.ok(Map.of("calculatedCount", count, "message", "综合评分计算完成"));
    }

    @PostMapping("/calculate/{studentId}")
    public R<ComprehensiveScore> calculateByStudent(@PathVariable String studentId) {
        return R.ok(comprehensiveScoreService.calculateByStudentId(studentId));
    }

    @GetMapping("/list")
    public R<PageVO<ComprehensiveScore>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Double scoreRangeMin,
            @RequestParam(required = false) Double scoreRangeMax) {
        return R.ok(comprehensiveScoreService.pageQuery(page, size, studentId, className, scoreRangeMin, scoreRangeMax));
    }

    @GetMapping("/{studentId}")
    public R<ComprehensiveScore> getByStudentId(@PathVariable String studentId) {
        ComprehensiveScore score = comprehensiveScoreService.lambdaQuery()
                .eq(ComprehensiveScore::getStudentId, studentId).one();
        if (score == null) {
            return R.error(404, "该学生暂无综合评分数据，请先计算");
        }
        return R.ok(score);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String className,
                       @RequestParam(required = false) Double scoreRangeMin,
                       @RequestParam(required = false) Double scoreRangeMax) {
        comprehensiveScoreService.exportExcel(response, className, scoreRangeMin, scoreRangeMax);
    }

    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics() {
        return R.ok(comprehensiveScoreService.getStatistics());
    }
}
