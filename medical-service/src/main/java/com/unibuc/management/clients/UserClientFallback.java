package com.unibuc.management.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public void deleteUser(Long userId) {
        log.warn("user-service indisponibil. Contul de utilizator {} nu a putut fi șters acum.", userId);
    }
}
