package cn.anecansaitin.free_camera_api_tripod.core.control_scheme;

import cn.anecansaitin.free_camera_api_tripod.FreeCameraApiTripod;
import cn.anecansaitin.freecameraapi.api.ControlScheme;
import cn.anecansaitin.freecameraapi.core.ModifierManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.joml.Vector2i;

import static cn.anecansaitin.freecameraapi.core.ModifierStates.ENABLE;
import static cn.anecansaitin.freecameraapi.core.ModifierStates.GLOBAL_MODE;

@EventBusSubscriber(modid = FreeCameraApiTripod.MODID)
public class OnMovementInputUpdate {
    private static final Vector2i vec2 = new Vector2i();
    private static final Vec2 vec = new Vec2(0, 1);
    private static final int[][] angles = new int[][]{{135, 90, 45}, {180, 0, 0}, {-135, -90, -45}};

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        ModifierManager manager = ModifierManager.INSTANCE;
        ControlScheme controlScheme = manager.controlScheme();

        if (controlScheme == ControlScheme.CAMERA_RELATIVE && manager.isStateEnabledAnd(ENABLE | GLOBAL_MODE)) {
            Input input = event.getInput().keyPresses;
            float yRot = manager.rot().y;
            vec2.set(input.left() == input.right() ? 0 : (input.left() ? 1 : -1), input.forward() == input.backward() ? 0 : (input.forward() ? 1 : -1));

            if (vec2.lengthSquared() <= 0) {
                return;
            }

            int angle = angles[vec2.x + 1][vec2.y + 1];

            Minecraft.getInstance().player.setYRot(yRot + angle);
            event.getInput().keyPresses = new Input(true, false, false, false, input.jump(), input.shift(), input.sprint());
            event.getInput().moveVector = vec;
        }
    }
}
