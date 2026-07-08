package main.scoresystem.controller;

import jakarta.servlet.http.HttpServletResponse;
import main.scoresystem.common.response.R;
import main.scoresystem.dto.BatchImportResultDTO;
import main.scoresystem.entity.Attendance;
import main.scoresystem.service.AttendanceService;
import main.scoresystem.vo.PageVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public R<Attendance> create(@RequestBody Attendance attendance) {
        return R.ok(attendanceService.createAttendance(attendance));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        attendanceService.removeById(id);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Attendance> update(@PathVariable Long id, @RequestBody Attendance attendance) {
        return R.ok(attendanceService.updateAttendance(id, attendance));
    }

    @GetMapping("/{id}")
    public R<Attendance> getById(@PathVariable Long id) {
        Attendance attendance = attendanceService.getById(id);
        if (attendance == null) {
            return R.error(404, "考勤记录不存在");
        }
        return R.ok(attendance);
    }

    @GetMapping("/list")
    public R<PageVO<Attendance>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Double scoreRangeMin,
            @RequestParam(required = false) Double scoreRangeMax) {
        return R.ok(attendanceService.pageQuery(page, size, studentId, year, month, scoreRangeMin, scoreRangeMax));
    }

    @PostMapping("/import")
    public R<BatchImportResultDTO> batchImport(@RequestParam("file") MultipartFile file) {
        return R.ok(attendanceService.batchImport(file));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String studentId,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer month) {
        attendanceService.exportExcel(response, studentId, year, month);
    }
}
