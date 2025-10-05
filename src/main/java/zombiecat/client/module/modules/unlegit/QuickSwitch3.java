package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import zombiecat.client.mixins.IKeyBinding;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.Setting;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.WatchTimer;

public class QuickSwitch3 extends Module {
  public static BooleanSetting onlyFire;
  
  public static SliderSetting delay;
  
  public static BooleanSetting anySlot;
  
  public static BooleanSetting s2;
  
  public static BooleanSetting s3;
  
  public static BooleanSetting s4;
  
  public static SliderSetting check;
  
  public static BooleanSetting hudDisplay;
  
  public static BooleanSetting blockRightClick;
  
  public static SliderSetting clicksPerSecond;
  
  public static QuickSwitch3 INSTANCE;
  
  private final WatchTimer swapTimer = new WatchTimer();
  
  private final WatchTimer clickTimer = new WatchTimer();
  
  private boolean hold = false;
  
  private boolean allowSlot6Click = false;
  
  public QuickSwitch3() {
    super("QuickSwitch3", Module.ModuleCategory.unlegit);
    registerSetting((Setting)(onlyFire = new BooleanSetting("OnlyFire", true)));
    registerSetting((Setting)(delay = new SliderSetting("Delay", 50.0D, 0.0D, 80.0D, 1.0D)));
    registerSetting((Setting)(anySlot = new BooleanSetting("AnySlot", true)));
    registerSetting((Setting)(s2 = new BooleanSetting("Slot2", true)));
    registerSetting((Setting)(s3 = new BooleanSetting("Slot3", true)));
    registerSetting((Setting)(s4 = new BooleanSetting("Slot4", true)));
    registerSetting((Setting)(check = new SliderSetting("Check", 0.0D, 0.0D, 100.0D, 1.0D)));
    registerSetting((Setting)(hudDisplay = new BooleanSetting("HudDisplay", true)));
    registerSetting((Setting)(blockRightClick = new BooleanSetting("BlockRightClick", false)));
    registerSetting((Setting)(clicksPerSecond = new SliderSetting("ClicksPerSecond", 40.0D, 10.0D, 50.0D, 1.0D)));
    INSTANCE = this;
  }
  
  public String getHudLabel() {
    if (!hudDisplay.getValue())
      return getName(); 
    StringBuilder slots = new StringBuilder();
    if (s2.getValue())
      slots.append('2'); 
    if (s3.getValue())
      slots.append('3'); 
    if (s4.getValue())
      slots.append('4'); 
    String slotStr = (slots.length() == 0) ? "-" : slots.toString();
    long ms = Math.round(delay.getValue());
    long cps = Math.round(clicksPerSecond.getValue());
    return getName() + " " + slotStr + " " + ms + "ms " + cps + "Hz";
  }
  
  @SubscribeEvent
  public void onRightClick(PlayerInteractEvent event) {
    if (isOn() && blockRightClick.getValue() && (
      event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK))
      event.setCanceled(true); 
  }
  
  @SubscribeEvent
  public void re(RenderWorldLastEvent e) {
    if (!isOn() || mc.field_71439_g == null || mc.field_71441_e == null)
      return; 
    if (!Keyboard.isKeyDown(29)) {
      this.hold = false;
    } else {
      boolean isHold = false;
      if (Keyboard.isKeyDown(78)) {
        isHold = true;
        if (!this.hold)
          delay.setValue(delay.getValue() + 1.0D); 
      } 
      if (Keyboard.isKeyDown(74)) {
        isHold = true;
        if (!this.hold)
          delay.setValue(delay.getValue() - 1.0D); 
      } 
      for (int i = 0; i < mc.field_71474_y.field_151456_ac.length; i++) {
        if (Keyboard.isKeyDown(mc.field_71474_y.field_151456_ac[i].func_151463_i())) {
          isHold = true;
          if (!this.hold) {
            switch (i) {
              case 1:
                s2.setEnabled(!s2.getValue());
                break;
              case 2:
                s3.setEnabled(!s3.getValue());
                break;
              case 3:
                s4.setEnabled(!s4.getValue());
                break;
            } 
            break;
          } 
        } 
      } 
      this.hold = isHold;
    } 
    if (onlyFire.getValue() && !mc.field_71474_y.field_74313_G.func_151470_d())
      return; 
    if (this.swapTimer.passed(delay.getValue())) {
      this.swapTimer.reset();
      performSwapCycle();
    } 
    double interval = 1000.0D / clicksPerSecond.getValue();
    if (this.clickTimer.passed(interval)) {
      this.clickTimer.reset();
      int currentSlot = mc.field_71439_g.field_71071_by.field_70461_c + 1;
      if (this.allowSlot6Click && currentSlot != 6)
        doSlot6Click(); 
    } 
  }
  
