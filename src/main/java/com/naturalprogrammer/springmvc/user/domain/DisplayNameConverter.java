package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DisplayNameConverter implements AttributeConverter<DisplayName, String> {

    @Override
    public String convertToDatabaseColumn(DisplayName object) {
        return object == null ? null : object.value();
    }

    @Override
    public DisplayName convertToEntityAttribute(String rawValue) {
        return rawValue == null ? null : new DisplayName(rawValue);
    }
}
