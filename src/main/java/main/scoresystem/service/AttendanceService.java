package main.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import main.scoresystem.dto.BatchImportResultDTO;
import main.scoresystem.entity.Attendance;
import main.scoresystem.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

public interface AttendanceService extends IService<Attendance> {
    Attendance createAttendance(Attendance attendance);
    Attendance updateAttendance(Long id, Attendance attendance);
    PageVO<Attendance> pageQuery(int page, int size, String studentId, Integer year, Integer month,
                                  Double scoreRangeMin, Double scoreRangeMax);
    BatchImportResultDTO batchImport(MultipartFile file);
    void exportExcel(HttpServletResponse response, String studentId, Integer year, Integer month);
}
