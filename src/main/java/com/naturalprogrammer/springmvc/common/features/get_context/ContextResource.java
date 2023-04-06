package com.naturalprogrammer.springmvc.common.features.get_context;

import java.util.List;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record ContextResource(List<KeyResource> keys) {

    public record KeyResource(String id, String publicKey) {

    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "context.v1+json";
}
