package zombiecat.client.module.modules.legit;

import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;

public class IgnoreEntity extends Module {
   public static boolean isOn = false;
   public BooleanSetting ignoreMobs;

   public IgnoreEntity() {
      super("IgnoreEntity", Module.ModuleCategory.legit);
      this.registerSetting(ignoreMobs = new BooleanSetting("Ignore Mobs", false)); // default: ignore mobs = false (so mobs NOT ignored)
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
    * Call this method to check if an entity should be ignored by the module.
    * 
    * @param entity The entity to check.
    * @return true if entity should be ignored, false if it should NOT be ignored.
    */
   public boolean shouldIgnore(Entity entity) {
      if (!isOn) return false; // module off, don't ignore anything

      if (ignoreMobs.getValue()) {
         // Ignore mobs and all other entities
         return true;
      } else {
         // Ignore everything except mobs (entities implementing IMob)
         if (entity instanceof IMob) {
            return false; // don't ignore mobs
         }
         return true; // ignore everything else
      }
   }
}
