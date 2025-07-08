package zombiecat.client.module.modules.bannable;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;

public class GhostBlock extends Module {
   public static BooleanSetting d;

   public GhostBlock() {
      super("GhostBlock", ModuleCategory.bannable);
      this.registerSetting(d = new BooleanSetting("Hold", true));
   }

   @SubscribeEvent
   public void update(TickEvent event) {
      if (this.isOn() && d.getValue()) {
         MovingObjectPosition ray = mc.objectMouseOver;
         if (ray != null && ray.typeOfHit == MovingObjectType.BLOCK) {
            BlockPos blockpos = ray.getBlockPos();

            // Skip if block is a chest or trapped chest
            if (mc.theWorld.getBlockState(blockpos).getBlock() != Blocks.chest &&
                mc.theWorld.getBlockState(blockpos).getBlock() != Blocks.trapped_chest) {
               mc.theWorld.setBlockToAir(blockpos);
            }
         }

         if (!Keyboard.isKeyDown(this.keycode)) {
            this.disable();
         }
      }
   }

   @Override
   public void onEnable() {
      if (!d.getValue()) {
         MovingObjectPosition ray = mc.objectMouseOver;
         if (ray != null && ray.typeOfHit == MovingObjectType.BLOCK) {
            BlockPos blockpos = ray.getBlockPos();

            // Skip if block is a chest or trapped chest
            if (mc.theWorld.getBlockState(blockpos).getBlock() != Blocks.chest &&
                mc.theWorld.getBlockState(blockpos).getBlock() != Blocks.trapped_chest) {
               mc.theWorld.setBlockToAir(blockpos);
            }
         }

         this.disable();
      }
   }
}
