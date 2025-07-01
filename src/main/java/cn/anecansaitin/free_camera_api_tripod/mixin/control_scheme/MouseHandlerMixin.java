package cn.anecansaitin.free_camera_api_tripod.mixin.control_scheme;

import cn.anecansaitin.freecameraapi.api.extension.ControlScheme;
import cn.anecansaitin.freecameraapi.core.ModifierManager;
import cn.anecansaitin.freecameraapi.core.ModifierStates;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), cancellable = true)
    public void freeCameraAPI$turnPlayer(double movementTime, CallbackInfo ci) {
        ModifierManager manager = ModifierManager.INSTANCE;

        if (!manager.isStateEnabledOr(ModifierStates.ENABLE) || Minecraft.getInstance().player.isPassenger()) {
            return;
        }

        switch (manager.controlScheme()) {
            case ControlScheme.PLAYER_RELATIVE playerRelative -> ci.cancel();
            default -> {}
        }
    }

//    @Redirect(method = "grabMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"))
//    public void freeCameraAPI$grabMouse(long handle, int func, double x, double y) {
//        ModifierManager manager = ModifierManager.INSTANCE;
//
//        if (!manager.isStateEnabledOr(ModifierStates.ENABLE)) {
//            InputConstants.grabOrReleaseMouse(minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
//            return;
//        }
//
//        InputConstants.grabOrReleaseMouse(minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
//
//    }
}
