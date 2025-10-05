package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.WatchTimer;

public class Slot6Clicker extends Module {

    public static SliderSetting clicksPerSecond;
    
    private final WatchTimer clickTimer = new WatchTimer();
    private final Minecraft mc = Minecraft.getMinecraft();

    public Slot6Clicker() {
        super("Slot6Clicker", Module.ModuleCategory.unlegit);
        registerSetting(clicksPerSecond = new SliderSetting("ClicksPerSecond", 40.0D, 10.0D, 50.0D, 1.0D));
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (!isOn() || mc.thePlayer == null || mc.theWorld == null)
            return;

        double interval = 1000.0D / clicksPerSecond.getValue();
        if (this.clickTimer.passed(interval)) {
            this.clickTimer.reset();
            doSlot6Click();
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
}
