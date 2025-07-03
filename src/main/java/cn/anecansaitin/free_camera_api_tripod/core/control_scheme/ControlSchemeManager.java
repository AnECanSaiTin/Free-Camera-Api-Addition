package cn.anecansaitin.free_camera_api_tripod.core.control_scheme;

import cn.anecansaitin.free_camera_api_tripod.FreeCameraApiTripod;
import cn.anecansaitin.freecameraapi.api.extension.ControlScheme;
import cn.anecansaitin.freecameraapi.core.ModifierManager;
import cn.anecansaitin.freecameraapi.core.ModifierStates;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static cn.anecansaitin.freecameraapi.api.extension.ControlScheme.*;
import static cn.anecansaitin.freecameraapi.core.ModifierStates.ENABLE;

@EventBusSubscriber(modid = FreeCameraApiTripod.MODID, value = Dist.CLIENT)
public class ControlSchemeManager {
    //region 控制模式转换
    private static final Vector2i VEC2 = new Vector2i();
    private static final Vec2 FORWARD = new Vec2(0, 1);
    private static final Vec2 BACKWARD = new Vec2(0, -1);
    private static final Vec2 STAY = new Vec2(0, 0);
    private static final int[][] ANGLES = new int[][]{{135, 90, 45}, {180, 0, 0}, {-135, -90, -45}};

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        ModifierManager manager = ModifierManager.INSTANCE;

        if (!manager.isStateEnabledAnd(ENABLE) || Minecraft.getInstance().player.isPassenger()) {
            return;
        }

        ControlScheme controlScheme = manager.controlScheme();
        ClientInput input = event.getInput();

        switch (controlScheme) {
            case CAMERA_RELATIVE cameraRelative -> cameraRelative(input, manager);
            case PLAYER_RELATIVE playerRelative -> playerRelative(input, playerRelative.angle());
            case PLAYER_RELATIVE_STRAFE playerRelativeStrafe -> mouseMove();
            default -> {}
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
    //endregion

    // region 鼠标控制设置
    private static final Vector3f VEC3 = new Vector3f();
    private static final Vector3f Y = new Vector3f(0, 1, 0);

    @SubscribeEvent
    public static void mouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;

        if (mc.screen != null || !manager.isStateEnabledOr(ModifierStates.ENABLE)) {
            return;
        }

//        Vec3 vec3 = pickBlock();
//
//        if (vec3 != null) {
//
//        }

        mc.mouseHandler.releaseMouse();
        event.setCanceled(true);
    }

    public static void mouseMove() {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;

        if (mc.screen != null || !manager.isStateEnabledOr(ModifierStates.ENABLE)) {
            return;
        }

        LocalPlayer player = mc.player;
        Vector3f mousePos = getMousePosInWorld();
        Vector3f playerPos = player.position().toVector3f();
        player.displayClientMessage(Component.literal("Mouse Pos: " + mousePos.x + ", " + mousePos.y + ", " + mousePos.z), true);
        Vector3f direction = mousePos.sub(playerPos).normalize();
//        Quaternionf rotation = new Quaternionf().lookAlong(direction, Mth.Y_AXIS);
        float yaw = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG - 90);
        player.setYRot(yaw);
    }

    private static Vec3 pickBlock() {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;
        Vector3f worldDir = getMouseRay();
        Vector3f cameraPos = manager.pos();
        Vec3 start = new Vec3(cameraPos.x, cameraPos.y, cameraPos.z);
        Vec3 end = start.add(worldDir.x * 10, worldDir.y * 10, worldDir.z * 10);
        BlockHitResult clip = mc.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, mc.player));

        if (clip.getType() != HitResult.Type.MISS) {
            return clip.getLocation();
        }

        return null;
    }

    private static Vector3f getMousePosInWorld() {
        Vector3f mouseRay = getMouseRay();
        Minecraft mc = Minecraft.getInstance();

        if (Math.abs(mouseRay.y) < 1e-6f) {
            return mc.player.position().toVector3f();
        }

        Vector3f cameraPos = ModifierManager.INSTANCE.pos();
        float playerY = (float) mc.player.getY();
        float length = (playerY - cameraPos.y) / mouseRay.y;
        return mouseRay.set(cameraPos.x + mouseRay.x * length, playerY, cameraPos.z + mouseRay.z * length);
    }

    private static @NotNull Vector3f getMouseRay() {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;
        Window window = mc.getWindow();
        int screenWidth = window.getScreenWidth();
        int screenHeight = window.getScreenHeight();
        float ndcX = (float) -((2 * mc.mouseHandler.xpos() / screenWidth) - 1);
        float ndcY = (float) (1 - (2 * mc.mouseHandler.ypos() / screenHeight));
        float aspect = (float) screenWidth / screenHeight;
        float fovRad = (float) Math.toRadians(manager.fov());
        float tanHalfFovY = (float) Math.tan(fovRad / 2.0f);

        Vector3f cameraDir = new Vector3f(
                ndcX * tanHalfFovY * aspect,
                ndcY * tanHalfFovY,
                1.0f
        );

        Quaternionf cameraRotation = new Quaternionf();
        Vector3f rot = manager.rot();
        cameraRotation.rotateYXZ(-Mth.DEG_TO_RAD * rot.y, Mth.DEG_TO_RAD * rot.x, Mth.DEG_TO_RAD * rot.z);

        // 变换到世界空间方向
        Vector3f worldDir = new Vector3f(cameraDir);
        cameraRotation.transform(worldDir);
        worldDir.normalize();
        return worldDir;
    }
    // endregion
}
