package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import zombiecat.client.mixins.IKeyBinding;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;
import zombiecat.client.utils.WatchTimer;

public class QuickSwitch extends Module {
   public static BooleanSetting onlyFire;
   public static SliderSetting delay;
   public static BooleanSetting anySlot;
   public static BooleanSetting s2;
   public static BooleanSetting s3;
   public static BooleanSetting s4;
   public static SliderSetting check;
   public static QuickSwitch INSTANCE;
   private final WatchTimer timer = new WatchTimer();
   private boolean hold = false;
   private int currentIndex = 0;
   private final int[] sequence = {2, 3, 4};

   public QuickSwitch() {
      super("QuickSwitch", ModuleCategory.unlegit);
      this.registerSetting(onlyFire = new BooleanSetting("OnlyFire", true));
      this.registerSetting(delay = new SliderSetting("Delay", 71.0, 10.0, 200.0, 1.0));
      this.registerSetting(anySlot = new BooleanSetting("AnySlot", true));
      this.registerSetting(s2 = new BooleanSetting("Slot2", true));
      this.registerSetting(s3 = new BooleanSetting("Slot3", true));
      this.registerSetting(s4 = new BooleanSetting("Slot4", true));
      this.registerSetting(check = new SliderSetting("Check", 0.0, 0.0, 100.0, 1.0));
      INSTANCE = this;
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (!this.isOn() || mc.thePlayer == null || mc.theWorld == null) return;

      if (onlyFire.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown()) return;
      if (!timer.passed(delay.getValue())) return;

      timer.reset();
      int nextSlot = sequence[currentIndex];
      currentIndex = (currentIndex + 1) % sequence.length;

      if (isToggled(nextSlot)) {
         doSwap(nextSlot);
      }
   }

   public static void doSwap(int slot) {
      for (KeyBinding bind : mc.gameSettings.keyBindsHotbar) {
         KeyBinding.setKeyBindState(bind.getKeyCode(), false);
      }

      KeyBinding keyBind = mc.gameSettings.keyBindsHotbar[slot - 1];
      KeyBinding.setKeyBindState(keyBind.getKeyCode(), true);
      ((IKeyBinding) keyBind).setPressTime(1);

      // Release the keybind immediately to simulate a real key press
      KeyBinding.setKeyBindState(keyBind.getKeyCode(), false);
   }

   private boolean isToggled(int slot) {
      ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot - 1);
      if (stack == null || stack.getItem() == Items.dye) return false;

      if (check.getValue() > 0 && slot >= 2 && slot <= 4 && stack.stackSize == 1) {
         double durability = 1.0 - (double) stack.getItemDamage() / stack.getMaxDamage();
         if (durability < check.getValue() / 100.0) return false;
      }

      switch (slot) {
         case 2: return s2.getValue();
         case 3: return s3.getValue();
         case 4: return s4.getValue();
         default: return false;
      }
   }
}
