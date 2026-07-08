package main.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import main.scoresystem.dto.BatchImportResultDTO;
import main.scoresystem.entity.Student;
import main.scoresystem.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService extends IService<Student> {
    Student getByStudentId(String studentId);
    PageVO<Student> pageQuery(int page, int size, String studentId, String studentName, String className);
    BatchImportResultDTO batchImport(MultipartFile file);
}
