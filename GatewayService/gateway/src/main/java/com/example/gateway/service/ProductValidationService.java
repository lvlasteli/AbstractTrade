package com.example.gateway.service;

import com.example.gateway.client.ProductServiceClient;
import com.example.gateway.exception.ProductNotAvailableException;
import com.example.gateway.exception.ProductNotFoundException;
import com.example.gateway.model.dto.response.ProductValidationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductValidationService {
    
    private final ProductServiceClient productServiceClient;
    private final ObjectMapper objectMapper;

    public void fetchProductBySku(String sku, Integer requestedQuantity) {
        log.debug("Validating product via Product Service: sku={}", sku);
        
        try {
            Map<String, String> queryParams = new HashMap<>();
            if (requestedQuantity != null) {
                queryParams.put("quantity", String.valueOf(requestedQuantity));
            }

            String PRODUCT_SERVICE_PATH = "/products/sku/{sku}";
            ResponseEntity<Object> response = productServiceClient.forwardGetRequest(
                PRODUCT_SERVICE_PATH.replace("{sku}", sku), 
                queryParams
            );
            
            ProductValidationResponse productResponse = objectMapper.convertValue(
                response.getBody(), 
                ProductValidationResponse.class
            );
            
            if (productResponse == null) {
                throw new RuntimeException("Failed to parse product response");
            }
            
            log.info("Product validation successful: sku={}", sku);

        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(extractErrorMessage(e, "Product not found with SKU: " + sku));
            
        } catch (FeignException.BadRequest e) {
            String errorMessage = extractErrorMessage(e, e.getMessage());
            throw new ProductNotAvailableException(errorMessage);
            
        } catch (FeignException e) {
            throw new RuntimeException("Failed to validate product: " + extractErrorMessage(e, e.getMessage()));
        }
    }


    private String extractErrorMessage(FeignException e, String defaultMessage) {
        try {
            String content = e.contentUTF8();
            if (content != null && !content.isBlank()) {
                Map<String, Object> errorBody = objectMapper.readValue(content, Map.class);
                Object message = errorBody.get("message");
                if (message != null) {
                    return message.toString();
                }
            }
        } catch (Exception ex) {
            log.debug("Failed to parse error response from Product Service", ex);
        }
        return defaultMessage;
    }
}
