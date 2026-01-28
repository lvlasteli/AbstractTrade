package com.example.gateway.service;

import com.example.gateway.client.AuthServiceClient;
import com.example.gateway.config.CookieConfig;
import com.example.gateway.model.CartIdentity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartIdentityService {

    public static final String ATTR_CART_IDENTITY = "gateway.cart.identity";
    public static final String ATTR_USER_ID = "gateway.cart.userId";

    private static final String AUTH_SESSION_CURRENT_PATH = "/auth/session/current";
    private static final String ANON_CART_GENERATE_PATH = "/auth/anon-cart/generate";

    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;
    private final CookieConfig cookieConfig;

    public CartIdentity resolveIdentity(HttpServletRequest request) {
        CartIdentity cached = getCached();
        if (cached != null) {
            return cached;
        }

        boolean hasAuthSession = hasCookie(request, cookieConfig.getAuthSession().getName());
        boolean hasAnonCart = hasCookie(request, cookieConfig.getAnonCart().getName());

        CartIdentity identity;
        if (hasAuthSession) {
            identity = resolveAuthenticatedIdentity();
        } else if (!hasAnonCart) {
            identity = resolveOrCreateAnonymousIdentity();
        } else {
            identity = CartIdentity.builder().build();
        }

        cache(identity);
        if (identity.getUserId() != null) {
            RequestContextHolder.currentRequestAttributes()
                    .setAttribute(ATTR_USER_ID, identity.getUserId(), RequestAttributes.SCOPE_REQUEST);
        }
        return identity;
    }

    private CartIdentity resolveAuthenticatedIdentity() {
        try {
            ResponseEntity<Object> authResponse = authServiceClient.forwardGetRequest(AUTH_SESSION_CURRENT_PATH);
            String userId = extractNestedString(authResponse.getBody(), "data", "userId");
            return CartIdentity.builder().userId(userId).build();
        } catch (FeignException e) {
            log.debug("Failed to resolve authenticated user via {}: status={} msg={}",
                    AUTH_SESSION_CURRENT_PATH, e.status(), e.getMessage());
            return CartIdentity.builder().build();
        } catch (Exception e) {
            log.warn("Unexpected error resolving authenticated cart identity: {}", e.getMessage());
            return CartIdentity.builder().build();
        }
    }

    private CartIdentity resolveOrCreateAnonymousIdentity() {
        try {
            ResponseEntity<Object> authResponse = authServiceClient.forwardGetRequest(ANON_CART_GENERATE_PATH);

            String cartId = extractNestedString(authResponse.getBody(), "data", "cartId");
            String setCookie = authResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

            return CartIdentity.builder()
                    .cartId(cartId)
                    .setCookie(setCookie)
                    .build();
        } catch (FeignException e) {
            log.debug("Failed to generate anon cart via {}: status={} msg={}",
                    ANON_CART_GENERATE_PATH, e.status(), e.getMessage());
            return CartIdentity.builder().build();
        } catch (Exception e) {
            log.warn("Unexpected error generating anon cart identity: {}", e.getMessage());
            return CartIdentity.builder().build();
        }
    }

    private boolean hasCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || StringUtils.isBlank(cookieName)) {
            return false;
        }
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private String extractNestedString(Object body, String firstKey, String secondKey) {
        if (body == null) return null;
        Map<String, Object> root = objectMapper.convertValue(body, new TypeReference<>() {});
        Object first = root.get(firstKey);
        if (!(first instanceof Map<?, ?> firstMap)) return null;
        Object value = firstMap.get(secondKey);
        return value != null ? String.valueOf(value) : null;
    }

    private CartIdentity getCached() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        Object value = attrs.getAttribute(ATTR_CART_IDENTITY, RequestAttributes.SCOPE_REQUEST);
        return (value instanceof CartIdentity ci) ? ci : null;
    }

    private void cache(CartIdentity identity) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        attrs.setAttribute(ATTR_CART_IDENTITY, identity, RequestAttributes.SCOPE_REQUEST);
    }
}

