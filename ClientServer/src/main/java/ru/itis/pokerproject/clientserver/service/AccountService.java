package ru.itis.pokerproject.clientserver.service;

import ru.itis.pokerproject.clientserver.mapper.AccountEntityMapper;
import ru.itis.pokerproject.clientserver.repository.AccountRepository;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;

public class AccountService {
    private final AccountRepository repository = new AccountRepository();
    private final AccountEntityMapper entityMapper = new AccountEntityMapper();


    public AccountResponse findByUsername(String username) {
        return entityMapper.toResponse(repository.findByUsername(username).orElseThrow());
    }
}
