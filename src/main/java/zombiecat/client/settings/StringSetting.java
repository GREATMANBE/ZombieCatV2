package zombiecat.client.settings;

import java.util.Arrays;
import java.util.List;

public class StringSetting {
    private final String name;
    private String value;
    private final List<String> options;

    public StringSetting(String name, String defaultValue, String... options) {
        this.name = name;
        this.value = defaultValue;
        this.options = Arrays.asList(options);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (options.contains(value)) {
            this.value = value;
        }
    }

    public List<String> getOptions() {
        return options;
    }
}
