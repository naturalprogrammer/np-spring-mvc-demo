package com.naturalprogrammer.springmvc.common.features.get_context;

import com.naturalprogrammer.springmvc.config.MyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextGetter {

    private final MyProperties properties;

    public ContextResource get() {
        var context = new ContextResource(List.of(new ContextResource.KeyResource(
                properties.jws().id(),
                properties.jws().publicKey()
        )));
        log.info("Got {}", context);
        return context;
    }
}
