package zombiecat.client.module.modules.legit;

import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.StringSetting;
import zombiecat.client.utils.Utils;
import java.awt.*;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.entity.EntityLivingBase;
import java.io.*;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraftforge.client.ClientCommandHandler;

public class ESP extends Module {

   public class CommandESP extends CommandBase {
      @Override
      public String getCommandName() {
         return "esp";
      }

       @Override
       public String getCommandUsage(ICommandSender sender) {
           return "/esp add <x> <y> <z> OR /esp remove <x> <y> <z>";
       }

       @Override
       public void processCommand(ICommandSender sender, String[] args) {
           if (args.length < 4) {
               sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
               return;
           }
           String sub = args[0];
           try {
               int x = Integer.parseInt(args[1]);
               int y = Integer.parseInt(args[2]);
               int z = Integer.parseInt(args[3]);
               BlockPos pos = new BlockPos(x, y, z);
   
               if ("add".equalsIgnoreCase(sub)) {
                   if (addTrackedCoord(pos)) {
                       sender.addChatMessage(new ChatComponentText("Added ESP coordinate: " + pos));
                   } else {
                       sender.addChatMessage(new ChatComponentText("Coordinate already tracked."));
                   }
               } else if ("remove".equalsIgnoreCase(sub)) {
                   if (removeTrackedCoord(pos)) {
                       sender.addChatMessage(new ChatComponentText("Removed ESP coordinate: " + pos));
                   } else {
                       sender.addChatMessage(new ChatComponentText("Coordinate not found."));
                   }
               } else {
                   sender.addChatMessage(new ChatComponentText("Unknown subcommand."));
               }
           } catch (NumberFormatException e) {
               sender.addChatMessage(new ChatComponentText("Coordinates must be integers."));
           }
       }
   }

   private final StringSetting colorSetting = new StringSetting("Color", "Green", "Green", "Black", "White");
   private final CopyOnWriteArrayList<BlockPos> trackedCoords = new CopyOnWriteArrayList<>();
   private final File coordsFile = new File(mc.mcDataDir, "esp_coords.txt");

