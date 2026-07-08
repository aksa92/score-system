package main.scoresystem.controller;

import jakarta.validation.Valid;
import main.scoresystem.common.response.R;
import main.scoresystem.dto.BatchImportResultDTO;
import main.scoresystem.entity.Student;
import main.scoresystem.service.StudentService;
import main.scoresystem.vo.PageVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public R<Student> create(@Valid @RequestBody Student student) {
        Student existing = studentService.getByStudentId(student.getStudentId());
        if (existing != null) {
            return R.error(400, "学号已存在: " + student.getStudentId());
        }
        studentService.save(student);
        return R.ok(student);
    }

    @DeleteMapping("/{studentId}")
    public R<Void> delete(@PathVariable String studentId) {
        Student student = studentService.getByStudentId(studentId);
        if (student != null) {
            studentService.removeById(student.getId());
        }
        return R.ok();
    }

    @PutMapping("/{studentId}")
    public R<Student> update(@PathVariable String studentId, @RequestBody Student student) {
        Student existing = studentService.getByStudentId(studentId);
        if (existing == null) {
            return R.error(404, "学生不存在");
        }
        if (student.getStudentName() != null) existing.setStudentName(student.getStudentName());
        if (student.getClassName() != null) existing.setClassName(student.getClassName());
        if (student.getGender() != null) existing.setGender(student.getGender());
        if (student.getEmail() != null) existing.setEmail(student.getEmail());
        if (student.getPhone() != null) existing.setPhone(student.getPhone());
        if (student.getEnrolmentYear() != null) existing.setEnrolmentYear(student.getEnrolmentYear());
        studentService.updateById(existing);
        return R.ok(existing);
    }

    @GetMapping("/{studentId}")
    public R<Student> getByStudentId(@PathVariable String studentId) {
        Student student = studentService.getByStudentId(studentId);
        if (student == null) {
            return R.error(404, "学生不存在");
        }
        return R.ok(student);
    }

    @GetMapping("/list")
    public R<PageVO<Student>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String className) {
        return R.ok(studentService.pageQuery(page, size, studentId, studentName, className));
    }

    @PostMapping("/import")
    public R<BatchImportResultDTO> batchImport(@RequestParam("file") MultipartFile file) {
        return R.ok(studentService.batchImport(file));
    }
}
