package zombiecat.client.module.modules.bannable;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Aimbot extends Module {
   public static BooleanSetting onlyFire;
   public static BooleanSetting wsStair;
   public static BooleanSetting pup;
   public static BooleanSetting skelePriority;
   public static BooleanSetting mobPriority;
   public static SliderSetting a;
   public static SliderSetting predict;
   public static SliderSetting yPredict;

   public Aimbot() {
      super("Aimbot", ModuleCategory.bannable);
      this.registerSetting(a = new SliderSetting("Fineness", 0.4, 0.1, 1.0, 0.1));
      this.registerSetting(onlyFire = new BooleanSetting("OnlyFire", true));
      this.registerSetting(wsStair = new BooleanSetting("WSStair", true));
      this.registerSetting(pup = new BooleanSetting("Pup", false));
      this.registerSetting(skelePriority = new BooleanSetting("SkelePriority", false));
      this.registerSetting(mobPriority = new BooleanSetting("MobPriority", false));
      this.registerSetting(predict = new SliderSetting("Predict", 1, 0, 2, 0.1));
      this.registerSetting(yPredict = new SliderSetting("YPredict", 1, 0, 2, 0.1));
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (onlyFire.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown()) return;

      double dis = 9999999;
      Vec3 target = null;
      if (Utils.Player.isPlayerInGame()) {
         List<Entity> validTargets = new ArrayList<>();
         boolean prioritizeSkeletons = false;

         if (mobPriority.getValue()) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
               if (isValidNonSkeleton(entity)) validTargets.add(entity);
            }

            if (validTargets.isEmpty()) {
               for (Entity entity : mc.theWorld.loadedEntityList) {
                  if (isSpecialSkeleton(entity)) validTargets.add(entity);
               }
            }

            prioritizeSkeletons = true;
         } else if (skelePriority.getValue()) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
               if (isSpecialSkeleton(entity)) validTargets.add(entity);
            }
            prioritizeSkeletons = true;
         }

         for (Entity entity : mc.theWorld.loadedEntityList) {
            if (prioritizeSkeletons && !validTargets.contains(entity)) continue;

            if (entity instanceof EntityLivingBase && isValidTarget(entity)) {
               if (entity instanceof EntityWolf && !pup.getValue() && ((EntityWolf) entity).isChild()) continue;

               Vec3 offset = getMotionVec(entity);
               double distance = fovDistance(entity.getPositionEyes(1).add(offset));
               Vec3[] possibleTargets = getYOffsetVariants(entity.getPositionVector().add(offset), entity.getPositionEyes(1).yCoord - entity.getPositionVector().yCoord);
               for (Vec3 pos : possibleTargets) {
                  if (distance < dis && canWallShot(mc.thePlayer.getPositionEyes(1), pos)) {
                     dis = distance;
                     target = pos;
                     break;
                  }
               }
            }
         }

         if (target != null) {
            float[] angle = calculateYawPitch(mc.thePlayer.getPositionVector().addVector(0, mc.thePlayer.getEyeHeight(), 0), target);
            mc.thePlayer.rotationYaw = angle[0];
            mc.thePlayer.rotationPitch = angle[1];
         }
      }
   }

   private boolean isValidNonSkeleton(Entity entity) {
      return entity instanceof EntityLivingBase
              && !(entity instanceof EntityArmorStand)
              && !(entity instanceof EntityWither)
              && !(entity instanceof EntityVillager)
              && !(entity instanceof EntityPlayer)
              && !(entity instanceof EntityChicken)
              && !(entity instanceof EntityPig)
              && !(entity instanceof EntityCow)
              && !(entity instanceof EntitySkeleton)
              && entity.isEntityAlive();
   }

   private boolean isValidTarget(Entity entity) {
      return !(entity instanceof EntityArmorStand)
              && !(entity instanceof EntityWither)
              && !(entity instanceof EntityVillager)
              && !(entity instanceof EntityPlayer)
              && !(entity instanceof EntityChicken)
              && !(entity instanceof EntityPig)
              && !(entity instanceof EntityCow)
              && entity.isEntityAlive();
   }

   private boolean isSpecialSkeleton(Entity entity) {
      if (!(entity instanceof EntitySkeleton) || !entity.isEntityAlive()) return false;

      EntitySkeleton skel = (EntitySkeleton) entity;
      ItemStack helmet = skel.getEquipmentInSlot(4);
      ItemStack hand = skel.getHeldItem();
      ItemStack chest = skel.getEquipmentInSlot(3);
      ItemStack legs = skel.getEquipmentInSlot(2);
      ItemStack boots = skel.getEquipmentInSlot(1);

      boolean isPumpkinSword = helmet != null && helmet.getItem() == Item.getItemFromBlock(Blocks.pumpkin)
              && hand != null && hand.getItem() == Items.stone_sword;

      boolean isIronArmorSword = hand != null && hand.getItem() == Items.stone_sword &&
              chest != null && chest.getItem() == Items.iron_chestplate &&
              legs != null && legs.getItem() == Items.iron_leggings &&
              boots != null && boots.getItem() == Items.iron_boots;

      return isPumpkinSword || isIronArmorSword;
   }

   public static Vec3[] getYOffsetVariants(Vec3 base, double eyeOffset) {
      return new Vec3[]{
              base.addVector(0, 0, 0),
              base.addVector(0, -eyeOffset * 0.1, 0),
              base.addVector(0, -eyeOffset * 0.2, 0),
              base.addVector(0, -eyeOffset * 0.3, 0),
              base.addVector(0, -eyeOffset * 0.4, 0),
              base.addVector(0, -eyeOffset * 0.5, 0),
              base.addVector(0, -eyeOffset * 0.6, 0),
              base.addVector(0, -eyeOffset * 0.7, 0),
              base.addVector(0, -eyeOffset * 0.8, 0),
              base.addVector(0, -eyeOffset * 0.9, 0)
      };
   }

   public static double fovDistance(Vec3 vec3) {
      float[] angle = calculateYawPitch(mc.thePlayer.getPositionVector().addVector(0, mc.thePlayer.getEyeHeight(), 0), vec3);
      return angleBetween(angle[0], mc.thePlayer.rotationYaw)
              + Math.abs(angle[1] - mc.thePlayer.rotationPitch);
   }

   public static double angleBetween(double first, double second) {
      return Math.abs(subtractAngles(first, second));
   }

   public static double subtractAngles(double start, double end) {
      return MathHelper.wrapAngleTo180_double(end - start);
   }

   public static boolean canWallShot(Vec3 start, Vec3 end) {
      float[] angle = calculateYawPitch(start, end);
      Vec3 forward = fromPolar(angle[1], angle[0]).scale(a.getValue());
      Vec3 now = start;

      while (now.distanceTo(end) > a.getValue() + 0.1) {
         Block block = mc.theWorld.getBlockState(new BlockPos(now)).getBlock();
         if (block == Blocks.sandstone_stairs) return false;
         if (block instanceof BlockSlab && ((BlockSlab) block).isDouble()) return false;
         if (block instanceof BlockSlab || wsStair.getValue() && block instanceof BlockStairs && block != Blocks.spruce_stairs || block == Blocks.iron_door || block == Blocks.iron_bars || block instanceof BlockSign || block instanceof BlockBarrier) {
            return true;
         }
         if (block != Blocks.air && block != Blocks.grass && block != Blocks.tallgrass) return false;
         now = now.add(forward);
      }

      Block endBlock = mc.theWorld.getBlockState(new BlockPos(end)).getBlock();
      return endBlock == Blocks.air || endBlock == Blocks.iron_bars || endBlock instanceof BlockSlab || endBlock instanceof BlockSign || endBlock instanceof BlockBarrier;
   }

   public static Vec3 fromPolar(float pitch, float yaw) {
      float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
      float g = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
      float h = -MathHelper.cos(-pitch * 0.017453292F);
      float i = MathHelper.sin(-pitch * 0.017453292F);
      return new Vec3(g * h, i, f * h);
   }

   public static float[] calculateYawPitch(Vec3 start, Vec3 vec) {
      double dx = vec.xCoord - start.xCoord;
      double dy = vec.yCoord - start.yCoord;
      double dz = vec.zCoord - start.zCoord;
      double distXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
      return new float[]{MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch)};
   }

   public static Vec3 getMotionVec(Entity entity) {
      double dx = entity.posX - entity.prevPosX;
      double dy = entity.posY - entity.prevPosY;
      double dz = entity.posZ - entity.prevPosZ;

      int pingTicks = getPingTicks(entity);
      float scaleX = (float) pingTicks * predict.getValue();
      float scaleY = (float) pingTicks * yPredict.getValue();

      return new Vec3(dx * scaleX, dy * scaleY, dz * scaleX);
   }

   private static int getPingTicks(Entity entity) {
      if (entity instanceof EntityPlayer) {
         try {
            return mc.getNetHandler().getPlayerInfo(((EntityPlayer) entity).getUniqueID()).getResponseTime() / 50;
         } catch (Exception e) {
            return 0;
         }
      }
      return 0;
   }
}
