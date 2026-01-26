package com.example.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "auth-service",
    url = "${auth.service.url}",
    configuration = com.example.gateway.config.AuthServiceFeignConfig.class
)
public interface AuthServiceClient {
    
    @GetMapping(value = "{path}")
    ResponseEntity<Object> forwardGetRequest(@PathVariable("path") String path);
    
    @PostMapping(value = "{path}")
    ResponseEntity<Object> forwardPostRequest(@PathVariable("path") String path, @RequestBody(required = false) Object body);
    
    @PutMapping(value = "{path}")
    ResponseEntity<Object> forwardPutRequest(@PathVariable("path") String path, @RequestBody(required = false) Object body);
    
    @DeleteMapping(value = "{path}")
    ResponseEntity<Object> forwardDeleteRequest(@PathVariable("path") String path);
}
