package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.Minecraft;
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

    private final Minecraft mc = Minecraft.getMinecraft();

    public QuickSwitch3() {
        super("QuickSwitch3", Module.ModuleCategory.unlegit);
        registerSetting(onlyFire = new BooleanSetting("OnlyFire", true));
        registerSetting(delay = new SliderSetting("Delay", 50.0D, 0.0D, 80.0D, 1.0D));
        registerSetting(anySlot = new BooleanSetting("AnySlot", true));
        registerSetting(s2 = new BooleanSetting("Slot2", true));
        registerSetting(s3 = new BooleanSetting("Slot3", true));
        registerSetting(s4 = new BooleanSetting("Slot4", true));
        registerSetting(check = new SliderSetting("Check", 0.0D, 0.0D, 100.0D, 1.0D));
        registerSetting(hudDisplay = new BooleanSetting("HudDisplay", true));
        registerSetting(blockRightClick = new BooleanSetting("BlockRightClick", false));
        registerSetting(clicksPerSecond = new SliderSetting("ClicksPerSecond", 40.0D, 10.0D, 50.0D, 1.0D));
        INSTANCE = this;
    }

    @Override
    public String getHudLabel() {
        if (!hudDisplay.getValue())
            return getName();
        StringBuilder slots = new StringBuilder();
        if (s2.getValue()) slots.append('2');
        if (s3.getValue()) slots.append('3');
        if (s4.getValue()) slots.append('4');
        String slotStr = (slots.length() == 0) ? "-" : slots.toString();
        long ms = Math.round(delay.getValue());
        long cps = Math.round(clicksPerSecond.getValue());
        return getName() + " " + slotStr + " " + ms + "ms " + cps + "Hz";
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent event) {
        if (isOn() && blockRightClick.getValue() &&
                (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR ||
                        event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void re(RenderWorldLastEvent e) {
        if (!isOn() || mc.thePlayer == null || mc.theWorld == null)
            return;

        if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            this.hold = false;
        } else {
            boolean isHold = false;
            if (Keyboard.isKeyDown(Keyboard.KEY_N)) {
                isHold = true;
                if (!this.hold) delay.setValue(delay.getValue() + 1.0D);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
                isHold = true;
                if (!this.hold) delay.setValue(delay.getValue() - 1.0D);
            }
            KeyBinding[] hotbarKeys = mc.gameSettings.keyBindsHotbar;
            for (int i = 0; i < hotbarKeys.length; i++) {
                if (Keyboard.isKeyDown(hotbarKeys[i].getKeyCode())) {
                    isHold = true;
                    if (!this.hold) {
                        switch (i) {
                            case 1: s2.setEnabled(!s2.getValue()); break;
                            case 2: s3.setEnabled(!s3.getValue()); break;
                            case 3: s4.setEnabled(!s4.getValue()); break;
                        }
                        break;
                    }
                }
            }
            this.hold = isHold;
        }

        if (onlyFire.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown())
            return;

        if (this.swapTimer.passed(delay.getValue())) {
            this.swapTimer.reset();
            performSwapCycle();
        }

        double interval = 1000.0D / clicksPerSecond.getValue();
        if (this.clickTimer.passed(interval)) {
            this.clickTimer.reset();
            int currentSlot = mc.thePlayer.inventory.currentItem + 1;
            if (this.allowSlot6Click && currentSlot != 6)
                doSlot6Click();
        }
    }

    private void performSwapCycle() {
        int cSlot = mc.thePlayer.inventory.currentItem + 1;
        if (cSlot == 2 || cSlot == 3 || cSlot == 4) {
            this.allowSlot6Click = true;
        } else if (cSlot == 6) {
            this.allowSlot6Click = false;
        }

        if (cSlot == 2 && s2.getValue()) {
            int[] targets = {3, 4, 6, 7, 8, 9};
            for (int slot : targets) if (isToggled(slot)) { doSwap(slot); return; }
        }
        if (cSlot == 3 && s3.getValue()) {
            int[] targets = {4, 6, 7, 8, 9, 2};
            for (int slot : targets) if (isToggled(slot)) { doSwap(slot); return; }
        }
        if (cSlot == 4 && s4.getValue()) {
            int[] targets = {6, 7, 8, 9, 2, 3};
            for (int slot : targets) if (isToggled(slot)) { doSwap(slot); return; }
        }
        if (anySlot.getValue() && cSlot != 1) {
            int[] targets = {2, 3, 4, 6, 7, 8, 9};
            for (int slot : targets) if (isToggled(slot)) { doSwap(slot); return; }
        }
    }

    private void doSlot6Click() {
        try {
            int slotId = 41;
            int windowId = mc.thePlayer.openContainer.windowId;
            short transactionID = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory);
            C0EPacketClickWindow click = new C0EPacketClickWindow(
                    windowId, slotId, 0, 1, mc.thePlayer.inventory.getStackInSlot(5), transactionID);
            mc.thePlayer.sendQueue.addToSendQueue(click);
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(windowId));
        } catch (Exception ignored) {}
    }

    private static void doSwap(int slot) {
        Minecraft mc = Minecraft.getMinecraft();
        for (KeyBinding bind : mc.gameSettings.keyBindsHotbar)
            KeyBinding.setKeyBindState(bind.getKeyCode(), false);
        for (int i = 0; i < mc.gameSettings.keyBindsHotbar.length; i++) {
            if (i == slot - 1) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindsHotbar[i].getKeyCode(), true);
                ((IKeyBinding) mc.gameSettings.keyBindsHotbar[i]).setPressTime(1);
                break;
            }
        }
    }

    private boolean isToggled(int slot) {
        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot - 1);
        if (stack == null) return false;
        if (stack.getItem() == Items.fishing_rod) return false;

        if (check.getValue() >= 100.0D) {
            if (slot >= 2 && slot <= 4 && stack.getItemDamage() > 0 && stack.stackSize == 1)
                return false;
        } else if (check.getValue() > 0.0D &&
                slot >= 2 && slot <= 4 &&
                1.0D - ((double) stack.getItemDamage() / stack.getMaxDamage()) < check.getValue() / 100.0D &&
                stack.stackSize == 1) {
            return false;
        }

        switch (slot) {
            case 2: return s2.getValue();
            case 3: return s3.getValue();
            case 4: return s4.getValue();
        }
        return false;
    }
}
