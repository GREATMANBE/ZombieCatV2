package zombiecat.client.module.modules.bannable;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

public class Aimbot extends Module {
   public static BooleanSetting onlyFire;
   public static BooleanSetting wsStair;
   public static BooleanSetting pup; // Puppy toggle
   public static BooleanSetting skelePriority; // New toggle for pumpkin priority
   public static SliderSetting a;
   public static SliderSetting predict;
   public static SliderSetting yPredict;

   public Aimbot() {
      super("Aimbot", ModuleCategory.bannable);
      this.registerSetting(a = new SliderSetting("Fineness", 0.4, 0.1, 1.0, 0.1));
      this.registerSetting(onlyFire = new BooleanSetting("OnlyFire", true));
      this.registerSetting(wsStair = new BooleanSetting("WSStair", true));
      this.registerSetting(pup = new BooleanSetting("Pup", false)); // default off
      this.registerSetting(skelePriority = new BooleanSetting("SkelePriority", false)); // default off
      this.registerSetting(predict = new SliderSetting("Predict", 4, 0, 10, 0.1));
      this.registerSetting(yPredict = new SliderSetting("YPredict", 4, 0, 10, 0.1));
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (onlyFire.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
         return;
      }

      if (!Utils.Player.isPlayerInGame()) return;

      double dis = Double.MAX_VALUE;
      Vec3 target = null;
      Entity targetEntity = null;

      // Two separate candidates lists for prioritization
      // First list: pumpkin helmets
      // Second list: normal valid targets
      Entity pumpkinTargetEntity = null;
      Vec3 pumpkinTarget = null;
      double pumpkinDis = Double.MAX_VALUE;

      Entity normalTargetEntity = null;
      Vec3 normalTarget = null;
      double normalDis = Double.MAX_VALUE;

      for (Entity entity : mc.theWorld.loadedEntityList) {
         if (entity instanceof EntityLivingBase
                 && !(entity instanceof EntityArmorStand)
                 && !(entity instanceof EntityWither)
                 && !(entity instanceof EntityVillager)
                 && !(entity instanceof EntityPlayer)
                 && !(entity instanceof EntityChicken)
                 && !(entity instanceof EntityPig)
                 && !(entity instanceof EntityCow)
                 && entity.isEntityAlive()) {

            if (entity instanceof EntityWolf) {
               EntityWolf wolf = (EntityWolf) entity;
               if (!pup.getValue() && wolf.isChild()) {
                  continue;
               }
            }

            Vec3 offset = getMotionVec(entity, (float) predict.getValue(), (float) yPredict.getValue());

            // Helper to test multiple Y offsets - your existing multi-offset logic factored into a function to reduce clutter
            Vec3 bestOffsetTarget = null;
            double bestOffsetDis = Double.MAX_VALUE;

            double[] yOffsets = {0, -0.1, -0.2, -0.3, -0.4, -0.5, -0.6, -0.7, -0.8, -0.9};
            for (double yOffsetMul : yOffsets) {
               double yOffset = entity.getPositionEyes(1).yCoord - entity.getPositionVector().yCoord;
               Vec3 testTarget = entity.getPositionVector().add(offset).add(new Vec3(0, yOffsetMul * yOffset, 0));
               double distance = fovDistance(testTarget);
               if (distance < bestOffsetDis && canWallShot(mc.thePlayer.getPositionEyes(1), testTarget)) {
                  bestOffsetDis = distance;
                  bestOffsetTarget = testTarget;
               }
            }
            // Also test entity.getPositionEyes with offset, as in original code
            double eyesDistance = fovDistance(entity.getPositionEyes(1).add(offset));
            if (eyesDistance < bestOffsetDis && canWallShot(mc.thePlayer.getPositionEyes(1), entity.getPositionEyes(1).add(offset))) {
               bestOffsetDis = eyesDistance;
               bestOffsetTarget = entity.getPositionEyes(1).add(offset);
            }

            if (bestOffsetTarget == null) continue;

            boolean hasPumpkin = false;
            if (entity instanceof EntityLivingBase) {
               EntityLivingBase living = (EntityLivingBase) entity;
               ItemStack helmet = living.getEquipmentInSlot(4);
               if (helmet != null && helmet.getItem() == Item.getItemFromBlock(Blocks.pumpkin)) {
                  hasPumpkin = true;
               }
            }

            if (skelePriority.getValue() && hasPumpkin) {
               // Candidate pumpkin target
               if (bestOffsetDis < pumpkinDis) {
                  pumpkinDis = bestOffsetDis;
                  pumpkinTarget = bestOffsetTarget;
                  pumpkinTargetEntity = entity;
               }
            } else {
               // Candidate normal target
               if (bestOffsetDis < normalDis) {
                  normalDis = bestOffsetDis;
                  normalTarget = bestOffsetTarget;
                  normalTargetEntity = entity;
               }
            }
         }
      }

      // Choose pumpkin target if prioritizing and found any, else normal target
      if (skelePriority.getValue() && pumpkinTargetEntity != null) {
         target = pumpkinTarget;
         targetEntity = pumpkinTargetEntity;
         dis = pumpkinDis;
      } else {
         target = normalTarget;
         targetEntity = normalTargetEntity;
         dis = normalDis;
      }

      if (target != null && targetEntity != null) {
         // Adjust target Y by +0.2 if entity has a pumpkin on head (your original adjustment)
         if (targetEntity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) targetEntity;
            ItemStack helmet = living.getEquipmentInSlot(4);
            if (helmet != null && helmet.getItem() == Item.getItemFromBlock(Blocks.pumpkin)) {
               target = target.addVector(0, 0.2, 0);
            }
         }

         float[] angle = calculateYawPitch(mc.thePlayer.getPositionVector().addVector(0, mc.thePlayer.getEyeHeight(), 0), target);
         mc.thePlayer.rotationYaw = angle[0];
         mc.thePlayer.rotationPitch = angle[1];
      }
   }

   public static double fovDistance(Vec3 vec3) {
      float[] angle = calculateYawPitch(mc.thePlayer.getPositionVector().addVector(0, mc.thePlayer.getEyeHeight(), 0), vec3);
      return angleBetween(angle[0], mc.thePlayer.rotationYaw) +
              Math.abs(Math.max(angle[1], mc.thePlayer.rotationPitch) - Math.min(angle[1], mc.thePlayer.rotationPitch));
   }

   public static double angleBetween(double first, double second) {
      return Math.abs(subtractAngles(first, second));
   }

   public static double subtractAngles(double start, double end) {
      return MathHelper.wrapAngleTo180_double(end - start);
   }

   public static boolean canWallShot(Vec3 start, Vec3 end) {
      float[] angle = calculateYawPitch(start, end);

      Vec3 temp = fromPolar(angle[1], angle[0]);
      Vec3 forward = new Vec3(temp.xCoord * a.getValue(), temp.yCoord * a.getValue(), temp.zCoord * a.getValue());
      Vec3 now = start;
      while (now.distanceTo(end) > a.getValue() + 0.1) {
         Block block = mc.theWorld.getBlockState(new BlockPos(now)).getBlock();
         if (block == Blocks.sandstone_stairs) {
            return false;
         }
         if (block instanceof BlockSlab && ((BlockSlab) block).isDouble()) {
            return false;
         }
         if (block instanceof BlockSlab || (wsStair.getValue() && block instanceof BlockStairs && block != Blocks.spruce_stairs) || block == Blocks.iron_door || block == Blocks.iron_bars || block instanceof BlockSign || block instanceof BlockBarrier) {
            return true;
         }
         if (block != Blocks.air && block != Blocks.grass && block != Blocks.tallgrass) {
            return false;
         }
         now = now.add(forward);
      }
      Block endBlock = mc.theWorld.getBlockState(new BlockPos(end)).getBlock();
      return endBlock == Blocks.air || endBlock == Blocks.iron_bars || endBlock instanceof BlockSlab || endBlock instanceof BlockSign || endBlock instanceof BlockBarrier;
   }

   public static Vec3 fromPolar(float pitch, float yaw) {
      float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
      float g = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
      float h = -MathHelper.cos(-pitch * 0.017453292F);
      float i = MathHelper.sin(-pitch * 0.017453292F);
      return new Vec3(g * h, i, f * h);
   }

   public static float[] calculateYawPitch(Vec3 start, Vec3 vec) {
      double diffX = vec.xCoord - start.xCoord;
      double diffY = vec.yCoord - start.yCoord;
      double diffZ = vec.zCoord - start.zCoord;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
      float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new float[]{MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch)};
   }

   public static Vec3 getMotionVec(Entity entity, float ticks, float yTicks) {
      double dX = entity.posX - entity.prevPosX;
      double dY = entity.posY - entity.prevPosY;
      double dZ = entity.posZ - entity.prevPosZ;
      double entityMotionPosX = 0;
      double entityMotionPosY = 0;
      double entityMotionPosZ = 0;
      for (double i = 1; i <= ticks; i = i + 0.3) {
         for (double i2 = 1; i2 <= yTicks; i2 = i2 + 0.3) {
            if (!mc.theWorld.checkBlockCollision(entity.getEntityBoundingBox().offset(dX * i, dY * i2, dZ * i))) {
               entityMotionPosX = dX * i;
               entityMotionPosY = dY * i2;
               entityMotionPosZ = dZ * i;
            } else {
               break;
            }
         }
      }

      return new Vec3(entityMotionPosX, entityMotionPosY, entityMotionPosZ);
   }
}
