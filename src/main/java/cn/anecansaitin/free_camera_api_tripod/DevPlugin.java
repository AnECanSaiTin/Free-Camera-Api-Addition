package cn.anecansaitin.free_camera_api_tripod;

import cn.anecansaitin.freecameraapi.api.*;

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
                .setControlScheme(ControlScheme.PLAYER_RELATIVE);
    }

    @Override
    public void update() {
        modifier
                .enable()
                .setPos(0,3,-2)
                .setRotationYXZ(45,0,0);
    }
}