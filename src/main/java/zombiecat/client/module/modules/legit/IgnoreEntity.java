package zombiecat.client.module.modules.legit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;

public class IgnoreEntity extends Module {
    public static BooleanSetting ignoreMobs;
    public static boolean isOn = false;
    public static IgnoreEntity INSTANCE;

    public IgnoreEntity() {
        super("IgnoreEntity", ModuleCategory.legit);
        this.registerSetting(ignoreMobs = new BooleanSetting("Ignore Mobs", false));
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        isOn = true;
    }

    @Override
    public void onDisable() {
        isOn = false;
    }

    public static boolean shouldIgnore(Entity entity) {
        if (!isOn || entity == null) return false;

        if (ignoreMobs.getValue()) {
            // Ignore all entities if setting is enabled
            return true;
        } else {
            // Ignore entities that are NOT mobs (players, animals, armor stands, etc)
            return !(entity instanceof IMob);
        }
    }
}
