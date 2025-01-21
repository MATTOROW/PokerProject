package ru.itis.pokerproject.clientserver.repository;

import ru.itis.pokerproject.clientserver.config.Database;
import ru.itis.pokerproject.clientserver.mapper.AccountEntityMapper;
import ru.itis.pokerproject.clientserver.mapper.AccountRowMapper;
import ru.itis.pokerproject.clientserver.model.AccountEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AccountRepository {
    private final AccountRowMapper rowMapper = new AccountRowMapper();

    //language=sql
    private final String SQL_FIND_BY_USERNAME = "SELECT * FROM account WHERE username = ?";

    public Optional<AccountEntity> findByUsername(String username) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_USERNAME);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? Optional.ofNullable(rowMapper.mapRow(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            return Optional.empty();
        }
    }
}
