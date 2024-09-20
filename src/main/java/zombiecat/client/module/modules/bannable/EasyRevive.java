package zombiecat.client.module.modules.bannable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.mixins.IEntityPlayer;
import zombiecat.client.module.Module;
import zombiecat.client.utils.Utils;

public class EasyRevive extends Module {
   /*   public static BooleanSetting onlyClick;*/
   public static EasyRevive INSTANCE;

   public EasyRevive() {
      super("EasyRevive", ModuleCategory.bannable);
      /*      this.registerSetting(onlyClick = new BooleanSetting("OnlyClick", true));*/
      INSTANCE = this;
   }

   /*   public void onPacket(Packet<?> packet) {
         if (Utils.Player.isPlayerInGame()) {
            if (packet instanceof C03PacketPlayer) {
               C03PacketPlayer packetPlayer = ((C03PacketPlayer) packet);
               if (packetPlayer.getRotating()) {
                  if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                          && mc.objectMouseOver.entityHit instanceof EntityPlayer
                          && ((IEntityPlayer) mc.objectMouseOver.entityHit).getSleeping()) {
                     *//*               if (!onlyClick.getValue() || mc.gameSettings.keyBindUseItem.isKeyDown()) {*//*
                  float[] angle = Aimbot.calculateYawPitch(mc.thePlayer.getPositionEyes(1), mc.objectMouseOver.entityHit.getPositionVector());
                  ((IC03PacketPlayer) packet).setYaw(angle[0]);
                  ((IC03PacketPlayer) packet).setPitch(angle[1]);
                  *//*               }*//*
               }
            }
         }
      }
   }*/
   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (Utils.Player.isPlayerInGame()) {
         for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer) entity;
               if (((IEntityPlayer) player).getSleeping()) {
                  player.setEntityBoundingBox(new AxisAlignedBB(
                          player.posX - 0.3,
                          player.posY,
                          player.posZ - 0.3,
                          player.posX + 0.3,
                          player.posY + 1.8,
                          player.posZ + 0.3
                  ));
               }
            }
         }
      }
   }
}
