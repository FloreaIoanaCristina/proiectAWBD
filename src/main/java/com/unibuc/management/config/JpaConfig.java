package com.unibuc.management.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class JpaConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void registerListeners() {
        try {
            SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
            if (sessionFactory != null) {
                EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                        .getService(EventListenerRegistry.class);
            }
        } catch (Exception e) {
            System.err.println("[JPA Config] Error: " + e.getMessage());
        }
    }
}