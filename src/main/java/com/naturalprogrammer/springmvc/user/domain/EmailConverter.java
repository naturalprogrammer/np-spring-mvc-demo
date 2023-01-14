package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email object) {
        return object == null ? null : object.value();
    }

    @Override
    public Email convertToEntityAttribute(String rawValue) {
        return rawValue == null ? null : new Email(rawValue);
    }
}
