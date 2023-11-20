package com.andersenlab.hotel.reader;

import lombok.Data;
import lombok.SneakyThrows;

import java.util.Properties;

@Data
public class PropertyReaderFromFile {

    private final String propertyFile;

    @SneakyThrows
    public String readProperty(String property) {
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFile));

        return properties.getProperty(property);
    }
}
