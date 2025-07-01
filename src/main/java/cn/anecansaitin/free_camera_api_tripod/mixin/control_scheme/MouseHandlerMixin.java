package cn.anecansaitin.free_camera_api_tripod.mixin.control_scheme;

import cn.anecansaitin.freecameraapi.api.extension.ControlScheme;
import cn.anecansaitin.freecameraapi.core.ModifierManager;
import cn.anecansaitin.freecameraapi.core.ModifierStates;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), cancellable = true)
    public void freeCameraAPI$turnPlayer(double movementTime, CallbackInfo ci) {
        ModifierManager manager = ModifierManager.INSTANCE;

        if (!manager.isStateEnabledOr(ModifierStates.ENABLE)) {
            return;
        }

        switch (manager.controlScheme()) {
            case ControlScheme.PLAYER_RELATIVE playerRelative -> ci.cancel();
            default -> {}
        }
    }
}
