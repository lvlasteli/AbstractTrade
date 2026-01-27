package com.example.gateway.client;

import com.example.gateway.config.AuthServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "auth-service",
    url = "${auth.service.url}",
    configuration = AuthServiceFeignConfig.class
)
public interface AuthServiceClient {
    
    @GetMapping(value = "{path}")
    ResponseEntity<Object> forwardGetRequest(@PathVariable String path);
    
    @PostMapping(value = "{path}")
    ResponseEntity<Object> forwardPostRequest(@PathVariable String path, @RequestBody(required = false) Object body);
    
    @PutMapping(value = "{path}")
    ResponseEntity<Object> forwardPutRequest(@PathVariable String path, @RequestBody(required = false) Object body);
    
    @DeleteMapping(value = "{path}")
    ResponseEntity<Object> forwardDeleteRequest(@PathVariable String path);
}