  private void performSwapCycle() {
    int cSlot = mc.field_71439_g.field_71071_by.field_70461_c + 1;
    if (cSlot == 2 || cSlot == 3 || cSlot == 4) {
      this.allowSlot6Click = true;
    } else if (cSlot == 6) {
      this.allowSlot6Click = false;
    } 
    if (cSlot == 2 && s2.getValue()) {
      int[] targets = { 3, 4, 6, 7, 8, 9 };
      for (int slot : targets) {
        if (isToggled(slot)) {
          doSwap(slot);
          return;
        } 
      } 
    } 
    if (cSlot == 3 && s3.getValue()) {
      int[] targets = { 4, 6, 7, 8, 9, 2 };
      for (int slot : targets) {
        if (isToggled(slot)) {
          doSwap(slot);
          return;
        } 
      } 
    } 
    if (cSlot == 4 && s4.getValue()) {
      int[] targets = { 6, 7, 8, 9, 2, 3 };
      for (int slot : targets) {
        if (isToggled(slot)) {
          doSwap(slot);
          return;
        } 
      } 
    } 
    if (anySlot.getValue() && cSlot != 1) {
      int[] targets = { 2, 3, 4, 6, 7, 8, 9 };
      for (int slot : targets) {
        if (isToggled(slot)) {
          doSwap(slot);
          return;
        } 
      } 
    } 
  }
  
  private void doSlot6Click() {
    try {
      int slotId = 41;
      int windowId = mc.field_71439_g.field_71069_bz.field_75152_c;
      short transactionID = mc.field_71439_g.field_71069_bz.func_75136_a(mc.field_71439_g.field_71071_by);
      C0EPacketClickWindow click = new C0EPacketClickWindow(windowId, slotId, 0, 1, mc.field_71439_g.field_71071_by.func_70301_a(5), transactionID);
      mc.field_71439_g.field_71174_a.func_147297_a((Packet)click);
      mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C0DPacketCloseWindow(windowId));
    } catch (Exception exception) {}
  }
  
  private static void doSwap(int slot) {
    for (KeyBinding bind : mc.field_71474_y.field_151456_ac)
      KeyBinding.func_74510_a(bind.func_151463_i(), false); 
    for (int i = 0; i < mc.field_71474_y.field_151456_ac.length; i++) {
      if (i == slot - 1) {
        KeyBinding.func_74510_a(mc.field_71474_y.field_151456_ac[i].func_151463_i(), true);
        ((IKeyBinding)mc.field_71474_y.field_151456_ac[i]).setPressTime(1);
        break;
      } 
    } 
  }
  
  private boolean isToggled(int slot) {
    ItemStack stack = mc.field_71439_g.field_71071_by.func_70301_a(slot - 1);
    if (stack == null)
      return false; 
    if (stack.func_77973_b() == Items.field_151100_aR)
      return false; 
    if (check.getValue() >= 100.0D) {
      if (slot >= 2 && slot <= 4 && stack.func_77952_i() > 0 && stack.field_77994_a == 1)
        return false; 
    } else if (check.getValue() > 0.0D && 
      slot >= 2 && slot <= 4 && 1.0D - stack
      .func_77952_i() / stack.func_77958_k() < check
      .getValue() / 100.0D && stack.field_77994_a == 1) {
      return false;
    } 
    switch (slot) {
      case 2:
        return s2.getValue();
      case 3:
        return s3.getValue();
      case 4:
        return s4.getValue();
    } 
    return false;
  }
}
