package ru.itis.pokerproject.clientserver.mapper;

import ru.itis.pokerproject.clientserver.model.AccountEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountRowMapper implements RowMapper<AccountEntity> {
    @Override
    public AccountEntity mapRow(ResultSet resultSet) {
        try {
            String username = resultSet.getString("username");
            String password = resultSet.getString("password");
            long money = resultSet.getLong("money");
            return new AccountEntity(username, password, money);
        } catch (SQLException e) {
            return null;
        }
    }
}
