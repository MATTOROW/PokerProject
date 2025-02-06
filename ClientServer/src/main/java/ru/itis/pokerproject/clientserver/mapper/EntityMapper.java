package ru.itis.pokerproject.clientserver.mapper;

public interface EntityMapper<E, RQ, RS> {
    E toEntity(RQ request);

    RS toResponse(E entity);
}
