package zombiecat.client.module.modules.legit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
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
            // Ignore all entities
            return true;
        }

        // List of allowed hostile mobs
        return !(entity instanceof EntityZombie ||
                 entity instanceof EntitySkeleton ||
                 entity instanceof EntitySpider ||
                 entity instanceof EntityCaveSpider ||
                 entity instanceof EntityCreeper ||
                 entity instanceof EntityEnderman ||
                 entity instanceof EntityEndermite ||
                 entity instanceof EntitySilverfish ||
                 entity instanceof EntityWitch ||
                 entity instanceof EntitySlime ||
                 entity instanceof EntityMagmaCube ||
                 entity instanceof EntityBlaze ||
                 entity instanceof EntityGhast ||
                 entity instanceof EntityGuardian);
    }
}
