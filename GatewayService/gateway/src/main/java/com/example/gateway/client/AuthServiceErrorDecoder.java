package com.example.gateway.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthServiceErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultErrorDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("AuthService returned error status: {} for method: {}", response.status(), methodKey);
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
