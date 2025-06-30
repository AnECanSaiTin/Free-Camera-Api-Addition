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
                .enableGlobalMode()
                .asExtension()
                .setControlScheme(ControlScheme.CAMERA_RELATIVE);
    }

    @Override
    public void update() {
        modifier
                .enable()
                .setToVanilla()
                .rotateYXZ(0, 20, 0);
    }
}