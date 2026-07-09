package com.unibuc.management.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/auth/internal", fallback = UserClientFallback.class)
public interface UserClient {

    @DeleteMapping("/users/{userId}")
    void deleteUser(@PathVariable("userId") Long userId);
}
