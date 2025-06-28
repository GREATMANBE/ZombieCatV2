package zombiecat.client.module.modules.bannable;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
   public static BooleanSetting pup; // Added pup toggle
   public static SliderSetting a;
   public static SliderSetting predict;
   public static SliderSetting yPredict;

   public Aimbot() {
      super("Aimbot", ModuleCategory.bannable);
      this.registerSetting(a = new SliderSetting("Fineness", 0.4, 0.1, 1.0, 0.1));
      this.registerSetting(onlyFire = new BooleanSetting("OnlyFire", true));
      this.registerSetting(wsStair = new BooleanSetting("WSStair", true));
      this.registerSetting(pup = new BooleanSetting("Pup", false)); // default off
      this.registerSetting(predict = new SliderSetting("Predict", 4, 0, 10, 0.1));
      this.registerSetting(yPredict = new SliderSetting("YPredict", 4, 0, 10, 0.1));
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (onlyFire.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
         return;
      }

      double dis = 9999999;
      Vec3 targetPos = null;
      Entity targetEntity = null;

      if (Utils.Player.isPlayerInGame()) {
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

               // Base position without pumpkin offset
               Vec3 baseTargetPos = entity.getPositionEyes(1).add(offset);

               double distance = fovDistance(baseTargetPos);
               if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), baseTargetPos)) {
                  dis = distance;
                  targetPos = baseTargetPos;
                  targetEntity = entity;
               } else {

                  double yOffset = entity.getPositionEyes(1).yCoord - entity.getPositionVector().yCoord;

                  Vec3 pos1 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.1, 0));
                  distance = fovDistance(pos1);
                  if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos1)) {
                     dis = distance;
                     targetPos = pos1;
                     targetEntity = entity;
                  } else {
                     Vec3 pos2 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.2, 0));
                     distance = fovDistance(pos2);
                     if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos2)) {
                        dis = distance;
                        targetPos = pos2;
                        targetEntity = entity;
                     } else {
                        Vec3 pos3 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.3, 0));
                        distance = fovDistance(pos3);
                        if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos3)) {
                           dis = distance;
                           targetPos = pos3;
                           targetEntity = entity;
                        } else {
                           Vec3 pos4 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.4, 0));
                           distance = fovDistance(pos4);
                           if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos4)) {
                              dis = distance;
                              targetPos = pos4;
                              targetEntity = entity;
                           } else {
                              Vec3 pos5 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.5, 0));
                              distance = fovDistance(pos5);
                              if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos5)) {
                                 dis = distance;
                                 targetPos = pos5;
                                 targetEntity = entity;
                              } else {
                                 Vec3 pos6 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.6, 0));
                                 distance = fovDistance(pos6);
                                 if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos6)) {
                                    dis = distance;
                                    targetPos = pos6;
                                    targetEntity = entity;
                                 } else {
                                    Vec3 pos7 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.7, 0));
                                    distance = fovDistance(pos7);
                                    if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos7)) {
                                       dis = distance;
                                       targetPos = pos7;
                                       targetEntity = entity;
                                    } else {
                                       Vec3 pos8 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.8, 0));
                                       distance = fovDistance(pos8);
                                       if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos8)) {
                                          dis = distance;
                                          targetPos = pos8;
                                          targetEntity = entity;
                                       } else {
                                          Vec3 pos9 = entity.getPositionVector().add(offset).add(new Vec3(0, -yOffset * 0.9, 0));
                                          distance = fovDistance(pos9);
                                          if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos9)) {
                                             dis = distance;
                                             targetPos = pos9;
                                             targetEntity = entity;
                                          } else {
                                             Vec3 pos10 = entity.getPositionVector().add(offset);
                                             distance = fovDistance(pos10);
                                             if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos10)) {
                                                dis = distance;
                                                targetPos = pos10;
                                                targetEntity = entity;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
         
         if (targetEntity != null && targetPos != null) {
            boolean hasPumpkinHead = false;

            // Check if targetEntity has pumpkin head
            if (targetEntity instanceof EntityLivingBase) {
               EntityLivingBase living = (EntityLivingBase) targetEntity;
               ItemStack helmet = living.getEquipmentInSlot(4);
               if (helmet != null) {
                  String helmetName = helmet.getItem().getUnlocalizedName();
                  if (helmetName.contains("pumpkin") || helmetName.contains("jackolantern")) {
                     hasPumpkinHead = true;
                  }
               }
            }

            if (hasPumpkinHead) {
               targetPos = targetPos.addVector(0, 0.2, 0);
            }

            float[] angle = calculateYawPitch(mc.thePlayer.getPositionVector().addVector(0, mc.thePlayer.getEyeHeight(), 0), targetPos);
            mc.thePlayer.rotationYaw = angle[0];
            mc.thePlayer.rotationPitch = angle[1];
         }
      }
   }

   public static double fovDistance(Vec3 vec3) {
      float[] angle = calculateYawPitch(mc.thePlayer.getPositionVector().addVector(0,mc.thePlayer.getEyeHeight(),0), vec3);
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
         if (block instanceof BlockSlab || wsStair.getValue() && block instanceof BlockStairs && block != Blocks.spruce_stairs || block == Blocks.iron_door || block == Blocks.iron_bars || block instanceof BlockSign || block instanceof BlockBarrier) {
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
      return new Vec3((double)(g * h), (double)i, (double)(f * h));
   }

   public static Vec3 getMotionVec(Entity entity, float predict, float yPredict) {
      double x = entity.posX + entity.motionX * (double) predict;
      double y = entity.posY + entity.motionY * (double) yPredict;
      double z = entity.posZ + entity.motionZ * (double) predict;
      return new Vec3(x - entity.posX, y - entity.posY, z - entity.posZ);
   }

   public static float[] calculateYawPitch(Vec3 from, Vec3 to) {
      double difX = to.xCoord - from.xCoord;
      double difY = to.yCoord - from.yCoord;
      double difZ = to.zCoord - from.zCoord;
      double dist = Math.sqrt(difX * difX + difZ * difZ);
      float yaw = (float) Math.toDegrees(Math.atan2(difZ, difX)) - 90.0F;
      float pitch = (float) -Math.toDegrees(Math.atan2(difY, dist));
      return new float[]{yaw, pitch};
   }
}
