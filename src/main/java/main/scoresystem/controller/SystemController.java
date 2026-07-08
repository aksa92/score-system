package main.scoresystem.controller;

import main.scoresystem.common.response.R;
import main.scoresystem.config.JwtConfig;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final JwtConfig jwtConfig;

    public SystemController(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            String token = jwtConfig.generateToken(request.getUsername());
            return R.ok(Map.of("token", token, "username", request.getUsername(), "role", "admin"));
        }
        return R.error(401, "用户名或密码错误");
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
