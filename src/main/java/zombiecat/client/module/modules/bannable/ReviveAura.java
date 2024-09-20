package zombiecat.client.module.modules.bannable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.mixins.IEntityPlayer;
import zombiecat.client.module.Module;
import zombiecat.client.utils.Utils;
import zombiecat.client.utils.WatchTimer;

public class ReviveAura extends Module {

   public static ReviveAura INSTANCE;

   public ReviveAura() {
      super("ReviveAura", ModuleCategory.bannable);
      INSTANCE = this;
   }

   private WatchTimer watchTimer = new WatchTimer();
   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (!watchTimer.passed(300)) return;
      if (Utils.Player.isPlayerInGame()) {
         for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && mc.thePlayer.getDistanceToEntity(entity) <= 3) {
               EntityPlayer player = (EntityPlayer) entity;
               if (((IEntityPlayer) player).getSleeping()) {
                  mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.INTERACT));
               }
            }
         }
      }
   }
}
