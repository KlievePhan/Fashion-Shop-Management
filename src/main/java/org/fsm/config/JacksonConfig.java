package org.fsm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    /**
     * Configure ObjectMapper để format JSON consistent
     * - Không indent (compact format)
     * - Sorted keys
     * - No spaces after colons
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Disable pretty printing to ensure compact format
        mapper.disable(SerializationFeature.INDENT_OUTPUT);

        // Sort keys alphabetically
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        return mapper;
    }
}