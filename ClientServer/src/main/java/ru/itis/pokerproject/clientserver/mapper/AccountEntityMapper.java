package ru.itis.pokerproject.clientserver.mapper;

import ru.itis.pokerproject.clientserver.model.AccountEntity;
import ru.itis.pokerproject.shared.dto.request.AccountRequest;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;


public class AccountEntityMapper implements EntityMapper<AccountEntity, AccountRequest, AccountResponse> {
    @Override
    public AccountEntity toEntity(AccountRequest request) {
        if (request == null) {
            return null;
        }

        return new AccountEntity(request.username(), request.password(), -1);
    }

    @Override
    public AccountResponse toResponse(AccountEntity entity) {
        return new AccountResponse(entity.getUsername(), entity.getMoney(), null);
    }
}
