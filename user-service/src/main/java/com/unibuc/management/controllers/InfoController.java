package com.unibuc.management.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates centralized config + dynamic refresh: {@code app.message} is
 * served by the Config Server and can be reloaded at runtime via
 * {@code POST /actuator/refresh} without restarting the service.
 */
@RestController
@RequestMapping("/auth/info")
@RefreshScope
public class InfoController {

    @Value("${app.message:default-local-message}")
    private String message;

    @GetMapping
    public Map<String, String> info() {
        return Map.of("message", message);
    }
}
