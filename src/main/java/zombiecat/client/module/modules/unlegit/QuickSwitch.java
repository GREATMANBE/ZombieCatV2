package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import zombiecat.client.mixins.IKeyBinding;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

public class QuickSwitch extends Module {
   public static BooleanSetting onlyFire;
   public static SliderSetting delay;
   public static BooleanSetting anySlot;
   public static BooleanSetting s2;
   public static BooleanSetting s3;
   public static BooleanSetting s4;
   public static SliderSetting check;

   private long lastSwapTime = 0;
   private boolean hold = false;

   public QuickSwitch() {
      super("QuickSwitch", ModuleCategory.unlegit);
      this.registerSetting(onlyFire = new BooleanSetting("OnlyFire", true));
      this.registerSetting(delay = new SliderSetting("Delay", 71.0, 0.0, 100.0, 1.0));
      this.registerSetting(anySlot = new BooleanSetting("AnySlot", true));
      this.registerSetting(s2 = new BooleanSetting("Slot2", true));
      this.registerSetting(s3 = new BooleanSetting("Slot3", true));
      this.registerSetting(s4 = new BooleanSetting("Slot4", true));
      this.registerSetting(check = new SliderSetting("Check", 0.0, 0.0, 100.0, 1.0));
   }

   @SubscribeEvent
   public void onTick(TickEvent.ClientTickEvent event) {
      if (event.phase != TickEvent.Phase.END || !this.isOn() || mc.thePlayer == null || mc.theWorld == null)
         return;

      if (onlyFire.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown())
         return;

      long now = System.nanoTime();
      long delayNanos = (long) (delay.getValue() * 1_000_000);

      if (now - lastSwapTime < delayNanos)
         return;

      int currentSlot = mc.thePlayer.inventory.currentItem + 1;
      int nextSlot = getNextSlot(currentSlot);

      if (nextSlot != -1) {
         swapToSlot(nextSlot);
         lastSwapTime = now;
      }
   }

   private int getNextSlot(int current) {
      int[] cycle = new int[]{2, 3, 4};
      BooleanSetting[] toggles = new BooleanSetting[]{s2, s3, s4};

      for (int i = 0; i < cycle.length; i++) {
         if (cycle[i] == current) {
            for (int j = 1; j <= 3; j++) {
               int idx = (i + j) % 3;
               int slot = cycle[idx];
               if (toggles[idx].getValue()) {
                  ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot - 1);
                  if (stack != null && isValid(stack, slot)) {
                     return slot;
                  }
               }
            }
         }
      }

      if (anySlot.getValue() && current != 1) {
         for (int i = 0; i < 3; i++) {
            if (toggles[i].getValue()) {
               int slot = cycle[i];
               ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot - 1);
               if (stack != null && isValid(stack, slot)) {
                  return slot;
               }
            }
         }
      }

      return -1;
   }

   private boolean isValid(ItemStack stack, int slot) {
      if (stack.getItemDamage() == 0 || stack.stackSize != 1)
         return true;

      double ratio = 1.0 - (double) stack.getItemDamage() / stack.getMaxDamage();
      return check.getValue() <= 0 || ratio >= check.getValue() / 100.0;
   }

   private void swapToSlot(int slot) {
      for (KeyBinding bind : mc.gameSettings.keyBindsHotbar) {
         KeyBinding.setKeyBindState(bind.getKeyCode(), false);
      }

      int index = slot - 1;
      if (index >= 0 && index < mc.gameSettings.keyBindsHotbar.length) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindsHotbar[index].getKeyCode(), true);
         ((IKeyBinding) mc.gameSettings.keyBindsHotbar[index]).setPressTime(1);
      }
   }

   @SubscribeEvent
   public void renderDebug(TickEvent.RenderTickEvent ev) {
      if (ev.phase == TickEvent.Phase.END && Utils.Player.isPlayerInGame()) {
         mc.fontRendererObj.drawStringWithShadow("QuickSwitch: " + delay.getValue() + "ms", 10, 10, 0xFFFFFF);
      }
   }
}
