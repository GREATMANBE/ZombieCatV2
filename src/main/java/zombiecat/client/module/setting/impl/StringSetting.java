package zombiecat.client.module.setting.impl;

import com.google.gson.JsonObject;
import zombiecat.client.clickgui.Component;
import zombiecat.client.clickgui.components.ModuleComponent;
import zombiecat.client.module.setting.Setting;

public class StringSetting extends Setting {
    private String value;
    private final String defaultValue;
    private final String[] options;

    public StringSetting(String name, String defaultValue, String... options) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.options = options;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        // Optionally, validate value is in options array
        this.value = value;
    }

    @Override
    public void resetToDefaults() {
        this.value = defaultValue;
    }

    @Override
    public JsonObject getConfigAsJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        return obj;
    }

    @Override
    public String getSettingType() {
        return "String";
    }

    @Override
    public void applyConfigFromJson(JsonObject json) {
        if (json.has("value")) {
            setValue(json.get("value").getAsString());
        }
    }

    @Override
    public Component createComponent(ModuleComponent parent) {
        // You’ll want to implement your GUI component creation here
        // Return a new Component that allows selecting or typing a string value
        return null; // Placeholder: implement your own GUI component
    }
}
