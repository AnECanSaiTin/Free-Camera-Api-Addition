package cn.anecansaitin.free_camera_api_tripod.core.control_scheme;

import cn.anecansaitin.free_camera_api_tripod.FreeCameraApiTripod;
import cn.anecansaitin.freecameraapi.api.extension.ControlScheme;
import cn.anecansaitin.freecameraapi.core.ModifierManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.joml.Vector2i;

import static cn.anecansaitin.freecameraapi.api.extension.ControlScheme.*;
import static cn.anecansaitin.freecameraapi.core.ModifierStates.ENABLE;

@EventBusSubscriber(modid = FreeCameraApiTripod.MODID)
public class ControlSchemeManager {
    private static final Vector2i VEC2 = new Vector2i();
    private static final Vec2 FORWARD = new Vec2(0, 1);
    private static final Vec2 BACKWARD = new Vec2(0, -1);
    private static final Vec2 STAY = new Vec2(0, 0);
    private static final int[][] ANGLES = new int[][]{{135, 90, 45}, {180, 0, 0}, {-135, -90, -45}};

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        ModifierManager manager = ModifierManager.INSTANCE;

        if (!manager.isStateEnabledAnd(ENABLE)) {
            return;
        }

        ControlScheme controlScheme = manager.controlScheme();
        ClientInput input = event.getInput();

        switch (controlScheme) {
            case CAMERA_RELATIVE cameraRelative -> cameraRelative(input, manager);
            case PLAYER_RELATIVE playerRelative -> playerRelative(input, playerRelative.angle());
            case VANILLA vanilla -> {
            }
        }
    }

    private static void cameraRelative(ClientInput input, ModifierManager manager) {
        Input keyPresses = input.keyPresses;
        float yRot = manager.rot().y;
        calculateImpulse(keyPresses);

        if (VEC2.lengthSquared() <= 0) {
            return;
        }

        int angle = ANGLES[VEC2.x + 1][VEC2.y + 1];

        Minecraft.getInstance().player.setYRot(yRot + angle);
        input.keyPresses = new Input(true, false, false, false, keyPresses.jump(), keyPresses.shift(), keyPresses.sprint());
        input.moveVector = FORWARD;
    }

    private static void playerRelative(ClientInput input, int angle) {
        Input keyPresses = input.keyPresses;
        calculateImpulse(keyPresses);
        LocalPlayer player = Minecraft.getInstance().player;
        player.setYRot(VEC2.x * -angle + player.getYRot(Minecraft.getInstance().gameRenderer.getMainCamera().getPartialTickTime()));

        switch (VEC2.y) {
            case -1 -> input.moveVector = BACKWARD;
            case 0 -> input.moveVector = STAY;
            case 1 -> input.moveVector = FORWARD;
        }
    }

    private static void calculateImpulse(Input keyPress) {
        VEC2.set(keyPress.left() == keyPress.right() ? 0 : (keyPress.left() ? 1 : -1), keyPress.forward() == keyPress.backward() ? 0 : (keyPress.forward() ? 1 : -1));
    }
}
