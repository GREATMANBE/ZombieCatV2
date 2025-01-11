package zombiecat.client.module.modules.bannable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import zombiecat.client.mixins.IC03PacketPlayer;
import zombiecat.client.module.Module;

public class Phase extends Module {
   public static boolean isOn = false;

   public Phase() {
      super("Phase", ModuleCategory.bannable);
   }

   static TickTimer tickTimer = new TickTimer();
   @Override
   public void onEnable() {
      isOn = true;
   }

   @Override
   public void onDisable() {
      isOn = false;
   }

   public static void onUpdate() {
      if (isOn) {
         boolean isInsideBlock = collideBlockIntersects(mc.thePlayer.getEntityBoundingBox());

         if (isInsideBlock) {
            mc.thePlayer.noClip = true;
            mc.thePlayer.motionY = 0.0;
            mc.thePlayer.onGround = false;
         }

         if (tickTimer.hasTimePassed(2) && mc.thePlayer.isCollidedHorizontally && (!isInsideBlock || mc.thePlayer.isSneaking())) {
            double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
            double oldX = mc.thePlayer.posX;
            double oldZ = mc.thePlayer.posZ;
            double x = -Math.sin(yaw);
            double z = Math.cos(yaw);

            mc.thePlayer.setPosition(oldX + x, mc.thePlayer.posY, oldZ + z);
            tickTimer.reset();
         }
         tickTimer.update();
      }
   }

   public static void onPacket(Packet<?> packet) {
      if (isOn) {
         if (packet instanceof C03PacketPlayer) {
            double yaw = getDirection();

            ((IC03PacketPlayer) packet).setX(((C03PacketPlayer) packet).getPositionX() - Math.sin(yaw) * 0.00000001);
            ((IC03PacketPlayer) packet).setZ(((C03PacketPlayer) packet).getPositionZ() + Math.cos(yaw) * 0.00000001);
         }
      }
   }

   public static double getDirection() {
      if (mc.thePlayer != null) {
         float yaw = mc.thePlayer.rotationYaw;
         float forward = 1f;

         if (mc.thePlayer.movementInput.moveForward < 0f) {
            yaw += 180f;
            forward = -0.5f;
         } else if (mc.thePlayer.movementInput.moveForward > 0f) {
            forward = 0.5f;
         }

         if (mc.thePlayer.movementInput.moveStrafe < 0f) {
            yaw += 90f * forward;
         } else if (mc.thePlayer.movementInput.moveStrafe > 0f) {
            yaw -= 90f * forward;
         }

         return Math.toRadians(yaw);
      } else {
         return 0.0;
      }
   }

   public static boolean collideBlockIntersects(AxisAlignedBB axisAlignedBB) {
      EntityPlayer thePlayer = mc.thePlayer;
      World world = mc.theWorld;

      int y = (int) axisAlignedBB.minY;
      BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(0, 0, 0);
      for (int x = (int) thePlayer.getEntityBoundingBox().minX; x < (int) thePlayer.getEntityBoundingBox().maxX + 1; x++) {
         for (int z = (int) thePlayer.getEntityBoundingBox().minZ; z < (int) thePlayer.getEntityBoundingBox().maxZ + 1; z++) {
            BlockPos blockPos = mutable.set(x, y, z);
            IBlockState state = mc.theWorld.getBlockState(blockPos);
            Block block = state.getBlock();

            if (block != Blocks.air) {
               AxisAlignedBB boundingBox = block.getCollisionBoundingBox(world, blockPos, state);

               if (boundingBox != null && thePlayer.getEntityBoundingBox().intersectsWith(boundingBox)) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   public static class TickTimer {
      private int tick = 0;

      public void update() {
         tick++;
      }

      public void reset() {
         tick = 0;
      }

      public boolean hasTimePassed(int ticks) {
         return tick >= ticks;
      }
   }
}
