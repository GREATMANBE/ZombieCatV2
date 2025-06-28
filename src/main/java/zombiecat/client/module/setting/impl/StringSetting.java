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
        return new Component(parent, this) {
            private int currentIndex = getIndex();
    
            private int getIndex() {
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equalsIgnoreCase(value)) return i;
                }
                return 0;
            }
    
            @Override
            public void drawComponent(int mouseX, int mouseY) {
                // Basic drawing: label and current value
                mc.fontRendererObj.drawStringWithShadow(
                    getName() + ": " + value, 
                    x + 2, y + 2, 
                    0xFFFFFF
                );
            }
    
            @Override
            public void mouseClicked(int mouseX, int mouseY, int button) {
                if (isHovered(mouseX, mouseY) && button == 0) {
                    currentIndex = (currentIndex + 1) % options.length;
                    setValue(options[currentIndex]);
                }
            }
    
            private boolean isHovered(int mouseX, int mouseY) {
                return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
            }
        };
    }
}
