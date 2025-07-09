import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;

public class ESP extends Module {

    private Minecraft mc = Minecraft.getMinecraft();

    // Call this method every tick or on a suitable event
    public void printHeadgear() {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase)) continue;

            EntityLivingBase living = (EntityLivingBase) entity;
            ItemStack helmet = living.getEquipmentInSlot(4); // helmet slot

            if (helmet != null) {
                String itemName = getItemName(helmet);
                String entityName = entity.getName();

                System.out.println("[ESP] " + entityName + " is wearing on head: " + itemName);
            }
        }
    }

    private String getItemName(ItemStack stack) {
        if (stack == null) return "Nothing";

        // Try to get display name
        String displayName = stack.getDisplayName();

        if (!StringUtils.isNullOrEmpty(displayName)) {
            return displayName;
        }

        // fallback to item registry name
        return stack.getItem().getUnlocalizedName();
    }
}
