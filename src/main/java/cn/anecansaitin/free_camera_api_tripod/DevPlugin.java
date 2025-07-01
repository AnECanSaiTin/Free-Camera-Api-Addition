package cn.anecansaitin.free_camera_api_tripod;

import cn.anecansaitin.freecameraapi.api.*;
import cn.anecansaitin.freecameraapi.api.extension.ControlScheme;
import net.minecraft.client.Minecraft;

@CameraPlugin(value = "dev", priority = ModifierPriority.LOWEST)
public class DevPlugin implements ICameraPlugin {
    private ICameraModifier modifier;

    @Override
    public void initialize(ICameraModifier modifier) {
        this.modifier = modifier;
        modifier.disable()
                .enablePos()
                .enableRotation()
                .asExtension()
                .setControlScheme(ControlScheme.PLAYER_RELATIVE(10));
    }

    @Override
    public void update() {
//        Minecraft.getInstance().mouseHandler.releaseMouse();
        modifier
                .disable()
//                .enable()
                .setPos(0,3,-2)
                .setRotationYXZ(45,0,0);
    }
}