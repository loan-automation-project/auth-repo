package com.demo.authentication_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    @PostMapping("/api/customers/create")
    void createCustomerWithUserId(@RequestParam("userId") Long userId);
    
    @GetMapping("/api/customers/exists/{userId}")
    boolean checkCustomerExists(@PathVariable("userId") Long userId);
} 