package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PasswordConverter implements AttributeConverter<Password, String> {

    @Override
    public String convertToDatabaseColumn(Password object) {
        return object == null ? null : object.value();
    }

    @Override
    public Password convertToEntityAttribute(String rawValue) {
        return rawValue == null ? null : new Password(rawValue);
    }
}
