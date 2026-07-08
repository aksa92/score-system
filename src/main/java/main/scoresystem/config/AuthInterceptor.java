package main.scoresystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.scoresystem.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/api/system/login";
    private static final String OPTIONS = "OPTIONS";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (OPTIONS.equalsIgnoreCase(request.getMethod()) || LOGIN_PATH.equals(request.getRequestURI())) {
            return true;
        }
        return true;
    }
}
