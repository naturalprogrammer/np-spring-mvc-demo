package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter
public class UserIdConverter implements AttributeConverter<UserId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(UserId object) {
        return object == null ? null : object.value();
    }

    @Override
    public UserId convertToEntityAttribute(UUID rawValue) {
        return rawValue == null ? null : new UserId(rawValue);
    }
}
