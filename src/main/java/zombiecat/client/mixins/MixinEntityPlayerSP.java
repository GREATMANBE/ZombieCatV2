package zombiecat.client.mixins;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombiecat.client.module.modules.bannable.Phase;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Inject(
            method = "onLivingUpdate",
            at = {@At("HEAD")}
    )
    public void onHead(CallbackInfo ci) {
        Phase.onUpdate();
    }
}
