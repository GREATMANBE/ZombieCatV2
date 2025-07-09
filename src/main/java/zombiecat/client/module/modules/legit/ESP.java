package zombiecat.client.module.modules.legit;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import zombiecat.client.module.Module;
import zombiecat.client.module.Category;

public class ESP extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public ESP() {
        super("ESP", Category.LEGIT);
    }

    @Override
    public void onUpdate() {
        if (mc.theWorld == null) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;

            ItemStack helmet = living.getEquipmentInSlot(4);

            if (helmet != null) {
                String headgearName = helmet.getDisplayName();

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