   public ESP() {
      super("ESP", Module.ModuleCategory.legit);
      this.registerSetting(colorSetting);
      loadCoordsFromFile();
      ClientCommandHandler.instance.registerCommand(new CommandESP());
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (Utils.Player.isPlayerInGame()) {
         checkForSpawnedEntities();
         trace();
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

               int color = getESPColorRGB();

               if (entity instanceof EntityZombie && ((EntityZombie) entity).isChild() && entity.getInventory() != null && entity.getInventory()[0] != null && entity.getInventory()[0].getItem() == Items.diamond_sword) {
                  Utils.HUD.drawBoxAroundEntity(entity, true, Color.red.getRGB());
               } else if (((EntityLivingBase) entity).isPotionActive(Potion.invisibility)) {
                  Utils.HUD.drawBoxAroundEntity(entity, true, Color.blue.getRGB());
               } else if (entity instanceof EntityZombie) {
                  EntityLivingBase living = (EntityLivingBase) entity;
                  ItemStack chest = living.getEquipmentInSlot(3);
                  ItemStack legs = living.getEquipmentInSlot(2);
                  ItemStack boots = living.getEquipmentInSlot(1);
                  ItemStack mainHand = living.getHeldItem();

                  boolean chestBlack = chest != null && chest.getItem() == Items.leather_chestplate && chest.hasTagCompound() &&
                          chest.getTagCompound().hasKey("display") &&
                          chest.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          chest.getTagCompound().getCompoundTag("display").getInteger("color") == 0x000000;

                  boolean legsBlack = legs != null && legs.getItem() == Items.leather_leggings && legs.hasTagCompound() &&
                          legs.getTagCompound().hasKey("display") &&
                          legs.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          legs.getTagCompound().getCompoundTag("display").getInteger("color") == 0x000000;

                  boolean bootsBlack = boots != null && boots.getItem() == Items.leather_boots && boots.hasTagCompound() &&
                          boots.getTagCompound().hasKey("display") &&
                          boots.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          boots.getTagCompound().getCompoundTag("display").getInteger("color") == 0x000000;

                  boolean chestLime = chest != null && chest.getItem() == Items.leather_chestplate && chest.hasTagCompound() &&
                          chest.getTagCompound().hasKey("display") &&
                          chest.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          chest.getTagCompound().getCompoundTag("display").getInteger("color") == 0x55FF55;

                  boolean legsLime = legs != null && legs.getItem() == Items.leather_leggings && legs.hasTagCompound() &&
                          legs.getTagCompound().hasKey("display") &&
                          legs.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          legs.getTagCompound().getCompoundTag("display").getInteger("color") == 0x55FF55;

                  boolean bootsLime = boots != null && boots.getItem() == Items.leather_boots && boots.hasTagCompound() &&
                          boots.getTagCompound().hasKey("display") &&
                          boots.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          boots.getTagCompound().getCompoundTag("display").getInteger("color") == 0x55FF55;

                  boolean chestYellow = chest != null && chest.getItem() == Items.leather_chestplate && chest.hasTagCompound() &&
                          chest.getTagCompound().hasKey("display") &&
                          chest.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          chest.getTagCompound().getCompoundTag("display").getInteger("color") == 0xFFAA00;

                  boolean legsYellow = legs != null && legs.getItem() == Items.leather_leggings && legs.hasTagCompound() &&
                          legs.getTagCompound().hasKey("display") &&
                          legs.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          legs.getTagCompound().getCompoundTag("display").getInteger("color") == 0xFF5555;

                  boolean bootsYellow = boots != null && boots.getItem() == Items.leather_boots && boots.hasTagCompound() &&
                          boots.getTagCompound().hasKey("display") &&
                          boots.getTagCompound().getCompoundTag("display").hasKey("color") &&
                          boots.getTagCompound().getCompoundTag("display").getInteger("color") == 0xAA0000;

                  boolean holdingGoldSword = mainHand != null && mainHand.getItem() == Items.golden_sword;
                  boolean holdingNothing = mainHand == null || mainHand.getItem() == null;

                  boolean chestGold = chest != null && chest.getItem() == Items.golden_chestplate;
                  boolean legsGold = legs != null && legs.getItem() == Items.golden_leggings;
                  boolean bootsGold = boots != null && boots.getItem() == Items.golden_boots;

                  if (chestBlack && legsBlack && bootsBlack && holdingNothing && !((EntityZombie) entity).isChild()) {
                     Utils.HUD.drawBoxAroundEntity(entity, true, Color.red.getRGB());
                  } else if (chestLime && legsLime && bootsLime && holdingNothing) {
                     Utils.HUD.drawBoxAroundEntity(entity, true, Color.red.getRGB());
                  } else if (chestYellow && legsYellow && bootsYellow && holdingGoldSword) {
                     Utils.HUD.drawBoxAroundEntity(entity, true, Color.red.getRGB());
                  } else if (chestGold && legsGold && bootsGold && holdingNothing) {
                     Utils.HUD.drawBoxAroundEntity(entity, true, Color.red.getRGB());
                  } else {
                     Utils.HUD.drawBoxAroundEntity(entity, true, color);
                  }
               } else {
                  Utils.HUD.drawBoxAroundEntity(entity, true, color);
               }
            }
         }
      }
   }
   private void checkForSpawnedEntities() {
      for (Entity entity : mc.theWorld.loadedEntityList) {
         BlockPos entityPos = entity.getPosition();
         if (trackedCoords.contains(entityPos)) {
                     // Play sound when mob spawns at tracked coords
            mc.theWorld.playSoundEffect(entityPos.getX(), entityPos.getY(), entityPos.getZ(), "random.levelup", 1.0F, 1.0F);
                     // Optionally remove coordinate after detection if only want 1-time alert:
                     // trackedCoords.remove(entityPos);
                     // saveCoordsToFile();
                 }
             }
         }

   public void trace() {
      Entity thePlayer = mc.thePlayer;
      if (thePlayer == null) return;

      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
      GL11.glLineWidth(2);
      GL11.glDisable(GL11.GL_TEXTURE_2D);
      GL11.glDisable(GL11.GL_DEPTH_TEST);
      GL11.glDepthMask(false);

      GL11.glBegin(GL11.GL_LINES);

      List<Entity> entities = mc.theWorld.loadedEntityList;
      Color color = new Color(255, 0, 0, 150);
      for (Entity entity : entities) {
         if (entity != thePlayer) {
            if (entity instanceof EntityZombie && ((EntityZombie) entity).isChild()
                    && entity.getInventory() != null
                    && entity.getInventory()[0] != null
                    && entity.getInventory()[0].getItem() == Items.diamond_sword) {
               drawTraces(entity, color);
            }

            if (entity instanceof EntityZombie) {
               EntityLivingBase living = (EntityLivingBase) entity;
               ItemStack chest = living.getEquipmentInSlot(3);
               ItemStack legs = living.getEquipmentInSlot(2);
               ItemStack boots = living.getEquipmentInSlot(1);
               ItemStack mainHand = living.getHeldItem();

               boolean chestLime = chest != null && chest.getItem() == Items.leather_chestplate && chest.hasTagCompound() &&
                       chest.getTagCompound().hasKey("display") &&
                       chest.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       chest.getTagCompound().getCompoundTag("display").getInteger("color") == 0x55FF55;

               boolean legsLime = legs != null && legs.getItem() == Items.leather_leggings && legs.hasTagCompound() &&
                       legs.getTagCompound().hasKey("display") &&
                       legs.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       legs.getTagCompound().getCompoundTag("display").getInteger("color") == 0x55FF55;

               boolean bootsLime = boots != null && boots.getItem() == Items.leather_boots && boots.hasTagCompound() &&
                       boots.getTagCompound().hasKey("display") &&
                       boots.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       boots.getTagCompound().getCompoundTag("display").getInteger("color") == 0x55FF55;

               boolean chestBlack = chest != null && chest.getItem() == Items.leather_chestplate && chest.hasTagCompound() &&
                       chest.getTagCompound().hasKey("display") &&
                       chest.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       chest.getTagCompound().getCompoundTag("display").getInteger("color") == 0x000000;

               boolean legsBlack = legs != null && legs.getItem() == Items.leather_leggings && legs.hasTagCompound() &&
                       legs.getTagCompound().hasKey("display") &&
                       legs.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       legs.getTagCompound().getCompoundTag("display").getInteger("color") == 0x000000;

               boolean bootsBlack = boots != null && boots.getItem() == Items.leather_boots && boots.hasTagCompound() &&
                       boots.getTagCompound().hasKey("display") &&
                       boots.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       boots.getTagCompound().getCompoundTag("display").getInteger("color") == 0x000000;

               boolean chestYellow = chest != null && chest.getItem() == Items.leather_chestplate && chest.hasTagCompound() &&
                       chest.getTagCompound().hasKey("display") &&
                       chest.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       chest.getTagCompound().getCompoundTag("display").getInteger("color") == 0xFFAA00;

               boolean legsYellow = legs != null && legs.getItem() == Items.leather_leggings && legs.hasTagCompound() &&
                       legs.getTagCompound().hasKey("display") &&
                       legs.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       legs.getTagCompound().getCompoundTag("display").getInteger("color") == 0xFF5555;

               boolean bootsYellow = boots != null && boots.getItem() == Items.leather_boots && boots.hasTagCompound() &&
                       boots.getTagCompound().hasKey("display") &&
                       boots.getTagCompound().getCompoundTag("display").hasKey("color") &&
                       boots.getTagCompound().getCompoundTag("display").getInteger("color") == 0xAA0000;

               boolean holdingGoldSword = mainHand != null && mainHand.getItem() == Items.golden_sword;
               boolean holdingNothing = mainHand == null || mainHand.getItem() == null;

               boolean chestGold = chest != null && chest.getItem() == Items.golden_chestplate;
               boolean legsGold = legs != null && legs.getItem() == Items.golden_leggings;
               boolean bootsGold = boots != null && boots.getItem() == Items.golden_boots;

               if (chestLime && legsLime && bootsLime && holdingNothing) {
                  drawTraces(entity, new Color(255, 0, 0, 150));
               }

               if (chestBlack && legsBlack && bootsBlack && holdingNothing && !((EntityZombie) entity).isChild()) {
                  drawTraces(entity, new Color(255, 0, 0, 150));
               }

               if (chestYellow && legsYellow && bootsYellow && holdingGoldSword) {
                  drawTraces(entity, new Color(255, 0, 0, 150));
               }

               if (chestGold && legsGold && bootsGold && holdingNothing) {
                  drawTraces(entity, new Color(255, 0, 0, 150));
               }
            }
         }
      }

      GL11.glEnd();

      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL11.glDisable(GL11.GL_LINE_SMOOTH);
      GL11.glEnable(GL11.GL_DEPTH_TEST);
      GL11.glDepthMask(true);
      GL11.glDisable(GL11.GL_BLEND);
      GL11.glColor4f(1f, 1f, 1f, 1f);
   }

   private void drawTraces(Entity entity, Color color) {
      Entity thePlayer = mc.thePlayer;
      if (thePlayer == null) return;

      double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * Utils.Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
      double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * Utils.Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
      double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * Utils.Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;

      float yaw = thePlayer.prevRotationYaw + (thePlayer.rotationYaw - thePlayer.prevRotationYaw) * Utils.Client.getTimer().renderPartialTicks;
      float pitch = thePlayer.prevRotationPitch + (thePlayer.rotationPitch - thePlayer.prevRotationPitch) * Utils.Client.getTimer().renderPartialTicks;

      Vec3 eyeVector = new Vec3(0.0, 0.0, 1.0).rotatePitch(-(float) Math.toRadians(pitch)).rotateYaw(-(float) Math.toRadians(yaw));

      GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

      GL11.glVertex3d(eyeVector.xCoord, thePlayer.getEyeHeight() + eyeVector.yCoord, eyeVector.zCoord);
      GL11.glVertex3d(x, y, z);
      GL11.glVertex3d(x, y, z);
      GL11.glVertex3d(x, y + entity.height, z);
   }
   
   public boolean addTrackedCoord(BlockPos pos) {
   if (!trackedCoords.contains(pos)) {
      trackedCoords.add(pos);
      saveCoordsToFile();
      return true;
      }
    return false;
   }

