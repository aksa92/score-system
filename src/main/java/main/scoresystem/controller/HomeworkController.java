package main.scoresystem.controller;

import main.scoresystem.common.response.R;
import main.scoresystem.entity.Homework;
import main.scoresystem.service.HomeworkService;
import main.scoresystem.vo.PageVO;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;

    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @PostMapping
    public R<Homework> create(@RequestBody Homework homework) {
        return R.ok(homeworkService.createHomework(homework));
    }

    @DeleteMapping("/{homeworkId}")
    public R<Void> delete(@PathVariable String homeworkId) {
        Homework homework = homeworkService.lambdaQuery().eq(Homework::getHomeworkId, homeworkId).one();
        if (homework != null) {
            homeworkService.removeById(homework.getId());
        }
        return R.ok();
    }

    @PutMapping("/{homeworkId}")
    public R<Homework> update(@PathVariable String homeworkId, @RequestBody Homework homework) {
        return R.ok(homeworkService.updateHomework(homeworkId, homework));
    }

    @GetMapping("/{homeworkId}")
    public R<Homework> getByHomeworkId(@PathVariable String homeworkId) {
        Homework homework = homeworkService.lambdaQuery().eq(Homework::getHomeworkId, homeworkId).one();
        if (homework == null) {
            return R.error(404, "作业记录不存在");
        }
        return R.ok(homework);
    }

    @GetMapping("/list")
    public R<PageVO<Homework>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String homeworkId,
            @RequestParam(required = false) String homeworkName,
            @RequestParam(required = false) String evaluator,
            @RequestParam(required = false) Double scoreRangeMin,
            @RequestParam(required = false) Double scoreRangeMax) {
        return R.ok(homeworkService.pageQuery(page, size, studentId, homeworkId, homeworkName, evaluator, scoreRangeMin, scoreRangeMax));
    }

    @PostMapping("/{homeworkId}/upload")
    public R<Homework> uploadFile(@PathVariable String homeworkId, @RequestParam("file") MultipartFile file) {
        return R.ok(homeworkService.uploadFile(homeworkId, file));
    }

    @GetMapping("/{homeworkId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String homeworkId) {
        Homework homework = homeworkService.lambdaQuery().eq(Homework::getHomeworkId, homeworkId).one();
        if (homework == null || homework.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = homeworkService.downloadFile(homeworkId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + homework.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/check-id")
    public R<Map<String, Boolean>> checkHomeworkId(@RequestParam String homeworkId) {
        boolean exists = homeworkService.checkHomeworkIdExists(homeworkId);
        return R.ok(Map.of("available", !exists));
    }
}
