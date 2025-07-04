package cn.anecansaitin.free_camera_api_tripod;

import cn.anecansaitin.freecameraapi.api.*;
import cn.anecansaitin.freecameraapi.api.extension.ControlScheme;

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
                .setControlScheme(ControlScheme.PLAYER_RELATIVE_STRAFE);
    }

    @Override
    public void update() {
        modifier
//                .disable()
                .enable()
//                .setPos(4,5,-4)
//                .setRotationYXZ(45,45,0)
                .setPos(0,5,1)
                .setRotationYXZ(90,0,0)
                ;
    }
}