package com.edusmart.manager.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({
        com.edusmart.manager.security.JwtProperties.class,
        com.edusmart.manager.security.CorsProperties.class
})
public class SecurityConfig {
    private final CorsProperties corsProperties;

    public SecurityConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                // 预检请求必须先放行，否则浏览器会在正式请求前被安全链拦截。
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/switch-role").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/admin/users/page").access(expr("hasRole('ADMIN') and hasAnyAuthority('user:view', 'user:manage', 'user:authorization:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/users/*").access(expr("hasRole('ADMIN') and hasAnyAuthority('user:view', 'user:manage', 'user:authorization:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/users/*/roles").access(expr("hasRole('ADMIN') and hasAuthority('user:authorization:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/users").access(expr("hasRole('ADMIN') and hasAnyAuthority('user:manage', 'user:authorization:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/users/*").access(expr("hasRole('ADMIN') and hasAuthority('user:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/admin/users/*").access(expr("hasRole('ADMIN') and hasAuthority('user:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/roles/page").access(expr("hasRole('ADMIN') and hasAnyAuthority('user:role:manage', 'user:authorization:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/roles/*").access(expr("hasRole('ADMIN') and hasAnyAuthority('user:role:manage', 'user:authorization:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/roles").access(expr("hasRole('ADMIN') and hasAuthority('user:role:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/roles/*").access(expr("hasRole('ADMIN') and hasAuthority('user:role:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/admin/roles/*").access(expr("hasRole('ADMIN') and hasAuthority('user:role:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/roles/*/permissions").access(expr("hasRole('ADMIN') and hasAuthority('user:role:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/courses/page").access(expr("hasRole('ADMIN') and hasAnyAuthority('course:view', 'course:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/courses/*").access(expr("hasRole('ADMIN') and hasAnyAuthority('course:view', 'course:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/courses").access(expr("hasRole('ADMIN') and hasAuthority('course:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/courses/*").access(expr("hasRole('ADMIN') and hasAuthority('course:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/admin/courses/*").access(expr("hasRole('ADMIN') and hasAuthority('course:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/classes/page").access(expr("hasRole('ADMIN') and hasAnyAuthority('class:view', 'class:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/classes/*").access(expr("hasRole('ADMIN') and hasAnyAuthority('class:view', 'class:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/classes").access(expr("hasRole('ADMIN') and hasAuthority('class:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/classes/*").access(expr("hasRole('ADMIN') and hasAuthority('class:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/admin/classes/*").access(expr("hasRole('ADMIN') and hasAuthority('class:manage')"))
                .requestMatchers("/api/admin/resources/**").access(expr("hasRole('ADMIN') and hasAuthority('resource:view')"))
                .requestMatchers("/api/admin/assignments/**").access(expr("hasRole('ADMIN') and hasAuthority('assignment:manage')"))
                .requestMatchers("/api/admin/scores/**").access(expr("hasRole('ADMIN') and hasAuthority('score:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/attendance-exceptions/page").access(expr("hasRole('ADMIN') and hasAnyAuthority('attendance:abnormal:view', 'attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/attendance-exceptions/*").access(expr("hasRole('ADMIN') and hasAnyAuthority('attendance:abnormal:view', 'attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/attendance-exceptions").access(expr("hasRole('ADMIN') and hasAuthority('attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/attendance-exceptions/*").access(expr("hasRole('ADMIN') and hasAuthority('attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/admin/attendance-exceptions/*").access(expr("hasRole('ADMIN') and hasAuthority('attendance:abnormal:manage')"))
                .requestMatchers("/api/admin/attendance/**").access(expr("hasRole('ADMIN') and hasAuthority('attendance:view')"))
                .requestMatchers("/api/admin/orders/**").access(expr("hasRole('ADMIN') and hasAuthority('finance:order:manage')"))
                .requestMatchers("/api/admin/bills/**").access(expr("hasRole('ADMIN') and hasAuthority('finance:bill:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/admin/logs/page").access(expr("hasRole('ADMIN') and hasAnyAuthority('setting:log:view', 'setting:log:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/logs/archive-strategy").access(expr("hasRole('ADMIN') and hasAnyAuthority('setting:log:view', 'setting:log:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/admin/logs/archive-strategy").access(expr("hasRole('ADMIN') and hasAuthority('setting:log:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/admin/logs/export").access(expr("hasRole('ADMIN') and hasAnyAuthority('setting:log:view', 'setting:log:manage')"))
                .requestMatchers("/api/admin/display/**").access(expr("hasRole('ADMIN') and hasAuthority('setting:display:manage')"))
                .requestMatchers("/api/admin/settings/**").access(expr("hasRole('ADMIN') and hasAuthority('setting:view')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/classes/page").access(expr("hasRole('TEACHER') and hasAnyAuthority('teacher:class:view', 'teacher:class:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/teacher/classes/*").access(expr("hasRole('TEACHER') and hasAnyAuthority('teacher:class:view', 'teacher:class:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/classes").access(expr("hasRole('TEACHER') and hasAuthority('teacher:class:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/teacher/classes/*").access(expr("hasRole('TEACHER') and hasAuthority('teacher:class:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/teacher/classes/*").access(expr("hasRole('TEACHER') and hasAuthority('teacher:class:manage')"))
                .requestMatchers("/api/teacher/assignments/**").access(expr("hasRole('TEACHER') and hasAuthority('assignment:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/resources/page").access(expr("hasRole('TEACHER') and hasAnyAuthority('resource:view', 'resource:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/teacher/resources/*").access(expr("hasRole('TEACHER') and hasAnyAuthority('resource:view', 'resource:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/resources").access(expr("hasRole('TEACHER') and hasAuthority('resource:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/teacher/resources/*").access(expr("hasRole('TEACHER') and hasAuthority('resource:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/teacher/resources/*").access(expr("hasRole('TEACHER') and hasAuthority('resource:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/attendance-exceptions/page").access(expr("hasRole('TEACHER') and hasAnyAuthority('attendance:abnormal:view', 'attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.GET, "/api/teacher/attendance-exceptions/*").access(expr("hasRole('TEACHER') and hasAnyAuthority('attendance:abnormal:view', 'attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/attendance-exceptions").access(expr("hasRole('TEACHER') and hasAuthority('attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/teacher/attendance-exceptions/*").access(expr("hasRole('TEACHER') and hasAuthority('attendance:abnormal:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/teacher/attendance-exceptions/*").access(expr("hasRole('TEACHER') and hasAuthority('attendance:abnormal:manage')"))
                .requestMatchers("/api/teacher/attendance/**").access(expr("hasRole('TEACHER') and hasAuthority('teacher:attendance:batch:manage')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/scores/page").access(expr("hasRole('TEACHER') and hasAnyAuthority('score:manage', 'teacher:grade:analysis:view')"))
                .requestMatchers(HttpMethod.GET, "/api/teacher/scores/analysis/*").access(expr("hasRole('TEACHER') and hasAnyAuthority('score:manage', 'teacher:grade:analysis:view')"))
                .requestMatchers(HttpMethod.GET, "/api/teacher/scores/*").access(expr("hasRole('TEACHER') and hasAnyAuthority('score:manage', 'teacher:grade:analysis:view')"))
                .requestMatchers(HttpMethod.POST, "/api/teacher/scores").access(expr("hasRole('TEACHER') and hasAuthority('score:manage')"))
                .requestMatchers(HttpMethod.PUT, "/api/teacher/scores/*").access(expr("hasRole('TEACHER') and hasAuthority('score:manage')"))
                .requestMatchers(HttpMethod.DELETE, "/api/teacher/scores/*").access(expr("hasRole('TEACHER') and hasAuthority('score:manage')"))
                .requestMatchers("/api/teacher/profile/**").access(expr("hasRole('TEACHER') and hasAuthority('teacher:profile:password:update')"))
                .requestMatchers("/api/student/courses/**").access(expr("hasRole('STUDENT') and hasAuthority('student:class:view')"))
                .requestMatchers("/api/student/assignments/**").access(expr("hasRole('STUDENT') and hasAuthority('student:assignment:submission:view')"))
                .requestMatchers("/api/student/resources/**").access(expr("hasRole('STUDENT') and hasAuthority('student:resource:view')"))
                .requestMatchers("/api/student/attendance/**").access(expr("hasRole('STUDENT') and hasAuthority('student:attendance:abnormal:view')"))
                .requestMatchers(HttpMethod.POST, "/api/student/scores/page").access(expr("hasRole('STUDENT') and hasAnyAuthority('student:exam:score:view', 'student:grade:analysis:view')"))
                .requestMatchers(HttpMethod.GET, "/api/student/scores/*").access(expr("hasRole('STUDENT') and hasAnyAuthority('student:exam:score:view', 'student:grade:analysis:view')"))
                .requestMatchers("/api/student/profile/**").access(expr("hasRole('STUDENT') and hasAuthority('student:profile:password:update')"))
                .requestMatchers("/api/admin/**").access(expr("hasRole('ADMIN')"))
                .requestMatchers("/api/teacher/**").access(expr("hasRole('TEACHER')"))
                .requestMatchers("/api/student/**").access(expr("hasRole('STUDENT')"))
                .anyRequest().authenticated()
        );

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> writeError(response, 40101, "未登录或登录已过期", HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) -> writeError(response, 40301, "无权限访问", HttpServletResponse.SC_FORBIDDEN))
        );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(
                emptyToDefault(
                        corsProperties.getAllowedOrigins(),
                        List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:5175")
                )
        );
        config.setAllowedMethods(emptyToDefault(corsProperties.getAllowedMethods(), List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
        config.setAllowedHeaders(emptyToDefault(corsProperties.getAllowedHeaders(), List.of("*")));
        config.setExposedHeaders(corsProperties.getExposedHeaders());
        config.setAllowCredentials(corsProperties.isAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> emptyToDefault(List<String> source, List<String> defaultValue) {
        return source == null || source.isEmpty() ? defaultValue : source;
    }

    private WebExpressionAuthorizationManager expr(String expression) {
        return new WebExpressionAuthorizationManager(expression);
    }

    private void writeError(HttpServletResponse response, int code, String message, int httpStatus) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        var body = new LinkedHashMap<String, Object>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}

