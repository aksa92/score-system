package main.scoresystem.controller;

import main.scoresystem.common.response.R;
import main.scoresystem.entity.WeeklyReport;
import main.scoresystem.service.WeeklyReportService;
import main.scoresystem.vo.PageVO;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/weekly-report")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    public WeeklyReportController(WeeklyReportService weeklyReportService) {
        this.weeklyReportService = weeklyReportService;
    }

    @PostMapping
    public R<WeeklyReport> create(@RequestPart("data") WeeklyReport weeklyReport,
                                   @RequestPart(value = "file", required = false) MultipartFile file) {
        return R.ok(weeklyReportService.createWithFile(weeklyReport, file));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        weeklyReportService.deleteWithFile(id);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<WeeklyReport> update(@PathVariable Long id, @RequestBody WeeklyReport weeklyReport) {
        WeeklyReport existing = weeklyReportService.getById(id);
        if (existing == null) {
            return R.error(404, "周报记录不存在");
        }
        if (weeklyReport.getRemarks() != null) existing.setRemarks(weeklyReport.getRemarks());
        weeklyReportService.updateById(existing);
        return R.ok(existing);
    }

    @PutMapping("/{id}/score")
    public R<WeeklyReport> evaluate(@PathVariable Long id, @RequestBody EvaluateRequest request) {
        return R.ok(weeklyReportService.evaluate(id, request.getRawScore(), request.getEvaluator()));
    }

    @GetMapping("/{id}")
    public R<WeeklyReport> getById(@PathVariable Long id) {
        WeeklyReport report = weeklyReportService.getById(id);
        if (report == null) {
            return R.error(404, "周报记录不存在");
        }
        return R.ok(report);
    }

    @GetMapping("/list")
    public R<PageVO<WeeklyReport>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) String evaluator,
            @RequestParam(required = false) Double scoreRangeMin,
            @RequestParam(required = false) Double scoreRangeMax) {
        return R.ok(weeklyReportService.pageQuery(page, size, studentId, year, weekNumber, evaluator, scoreRangeMin, scoreRangeMax));
    }

    @PostMapping("/{id}/upload")
    public R<WeeklyReport> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return R.ok(weeklyReportService.uploadFile(id, file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        WeeklyReport report = weeklyReportService.getById(id);
        if (report == null || report.getWordFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = weeklyReportService.downloadFile(id);
        String contentType = report.getOriginalFileName() != null && report.getOriginalFileName().endsWith(".docx")
                ? "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                : "application/msword";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getOriginalFileName() + "\"")
                .body(resource);
    }

    public static class EvaluateRequest {
        private BigDecimal rawScore;
        private String evaluator;
        public BigDecimal getRawScore() { return rawScore; }
        public void setRawScore(BigDecimal rawScore) { this.rawScore = rawScore; }
        public String getEvaluator() { return evaluator; }
        public void setEvaluator(String evaluator) { this.evaluator = evaluator; }
    }
}
