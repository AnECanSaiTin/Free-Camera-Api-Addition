package cn.anecansaitin.free_camera_api_tripod.core.control_scheme;

import cn.anecansaitin.free_camera_api_tripod.FreeCameraApiTripod;
import cn.anecansaitin.freecameraapi.api.extension.ControlScheme;
import cn.anecansaitin.freecameraapi.core.ModifierManager;
import cn.anecansaitin.freecameraapi.core.ModifierStates;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Optional;

import static cn.anecansaitin.freecameraapi.api.extension.ControlScheme.*;
import static cn.anecansaitin.freecameraapi.core.ModifierStates.ENABLE;

@EventBusSubscriber(modid = FreeCameraApiTripod.MODID, value = Dist.CLIENT)
public class ControlSchemeManager {
    // region 控制模式转换
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
            case CAMERA_RELATIVE_STRAFE cameraRelativeStrafe -> cameraRelativeStrafe(input, manager);
            case PLAYER_RELATIVE playerRelative -> playerRelative(input, playerRelative.angle());
            case PLAYER_RELATIVE_STRAFE playerRelativeStrafe -> mouseMove();
            default -> {
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

    private static void cameraRelativeStrafe(ClientInput input, ModifierManager manager) {
        mouseMove();

        Input keyPresses = input.keyPresses;
        float yRot = manager.rot().y;
        calculateImpulse(keyPresses);

        if (VEC2.lengthSquared() <= 0) {
            return;
        }

        int angle = ANGLES[VEC2.x + 1][VEC2.y + 1];
        yRot += angle;
        Minecraft mc = Minecraft.getInstance();
        float playerYRot = mc.player.getYRot();
        float yRotDelta = (playerYRot - yRot) * Mth.DEG_TO_RAD;
        input.moveVector = new Vec2(Mth.sin(yRotDelta), Mth.cos(yRotDelta));
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
    // endregion

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

        switch (manager.controlScheme()) {
            case CAMERA_RELATIVE_STRAFE cameraRelativeStrafe -> mc.mouseHandler.releaseMouse();
            case PLAYER_RELATIVE_STRAFE playerRelativeStrafe -> mc.mouseHandler.releaseMouse();
            case null, default -> {}
        }
    }

    public static void mouseMove() {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;

        if (mc.screen != null || !manager.isStateEnabledOr(ModifierStates.ENABLE)) {
            return;
        }

        Vector3f blockPos = pick();

        if (blockPos != null) {
//            mc.player.displayClientMessage(Component.literal("Mouse Pos: " + blockPos.x + ", " + blockPos.y + ", " + blockPos.z), true);
            Vector3f playerEyePos = mc.player.getEyePosition(mc.gameRenderer.getMainCamera().getPartialTickTime()).toVector3f();
            Vector3f direction = blockPos.sub(playerEyePos).normalize();

            float yaw = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG - 90);

            float horizontalLength = Mth.sqrt(direction.x * direction.x + direction.z * direction.z);
            float pitch = (float) -(Mth.atan2(direction.y, horizontalLength) * Mth.RAD_TO_DEG);

            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        }
    }

    private static Vector3f pick() {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;
        Vector3f start = manager.pos();
        Vector3f playerPos = mc.player.position().toVector3f();
        float length = playerPos.distance(start) + 50;
        Vector3f mouseRay = getMouseRay();
        Vector3f end = mouseRay.mul(length).add(start);

        //region 实体
        AABB aabb = mc.player.getBoundingBox().inflate(mc.player.entityInteractionRange() + 1);

        for (Entity entity : mc.level.getEntities(mc.player, aabb, EntitySelector.CAN_BE_PICKED)) {
            AABB aabb1 = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> optional = aabb1.clip(new Vec3(start), new Vec3(end));

            if (optional.isEmpty()) {
                continue;
            }

            return optional.get().toVector3f();
        }
        //endregion

        //region 方块
        BlockHitResult blockHitResult = mc.level.clip(new ClipContext(new Vec3(start), new Vec3(end), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, mc.player));

        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            return blockHitResult.getLocation().toVector3f();
        }
        //endregion

        return null;
    }

    private static Vector3f getMousePosInWorld() {
        Vector3f mouseRay = getMouseRay();
        Minecraft mc = Minecraft.getInstance();

        if (Math.abs(mouseRay.y) < 1e-6f) {
            return mc.player.position().toVector3f().add(mouseRay.mul(100));
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
        float ndcX = (float) ((2 * mc.mouseHandler.xpos() / screenWidth) - 1);
        float ndcY = (float) (1 - (2 * mc.mouseHandler.ypos() / screenHeight));
        float aspect = (float) screenWidth / screenHeight;
        float fovRad = manager.fov() * Mth.DEG_TO_RAD;
        float tanHalfFovY = (float) Math.tan(fovRad / 2.0f);

        Vector3f ray = new Vector3f(
                -ndcX * tanHalfFovY * aspect, // 因为右手坐标系，x轴左正，右负，因此需要乘以-1
                ndcY * tanHalfFovY,
                1.0f
        );

        // 变换到世界空间方向
        Vector3f cameraRot = manager.rot();
        ray.rotateZ(cameraRot.z * Mth.DEG_TO_RAD)
                .rotateX(cameraRot.x * Mth.DEG_TO_RAD)
                .rotateY(-cameraRot.y * Mth.DEG_TO_RAD);

        ray.normalize();
        return ray;
    }
    // endregion
}
