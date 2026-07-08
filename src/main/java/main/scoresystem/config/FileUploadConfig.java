package main.scoresystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadConfig {

    private String basePath = "./uploads";
    private List<String> allowedExtensions;
    private long maxFileSize = 10485760;
}
