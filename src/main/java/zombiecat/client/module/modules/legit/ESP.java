package zombiecat.client.module.modules.legit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.Minecraft;

public class ESP {

    private final Minecraft mc = Minecraft.getMinecraft();

    public ESP() {
        // Constructor
    }

    // Call this method regularly (e.g., each tick) to print headgear info for mobs
    public void printHeadgearInfo() {
        if (mc.theWorld == null) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;

            ItemStack helmet = living.getEquipmentInSlot(4); // 4 is helmet slot

            if (helmet != null) {
                String headgearName = helmet.getDisplayName();

                // Extra info if skull with NBT data
                if (helmet.getItem() == Items.skull) {
                    NBTTagCompound tag = helmet.getTagCompound();
                    if (tag != null && tag.hasKey("SkullOwner")) {
                        headgearName += " (Owner: " + tag.getString("SkullOwner") + ")";
                    }
                }

                System.out.println(entity.getName() + " is wearing on head: " + headgearName);
            } else {
                System.out.println(entity.getName() + " has no headgear.");
            }
        }
    }
}
