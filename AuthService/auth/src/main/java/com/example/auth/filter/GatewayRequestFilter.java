package com.example.auth.filter;

import com.example.auth.exception.GatewayAccessDeniedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GatewayRequestFilter extends OncePerRequestFilter {

    @Value("${auth.security.gateway.header-name:X-Gateway-Request}")
    private String headerName;

    @Value("${auth.security.gateway.enabled:true}")
    private Boolean fetatureEnabled;

    @Value("${auth.security.gateway.header-value:}")
    private String expectedHeaderValue;

    @Value("${auth.security.gateway.allowed-ips:gateway-service,172.18.0.0/16}")
    private String allowedIpsConfig;

    private List<String> allowedIps;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if(!fetatureEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerValue = request.getHeader(headerName);
        if (!StringUtils.hasText(headerValue) || !headerValue.equals(expectedHeaderValue)) {
            String clientIp = extractClientIp(request);
            log.warn("Access denied - Missing or invalid header. IP: {}, Path: {}", 
                    clientIp, requestPath);
            throw new GatewayAccessDeniedException("Missing or invalid gateway header");
        }

        String clientIp = extractClientIp(request);
        if (!isIpAllowed(clientIp)) {
            log.warn("Access denied - IP not in whitelist. IP: {}, Path: {}", clientIp, requestPath);
            throw new GatewayAccessDeniedException("IP address not allowed");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isIpAllowed(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return false;
        }

        if (allowedIps == null) {
            allowedIps = Arrays.stream(allowedIpsConfig.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }

        for (String allowedIp : allowedIps) {
            if (allowedIp.equals(clientIp)) {
                return true;
            }


            try {
                InetAddress[] addresses = InetAddress.getAllByName(allowedIp);
                for (InetAddress address : addresses) {
                    if (address.getHostAddress().equals(clientIp)) {
                        return true;
                    }
                }
            } catch (UnknownHostException e) {
                log.debug("Could not resolve hostname: {}", e.getMessage());
            }
            try {
                
                if (isIpInCidr(clientIp, allowedIp)) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("Could not resolve ip address in cidr: {}", e.getMessage());
            }
        }

        return false;
    }

    public static boolean isIpInCidr(String ip, String cidr) {
        try {
            SubnetUtils utils = new SubnetUtils(cidr);
            utils.setInclusiveHostCount(true);
            return utils.getInfo().isInRange(ip);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
