package com.desapp.football_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@ToString
public class EndpointLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String requestPath;
    private String httpMethod;
    private Integer statusCode;
    private Long responseContentLength;
    private Long responseTime;
    private String requestIp;
    private LocalDate timestamp;

    public EndpointLog(HttpServletRequestWrapper requestWrapper, Long responseTime, Integer statusCode, Authentication auth) {
        this.requestPath = requestWrapper.getRequestURI();
        this.httpMethod = requestWrapper.getMethod();
        this.responseContentLength = requestWrapper.getContentLengthLong();
        this.requestIp = requestWrapper.getRemoteAddr();
        this.timestamp = LocalDate.now();
        this.responseTime = responseTime;
        this.statusCode = statusCode;
        setUserIdByAuth(auth);
    }

    private void setUserIdByAuth(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            User user = (User) auth.getPrincipal();
            this.userId = user.getId();
        }
    }
}
