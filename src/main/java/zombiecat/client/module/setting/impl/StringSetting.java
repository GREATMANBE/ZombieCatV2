package zombiecat.client.module.setting.impl;

import com.google.gson.JsonObject;
import zombiecat.client.module.setting.Setting;

public class StringSetting extends Setting {
    private String currentValue;
    private String[] options;

    public StringSetting(String name, String defaultValue, String... options) {
        super(name);
        this.currentValue = defaultValue;
        this.options = options;
    }

    public String getValue() {
        return currentValue;
    }

    public void setValue(String value) {
        for (String option : options) {
            if (option.equalsIgnoreCase(value)) {
                this.currentValue = value;
                break;
            }
        }
    }

    public String[] getOptions() {
        return options;
    }

    @Override
    public JsonObject getConfigAsJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", currentValue);
        return obj;
    }

    @Override
    public void applyConfigFromJson(JsonObject json) {
        if (json.has("value")) {
            setValue(json.get("value").getAsString());
        }
    }

    @Override
    public void resetToDefaults() {
        this.currentValue = options.length > 0 ? options[0] : "";
    }
}
