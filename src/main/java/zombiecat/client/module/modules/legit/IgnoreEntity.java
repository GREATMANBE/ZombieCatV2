package zombiecat.client.module.modules.legit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.*;
import zombiecat.client.module.Module;

public class IgnoreEntity extends Module {
    public static boolean isOn = false;

    public IgnoreEntity() {
        super("IgnoreEntity", ModuleCategory.legit);
    }

    @Override
    public void onEnable() {
        isOn = true;
    }

    @Override
    public void onDisable() {
        isOn = false;
    }

    /**
     * Returns true if the given entity should be ignored (non-aggressive).
     */
    public static boolean shouldIgnore(Entity entity) {
        if (!isOn) return false;

        // Allow only aggressive mobs, ignore everything else
        return !(entity instanceof EntityMob || entity instanceof EntitySlime || entity instanceof EntityGhast || entity instanceof EntityDragon || entity instanceof EntityWither);
    }
}
