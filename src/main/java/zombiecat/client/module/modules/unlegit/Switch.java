package zombiecat.client.module.modules.unlegit;

import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;

public class Switch extends Module {
   public static BooleanSetting s4;
   public Switch() {
      super("Switch", ModuleCategory.unlegit);
      this.registerSetting(s4 = new BooleanSetting("QS3", true));
   }

   @Override
   public void onEnable() {
      if (QuickSwitch.INSTANCE.isOn()) {
         QuickSwitch.INSTANCE.disable();
         QuickSwitch2.INSTANCE.enable();
         if (QuickSwitch3.INSTANCE.isOn()) {
            QuickSwitch3.INSTANCE.disable();
         }
         this.disable();
         return;
      }
      if (QuickSwitch2.INSTANCE.isOn()) {
         QuickSwitch2.INSTANCE.disable();
         if (s4.getValue()) {
            QuickSwitch3.INSTANCE.enable();
         } else {
            QuickSwitch.INSTANCE.enable();
         }
         this.disable();
         return;
      }
      if (QuickSwitch3.INSTANCE.isOn()) {
         QuickSwitch3.INSTANCE.disable();
         QuickSwitch.INSTANCE.enable();
         this.disable();
         return;
      }
      QuickSwitch.INSTANCE.enable();
      disable();
   }
}
