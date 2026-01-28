package com.example.gateway.client;

import com.example.gateway.config.ProductServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
    name = "product-service",
    url = "${product.service.url}",
    configuration = ProductServiceFeignConfig.class
)
public interface ProductServiceClient {
    
    @GetMapping(value = "{path}")
    ResponseEntity<Object> forwardGetRequest(@PathVariable String path, @RequestParam Map<String, String> queryParams);
    
    @PostMapping(value = "{path}")
    ResponseEntity<Object> forwardPostRequest(@PathVariable String path, @RequestParam(required = false) Map<String, String> queryParams, @RequestBody(required = false) Object body);
    
    @PutMapping(value = "{path}")
    ResponseEntity<Object> forwardPutRequest(@PathVariable String path, @RequestParam(required = false) Map<String, String> queryParams, @RequestBody(required = false) Object body);
    
    @DeleteMapping(value = "{path}")
    ResponseEntity<Object> forwardDeleteRequest(@PathVariable String path, @RequestParam(required = false) Map<String, String> queryParams);
}
