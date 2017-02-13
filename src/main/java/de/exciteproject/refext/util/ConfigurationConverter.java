package de.exciteproject.refext.util;

import com.beust.jcommander.IStringConverter;

/**
 * Class for extending JCommander to handle configuration key=value pairs
 */
public class ConfigurationConverter implements IStringConverter<Configuration> {
    @Override
    public Configuration convert(String value) {
        String[] valueSplit = value.split("=");
        if (valueSplit.length == 2) {
            Configuration configuration = new Configuration(valueSplit[0], valueSplit[1]);
            return configuration;
        } else {
            throw new IllegalArgumentException("parameter should contain an equal sign: " + value);
        }
    }
}
