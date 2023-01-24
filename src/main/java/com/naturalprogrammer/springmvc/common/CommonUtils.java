package com.naturalprogrammer.springmvc.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtils {

    public static final String UNKNOWN = "UNKNOWN";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String CONTENT_TYPE_PREFIX = "application/vnd.com.naturalprogrammer.";
}
