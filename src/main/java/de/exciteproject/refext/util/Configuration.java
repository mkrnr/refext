package de.exciteproject.refext.util;

/**
 * Container class for a configuration consisting of a name value pair.
 */
public class Configuration {
    private String name;
    private String value;

    public Configuration(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

}
