package com.example.gateway.filter;

import com.example.gateway.exception.IpBlockedException;
import com.example.gateway.exception.RateLimitExceededException;
import com.example.gateway.service.IpBlockingService;
import com.example.gateway.service.IpRateLimitService;
import com.example.gateway.util.IpExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final IpBlockingService ipBlockingService;
    private final IpRateLimitService ipRateLimitService;

    private static final String LOGIN_PATH = "/auth/login";
    private static final String REGISTER_PATH = "/auth/register";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Only process POST requests to login and register endpoints
        if (!"POST".equals(method) || (!requestPath.equals(LOGIN_PATH) && !requestPath.equals(REGISTER_PATH))) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = IpExtractor.extractIpAddress(request);

        try {
            if (ipBlockingService.isIpBlocked(ipAddress)) {
                log.warn("Request from blocked IP: {} to {}", ipAddress, requestPath);
                throw new IpBlockedException("IP temporarily blocked. Try again later.");
            }

            if (requestPath.equals(LOGIN_PATH)) {
                handleLoginRequest(ipAddress, request, response, filterChain);
                return;
            }
            else if (requestPath.equals(REGISTER_PATH)) {
                handleRegisterRequest(ipAddress);
            }

            filterChain.doFilter(request, response);

        } catch (IpBlockedException | RateLimitExceededException e) {
            throw e;  // These exceptions will be handled by GlobalExceptionHandler
        }
    }


    private void handleLoginRequest(String ipAddress, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (ipRateLimitService.isIpRateLimited(ipAddress)) {
            long attempts = ipRateLimitService.recordFailedLoginByIp(ipAddress);
            ipBlockingService.blockIp(ipAddress, "Login rate limit exceeded", attempts, request.getHeader("User-Agent"));
            log.warn("IP rate limit exceeded for login, blocking IP: {}", ipAddress);
            throw new RateLimitExceededException("Too many login attempts from this IP. Please try again later.");
        }

        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response) {
            @Override
            public void setStatus(int sc) {
                super.setStatus(sc);
                handleLoginResponse(ipAddress, sc, request);
            }

            @Override
            public void sendError(int sc) throws IOException {
                super.sendError(sc);
                handleLoginResponse(ipAddress, sc, request);
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                super.sendError(sc, msg);
                handleLoginResponse(ipAddress, sc, request);
            }
        };

        filterChain.doFilter(request, responseWrapper);
    }


    private void handleLoginResponse(String ipAddress, int statusCode, HttpServletRequest request) {
        if (statusCode == HttpStatus.UNAUTHORIZED.value() || statusCode == HttpStatus.FORBIDDEN.value()) {
            long attempts = ipRateLimitService.recordFailedLoginByIp(ipAddress);
            log.debug("Recorded failed login attempt for IP: {}, total attempts: {}", ipAddress, attempts);

            if (ipRateLimitService.isIpRateLimited(ipAddress)) {
                ipBlockingService.blockIp(ipAddress, "Failed login attempts exceeded", attempts, request.getHeader("User-Agent"));
                log.warn("IP blocked after recording failed login attempt: {}", ipAddress);
            }
        }
    }

    private void handleRegisterRequest(String ipAddress) {
        if (ipRateLimitService.isRegistrationRateLimited(ipAddress)) {
            log.warn("Registration rate limit exceeded for IP: {}", ipAddress);
            throw new RateLimitExceededException("Too many registration attempts from this IP. Please try again later.");
        }
        // Record registration attempt
        ipRateLimitService.recordRegistrationByIp(ipAddress);
    }
}
