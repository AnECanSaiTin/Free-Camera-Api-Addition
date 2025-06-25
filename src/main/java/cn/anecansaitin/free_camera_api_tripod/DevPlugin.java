package cn.anecansaitin.free_camera_api_tripod;

import cn.anecansaitin.freecameraapi.api.CameraPlugin;
import cn.anecansaitin.freecameraapi.api.ICameraPlugin;
import cn.anecansaitin.freecameraapi.api.ICameraModifier;
import cn.anecansaitin.freecameraapi.api.ModifierPriority;

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
                .enableChunkLoader();
    }

    @Override
    public void update() {
        modifier
                .disable()
                .setPos(-100, 80, -314)
                .setRotationYXZ(90, 180, 0);
    }
}