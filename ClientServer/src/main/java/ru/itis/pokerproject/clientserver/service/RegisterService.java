package ru.itis.pokerproject.clientserver.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.itis.pokerproject.clientserver.model.AccountEntity;
import ru.itis.pokerproject.clientserver.repository.AccountRepository;

public class RegisterService {
    private static final AccountRepository accountRepository = new AccountRepository();
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private RegisterService() {
    }

    public static byte[] register(String username, String password) {
        AccountEntity account = accountRepository.create(username, encoder.encode(password)).orElse(null);
        if (account == null) {
            return null;
        }
        return new byte[0];
    }
}