public boolean removeTrackedCoord(BlockPos pos) {
    if (trackedCoords.remove(pos)) {
        saveCoordsToFile();
        return true;
    }
    return false;
}

   private void saveCoordsToFile() {
   try (BufferedWriter writer = new BufferedWriter(new FileWriter(coordsFile))) {
      for (BlockPos pos : trackedCoords) {
         writer.write(pos.getX() + " " + pos.getY() + " " + pos.getZ());
         writer.newLine();
      }
    } catch (IOException e) {
        e.printStackTrace();
      }
   }

   private void loadCoordsFromFile() {
      if (!coordsFile.exists()) return;
      try (BufferedReader reader = new BufferedReader(new FileReader(coordsFile))) {
         String line;
         while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            if (parts.length == 3) {
               try {
                  int x = Integer.parseInt(parts[0]);
                  int y = Integer.parseInt(parts[1]);
                  int z = Integer.parseInt(parts[2]);
                  trackedCoords.add(new BlockPos(x, y, z));
               } catch (NumberFormatException ignored) { }
            }
        }
    } catch (IOException e) {
         e.printStackTrace();
    }
}

   private int getESPColorRGB() {
      switch (colorSetting.getValue().toLowerCase()) {
         case "black":
            return Color.black.getRGB();
         case "white":
            return Color.white.getRGB();
         case "green":
         default:
            return Color.green.getRGB();
      }
   }
}
