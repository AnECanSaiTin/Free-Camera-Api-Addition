package cn.anecansaitin.freecameraapiaddition;

import cn.anecansaitin.freecameraapi.api.CameraPlugin;
import cn.anecansaitin.freecameraapi.api.ICameraPlugin;
import cn.anecansaitin.freecameraapi.api.ICameraModifier;
import cn.anecansaitin.freecameraapi.api.ModifierPriority;

@CameraPlugin(value = "example", priority = ModifierPriority.LOWEST)
public class ExamplePlugin implements ICameraPlugin {
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
                .setPos(-10, 64, -314)
                .setRotationYXZ(90, 180, 0);
    }
}