package com.example.gateway.filter;

import com.example.gateway.constants.ErrorMsg;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Component
@Slf4j
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final IpBlockingService ipBlockingService;
    private final IpRateLimitService ipRateLimitService;
    private final HandlerExceptionResolver resolver;

    public IpRateLimitFilter(IpBlockingService ipBlockingService, 
                             IpRateLimitService ipRateLimitService,
                             @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.ipBlockingService = ipBlockingService;
        this.ipRateLimitService = ipRateLimitService;
        this.resolver = resolver;
    }
    private static final String CART_PATH_PREFIX = "/cart";
    private static final String LOGIN_PATH = "/auth/login";
    private static final String REGISTER_PATH = "/auth/register";
    private static final String ANON_CART_GENERATE_PATH = "/auth/anonymous-cart/generate";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        if (!"POST".equals(method) || (!requestPath.equals(LOGIN_PATH) && !requestPath.equals(REGISTER_PATH)
                && !requestPath.equals(CART_PATH_PREFIX) && !requestPath.equals(ANON_CART_GENERATE_PATH))) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = IpExtractor.extractIpAddress(request);

        try {
            if (ipBlockingService.isIpBlocked(ipAddress)) {
                log.warn("Request from blocked IP: {} to {}", ipAddress, requestPath);
                throw new IpBlockedException(ErrorMsg.IP_TEMPORARILY_BLOCKED);
            }

            if (requestPath.equals(LOGIN_PATH)) {
                handleLoginRequest(ipAddress, request);
            }
            else {
                handleRegisterRequest(ipAddress);
            }

            filterChain.doFilter(request, response);

        } catch (IpBlockedException | RateLimitExceededException e) {
            resolver.resolveException(request, response, null, e);
        }
    }


    private void handleLoginRequest(String ipAddress, HttpServletRequest request) {
        if (ipRateLimitService.isIpRateLimited(ipAddress)) {
            long attempts = ipRateLimitService.recordFailedLoginByIp(ipAddress);
            ipBlockingService.blockIp(ipAddress, "Login rate limit exceeded", attempts, request.getHeader("User-Agent"));
            log.warn("IP rate limit exceeded for login, blocking IP: {}", ipAddress);
            throw new RateLimitExceededException(ErrorMsg.TOO_MANY_LOGIN_ATTEMPTS_FROM_IP);
        }

        ipRateLimitService.recordFailedLoginByIp(ipAddress);
    }

    private void handleRegisterRequest(String ipAddress) {
        if (ipRateLimitService.isRegistrationRateLimited(ipAddress)) {
            log.warn("Registration rate limit exceeded for IP: {}", ipAddress);
            throw new RateLimitExceededException(ErrorMsg.TOO_MANY_REGISTRATION_ATTEMPTS);
        }
        ipRateLimitService.recordRegistrationByIp(ipAddress);
    }
}
