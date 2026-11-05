package com.veth007.hotelmanagement.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Lets every request accept either a plain numeric id or a hashid
 * (the encoded ids returned in responses), without changing any DTO:
 *
 * - the Converter covers form / query / path binding
 *   (e.g. @ModelAttribute RoomRequest.floorId in multipart requests)
 * - the Jackson module covers JSON bodies
 *   (e.g. {"customerId": "aB3xK9pQ"} binds to a Long field)
 */
@Configuration
public class HashIdWebConfig {

    @Bean
    public Converter<String, Long> hashIdStringToLongConverter() {
        // anonymous class (not a lambda) so Spring can read the generic types
        return new Converter<String, Long>() {
            @Override
            public Long convert(String source) {
                if (source.isBlank()) {
                    return null;
                }
                return HashIdUtil.resolveId(source);
            }
        };
    }

    @Bean
    public Module hashIdJacksonModule() {
        SimpleModule module = new SimpleModule("hashIdModule");
        module.addDeserializer(Long.class, new JsonDeserializer<Long>() {
            @Override
            public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                    return p.getLongValue();
                }
                String text = p.getValueAsString();
                if (text == null || text.isBlank()) {
                    return null;
                }
                return HashIdUtil.resolveId(text);
            }
        });
        return module;
    }
}
