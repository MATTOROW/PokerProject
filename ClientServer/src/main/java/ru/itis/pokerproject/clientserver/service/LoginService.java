package ru.itis.pokerproject.clientserver.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.itis.pokerproject.clientserver.mapper.AccountEntityMapper;
import ru.itis.pokerproject.clientserver.mapper.AccountRowMapper;
import ru.itis.pokerproject.clientserver.model.AccountEntity;
import ru.itis.pokerproject.clientserver.repository.AccountRepository;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;

public class LoginService {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final AccountRepository repository = new AccountRepository();
    private static final AccountEntityMapper entityMapper = new AccountEntityMapper();

    private LoginService() {}

    public static AccountResponse login(String username, String password) {
        AccountEntity account = repository.findByUsername(username).orElseThrow();

        if (encoder.matches(password, account.getPassword())) {
            return entityMapper.toResponse(account);
        }

        return null;
    }
}
