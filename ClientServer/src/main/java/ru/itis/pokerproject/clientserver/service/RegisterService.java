package ru.itis.pokerproject.clientserver.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.itis.pokerproject.clientserver.mapper.AccountEntityMapper;
import ru.itis.pokerproject.clientserver.model.AccountEntity;
import ru.itis.pokerproject.clientserver.repository.AccountRepository;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;

public class RegisterService {
    private static final AccountRepository accountRepository = new AccountRepository();
    private static final AccountEntityMapper accountEntityMapper = new AccountEntityMapper();
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private RegisterService() {}

    public static AccountResponse register(String username, String password) {
        AccountEntity account = accountRepository.create(username, encoder.encode(password)).orElse(null);
        if (account == null) {
            return null;
        }
        return accountEntityMapper.toResponse(account);
    }
}
