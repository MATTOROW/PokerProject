package ru.itis.pokerproject.clientserver.repository;

import ru.itis.pokerproject.clientserver.config.Database;
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
    //language=sql
    private final String SQL_CREATE = "INSERT INTO account (username, password) VALUES (?, ?) RETURNING *";
    //language=sql
    private final String SQL_UPDATE_MONEY_BY_USERNAME = "UPDATE account SET money = ? WHERE username = ? RETURNING money";

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

    public Optional<AccountEntity> create(String username, String hashedPassword) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_CREATE);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? Optional.ofNullable(rowMapper.mapRow(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public long updateMoney(String username, long money) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_MONEY_BY_USERNAME);
            statement.setLong(1, money);
            statement.setString(2, username);
            return statement.executeQuery().getLong(1);
        } catch (SQLException e) {
            return -404;
        }
    }
}
