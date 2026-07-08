package main.scoresystem.utils;

import lombok.extern.slf4j.Slf4j;
import main.scoresystem.common.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
public class FileUtils {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".doc", ".docx", ".pdf", ".txt", ".zip", ".rar", ".jpg", ".png");

    public static String validateAndSaveFile(MultipartFile file, String basePath, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new BusinessException(400, "文件名不能为空");
        }

        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalName.substring(dotIndex).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(400, "不支持的文件类型: " + ext + "，允许类型: " + ALLOWED_EXTENSIONS);
        }

        String uniqueName = UUID.randomUUID().toString().replace("-", "") + "_" + originalName;
        Path targetDir = Paths.get(basePath, subDir);
        Path targetPath = targetDir.resolve(uniqueName);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetPath.toFile());
            log.info("File saved: {}", targetPath);
            return targetPath.toString().replace("\\", "/");
        } catch (IOException e) {
            log.error("Failed to save file", e);
            throw new BusinessException(500, "文件保存失败: " + e.getMessage());
        }
    }

    public static void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) return;
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
        }
    }
}
