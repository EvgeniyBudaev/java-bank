package com.accountsservice.service.mapper;

import com.accountsservice.controller.dto.response.ResponseAccountDto;
import com.accountsservice.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    ResponseAccountDto toResponseAccountDto(AccountEntity accountEntity);

    @Mapping(target = "balance", constant = "0")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccountEntity toCreateAccountEntity(UUID userId);
}
