package com.herobrot.scalingdifficulty.client;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.network.payload.ZoneSyncPayload;
import com.herobrot.scalingdifficulty.zone.ClientZoneTracker;
import com.herobrot.scalingdifficulty.zone.DifficultyZone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;

public class ZoneBorderRenderer {

    private static final float LINE_WIDTH = 2.0F;
    private static final int SPHERE_SEGMENTS = 64;
    private static final int MERIDIAN_COUNT = 32;
    private static final int ALPHA = 160;

    @SuppressWarnings("resource")
    public static void render(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        String dimension = client.player.level().dimension().location().toString();

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;
        Frustum frustum = event.getFrustum();

        PoseStack poseStack = event.getPoseStack();
        RenderType renderType = RenderType.debugLineStrip(LINE_WIDTH);
        MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();
        double maxDistanceSqr = sq(ScalingDifficulty.CONFIG.renderDistance * 16);

        for (ZoneSyncPayload.ZoneEntry zone : ClientZoneTracker.getZones()) {
            if (!zone.dimension().equals(dimension)) continue;
            AABB bounds = zoneBounds(zone);
            if (!withinRenderDistance(bounds, maxDistanceSqr, camX, camY, camZ)) continue;
            if (!frustum.isVisible(bounds)) continue;
            int color = zoneColor(zone);
            if (zone.shape() == DifficultyZone.Shape.BOX)
                renderBox(consumer, matrix, zone, color, camX, camY, camZ);
            else renderSphere(consumer, matrix, zone, color, camX, camY, camZ);
        }
        bufferSource.endBatch(renderType);
    }

    private static double sq(double v) { return v * v; }

    private static boolean withinRenderDistance(AABB bounds, double maxDistanceSqr, double camX, double camY, double camZ) {
        double dx = Math.max(Math.max(bounds.minX - camX, 0.0), camX - bounds.maxX);
        double dy = Math.max(Math.max(bounds.minY - camY, 0.0), camY - bounds.maxY);
        double dz = Math.max(Math.max(bounds.minZ - camZ, 0.0), camZ - bounds.maxZ);
        return dx * dx + dy * dy + dz * dz <= maxDistanceSqr;
    }

    private static AABB zoneBounds(ZoneSyncPayload.ZoneEntry zone) {
        if (zone.shape() == DifficultyZone.Shape.BOX)
            return new AABB(zone.minX(), zone.minY(), zone.minZ(), zone.maxX() + 1, zone.maxY() + 1, zone.maxZ() + 1);
        double cx = zone.minX() + 0.5;
        double cy = zone.minY() + 0.5;
        double cz = zone.minZ() + 0.5;
        return new AABB(cx - zone.radius(), cy - zone.radius(), cz - zone.radius(),
                cx + zone.radius(), cy + zone.radius(), cz + zone.radius());
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, ZoneSyncPayload.ZoneEntry zone, int color, double camX, double camY, double camZ) {
        float x1 = (float) (zone.minX() - camX);
        float x2 = (float) (zone.maxX() + 1 - camX);
        float y1 = (float) (zone.minY() - camY);
        float y2 = (float) (zone.maxY() + 1 - camY);
        float z1 = (float) (zone.minZ() - camZ);
        float z2 = (float) (zone.maxZ() + 1 - camZ);

        for (int i = 0; i < 4; i++) {
            float x = (i & 1) == 0 ? x1 : x2;
            float z = (i & 2) == 0 ? z1 : z2;
            line(consumer, matrix, color, x, y1, z, x, y2, z);
        }
        rectangle(consumer, matrix, color, x1, x2, z1, z2, y1);
        rectangle(consumer, matrix, color, x1, x2, z1, z2, y2);
    }

    private static void renderSphere(VertexConsumer consumer, Matrix4f matrix, ZoneSyncPayload.ZoneEntry zone,
                                     int color, double camX, double camY, double camZ) {
        double cx = zone.minX() + 0.5;
        double cy = zone.minY() + 0.5;
        double cz = zone.minZ() + 0.5;
        double radius = zone.radius();

        // Ecuador: plano XZ
        drawCircle(consumer, matrix, color, cx, cy, cz, radius,
                new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), camX, camY, camZ);

        // Meridianos: planos que contienen el eje Y girados cada 180/MERIDIAN_COUNT grados
        for (int m = 0; m < MERIDIAN_COUNT; m++) {
            double angle = (Math.PI * m) / MERIDIAN_COUNT;
            Vec3 horizontal = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            drawCircle(consumer, matrix, color, cx, cy, cz, radius, horizontal,
                    new Vec3(0.0, 1.0, 0.0), camX, camY, camZ);
        }
    }

    /** Circunferencia completa de radio dado, en el plano definido por dos ejes ortonormales. */
    private static void drawCircle(VertexConsumer consumer, Matrix4f matrix, int color,
                                   double cx, double cy, double cz, double radius,
                                   Vec3 axis1, Vec3 axis2, double camX, double camY, double camZ) {
        // Fade-in
        addFadedVertex(consumer, matrix, cx, cy, cz, axis1, radius, camX, camY, camZ);
        for (int i = 0; i <= SPHERE_SEGMENTS; i++) {
            double angle = (Math.PI * 2.0 * i) / SPHERE_SEGMENTS;
            double ox = Math.cos(angle) * radius;
            double oy = Math.sin(angle) * radius;
            consumer.addVertex(matrix,
                    (float) (cx + axis1.x * ox + axis2.x * oy - camX),
                    (float) (cy + axis1.y * ox + axis2.y * oy - camY),
                    (float) (cz + axis1.z * ox + axis2.z * oy - camZ)).setColor(color);
        }
        // Fade-out
        addFadedVertex(consumer, matrix, cx, cy, cz, axis1, radius, camX, camY, camZ);
    }

    /** Vertices en axis1*radius con alpha 0: separa circulars consecutivos dentro del strip. */
    private static void addFadedVertex(VertexConsumer consumer, Matrix4f matrix,
                                       double cx, double cy, double cz,
                                       Vec3 axis1, double radius,
                                       double camX, double camY, double camZ) {
        consumer.addVertex(matrix,
                (float) (cx + axis1.x * radius - camX),
                (float) (cy + axis1.y * radius - camY),
                (float) (cz + axis1.z * radius - camZ)).setColor(0);
    }

    private static void rectangle(VertexConsumer consumer, Matrix4f matrix, int color,
                                  float x1, float x2, float z1, float z2, float y) {
        line(consumer, matrix, color, x1, y, z1, x2, y, z1);
        line(consumer, matrix, color, x2, y, z1, x2, y, z2);
        line(consumer, matrix, color, x2, y, z2, x1, y, z2);
        line(consumer, matrix, color, x1, y, z2, x1, y, z1);
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix, int color,
                             float x1, float y1, float z1, float x2, float y2, float z2) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(color);
        consumer.addVertex(matrix, x2, y2, z2).setColor(color);
        consumer.addVertex(matrix, x2, y2, z2).setColor(0);
    }

    private static int zoneColor(ZoneSyncPayload.ZoneEntry zone) {
        int hue = Math.floorMod(zone.shortId().hashCode(), 360);
        return FastColor.ARGB32.color(ALPHA, Mth.hsvToRgb(hue / 360.0F, 0.65F, 1.0F));
    }
}