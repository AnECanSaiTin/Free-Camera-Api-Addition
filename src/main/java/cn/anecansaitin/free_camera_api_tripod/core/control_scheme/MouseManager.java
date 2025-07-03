package cn.anecansaitin.free_camera_api_tripod.core.control_scheme;

import cn.anecansaitin.free_camera_api_tripod.FreeCameraApiTripod;
import cn.anecansaitin.freecameraapi.core.ModifierManager;
import cn.anecansaitin.freecameraapi.core.ModifierStates;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;

@EventBusSubscriber(modid = FreeCameraApiTripod.MODID, value = Dist.CLIENT)
public class MouseManager {
    private static final Vector3f VEC3 = new Vector3f();
    private static final Vector3f Y = new Vector3f(0, 1, 0);

    @SubscribeEvent
    public static void mouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;

        if (mc.screen != null || !manager.isStateEnabledOr(ModifierStates.ENABLE)) {
            return;
        }

        Vec3 vec3 = pickBlock();

        if (vec3 != null) {

        }

        mc.mouseHandler.releaseMouse();
        event.setCanceled(true);
    }

    public static void mouseMove(double xPos, double yPos) {
        Minecraft mc = Minecraft.getInstance();
        ModifierManager manager = ModifierManager.INSTANCE;

        if (mc.screen != null || !manager.isStateEnabledOr(ModifierStates.ENABLE)) {
            return;
        }

        updateViewMatrix();
        Vector2f vector2f = worldToScreen(-15.5f, 56, -269.5f);
        if (vector2f != null)
            mc.player.displayClientMessage(Component.literal("pos: " + vector2f.x + ", " + vector2f.y), true);
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
        Vector3f pos = ModifierManager.INSTANCE.pos();
        float length = (float) ((pos.y - Minecraft.getInstance().player.getY()) / mouseRay.angleCos(Y));
        return mouseRay.mul(length).add(pos);
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

    private static final Matrix4f viewMatrix = new Matrix4f();
    private static Matrix4f PROJECTION_MATRIX = new Matrix4f();
    private static final Quaternionf rotation = new Quaternionf();
    private static final Vector3f rot = new Vector3f();
    private static final Vector3f forward = new Vector3f();
    private static final Vector3f up = new Vector3f();
    private static final Vector3f target = new Vector3f();

    /**
     * 更新视图矩阵（基于相机位置和YXZ欧拉角旋转）
     */
    private static void updateViewMatrix() {
        ModifierManager manager = ModifierManager.INSTANCE;
        manager.rot().mul(Mth.DEG_TO_RAD, rot);

        // 构建旋转四元数（YXZ顺序）
        rotation.identity()
                .rotateY(rot.y)
                .rotateX(rot.x)
                .rotateZ(rot.z);

        // 计算旋转后的前向向量和上向量（初始前向为(0,0,-1)，上向量为(0,1,0)）
        forward.set(0, 0, -1).rotate(rotation);
        up.set(0, 1, 0).rotate(rotation);

        // 目标点 = 相机位置 + 前向向量
        target.set(manager.pos()).add(forward);

        // 构建视图矩阵（lookAt方法自动处理正交化）
        viewMatrix.identity().lookAt(manager.pos(), target, up);
    }

    /**
     * 更新投影矩阵（透视投影）
     */
    private static void updateProjectionMatrix(Matrix4f projectionMatrix) {
        PROJECTION_MATRIX = projectionMatrix;
    }

    /**
     * 将世界坐标转换为屏幕坐标
     *
     * @return 屏幕坐标（若对象在相机后方，返回null）
     */
    public static Vector2f worldToScreen(float x, float y, float z) {
        // 1. 世界坐标 → 视图坐标（相机空间）
        Vector4f viewPos = new Vector4f(x, y, z, 1.0f);
        viewPos.mul(viewMatrix);

        // 2. 视图坐标 → 投影坐标（裁剪空间）
        Vector4f clipPos = new Vector4f(viewPos);
        clipPos.mul(PROJECTION_MATRIX);

        // 3. 透视除法（归一化到NDC，范围[-1,1]）
        if (clipPos.w <= 0) return null; // 物体在相机后方或不可见
        clipPos.x /= clipPos.w;
        clipPos.y /= clipPos.w;
        clipPos.z /= clipPos.w;
//
//        // 4. NDC → 屏幕坐标（翻转Y轴以适配屏幕坐标系）
//        // 修正：确保NDC的Y=1对应屏幕顶部，Y=-1对应底部
//        int screenWidth = Minecraft.getInstance().getWindow().getScreenWidth();
//        int screenHeight = Minecraft.getInstance().getWindow().getScreenHeight();
//        float screenX = (clipPos.x + 1.0f) * 0.5f * screenWidth;
//        float screenY = (1.0f - clipPos.y) * 0.5f * screenHeight; // 保留原逻辑，根据实际场景验证
//
//        // 确保坐标在屏幕范围内（可选）
//        screenX = Math.max(0, Math.min(screenX, screenWidth));
//        screenY = Math.max(0, Math.min(screenY, screenHeight));

        return new Vector2f(clipPos.x, clipPos.y);
    }
}
