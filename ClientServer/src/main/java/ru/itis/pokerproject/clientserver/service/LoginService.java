package ru.itis.pokerproject.clientserver.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.itis.pokerproject.clientserver.model.AccountEntity;
import ru.itis.pokerproject.clientserver.repository.AccountRepository;

import java.util.Date;

public class LoginService {
    private static final String SECRET_KEY = "EgorPomidorVishelIzZAGORPOSADILKARtoshkuVishelNaLukoshkoLALALALALALAL";
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final AccountRepository accountRepository = new AccountRepository();

    private LoginService() {
    }

    public static byte[] login(String username, String password) {
        AccountEntity account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) {
            return new byte[0];
        }
        if (encoder.matches(password, account.getPassword())) {
            return "%s;%s;%s".formatted(account.getUsername(), account.getMoney(), generateToken(account)).getBytes();
        }
        return new byte[0];
    }

    private static String generateToken(AccountEntity account) {
        return Jwts.builder()
                .setSubject(account.getUsername())
                .claim("money", account.getMoney())  // Добавляем информацию о балансе
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))  // Токен будет действовать 30 минут
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}
