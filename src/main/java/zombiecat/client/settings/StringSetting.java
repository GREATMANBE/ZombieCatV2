package zombiecat.client.module.setting.impl;

import com.google.gson.JsonObject;
import zombiecat.client.clickgui.Component;
import zombiecat.client.clickgui.components.ModuleComponent;
import zombiecat.client.module.setting.Setting;

public class StringSetting extends Setting {
    private String value;
    private final String defaultValue;
    private final String[] allowedValues;

    public StringSetting(String name, String defaultValue, String... allowedValues) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.allowedValues = allowedValues;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String newValue) {
        for (String allowed : allowedValues) {
            if (allowed.equalsIgnoreCase(newValue)) {
                this.value = newValue;
                return;
            }
        }
        // If newValue not in allowedValues, ignore or throw error as you want
    }

    @Override
    public void resetToDefaults() {
        this.value = this.defaultValue;
    }

    @Override
    public JsonObject getConfigAsJson() {
        JsonObject json = new JsonObject();
        json.addProperty("value", this.value);
        return json;
    }

    @Override
    public String getSettingType() {
        return "String";
    }

    @Override
    public void applyConfigFromJson(JsonObject json) {
        if (json.has("value")) {
            this.setValue(json.get("value").getAsString());
        }
    }

    @Override
    public Component createComponent(ModuleComponent parent) {
        // TODO: Implement GUI component creation here
        // For now return null or some dummy component if you don't have GUI code yet
        return null;
    }
}
