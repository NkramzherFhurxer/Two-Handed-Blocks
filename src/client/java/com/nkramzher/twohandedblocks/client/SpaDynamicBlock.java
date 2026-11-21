package com.nkramzher.twohandedblocks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nkramzher.twohandedblocks.TwoHandedBlocks;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

/**
 * Experimental SPA-only third-person block motion. No SPA classes are linked.
 * If an anchor is missing or the player is not on the bent rig, the original
 * fixed placement is used unchanged. Never changes item transforms in F1.
 */
public final class SpaDynamicBlock {
    // Safety trial: keep the proven, fixed two-hand hold as the center.
    // Previous cap (0.65) let the item travel far outside the gripping hands.
    private static final float FOLLOW = 0.42f;
    private static final double MAX_MOTION = 0.23;
    private static final float DELTA_GAIN = 1.35f;
    private static final long RESET_AFTER_NS = 1_500_000_000L;
    private static final Map<Integer, Sample> SAMPLES = new HashMap<>();
    private static boolean checked;
    private static Method anchor;
    private static Method bent;
    private static boolean failed;
    private static long nextReportAt;
    private static String lastReport = "";

    /** Rate-limited diagnostic messages in logs/latest.log (no render changes). */
    private static void report(String status, String details) {
        long now = System.nanoTime();
        if (!status.equals(lastReport) || now >= nextReportAt) {
            nextReportAt = now + 5_000_000_000L;
            lastReport = status;
            TwoHandedBlocks.LOGGER.info("[SPA dynamic diagnostic] {}: {}", status, details);
        }
    }

    private SpaDynamicBlock() {}

    /** Called only when the existing two-handed renderer draws the selected block. */
    public static void follow(AvatarRenderState state, PoseStack poses) {
        if (failed) return;
        if (!FabricLoader.getInstance().isModLoaded("spa_packs")) {
            report("SPA_NOT_PRESENT", "The original static position is expected");
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            report("PLAYER_NOT_RESOLVED", "AvatarRenderState.id=" + state.id);
            return;
        }

        try {
            if (!checked) {
                Class<?> api = Class.forName("com.huaxiaosheng.spapacks.api.SpaApi");
                anchor = api.getMethod("anchor", AbstractClientPlayer.class,
                        String.class, double[].class, float[].class);
                bent = api.getMethod("onBentRig", AbstractClientPlayer.class);
                checked = true;
            }
            if (!Boolean.TRUE.equals(bent.invoke(null, player))) {
                report("NOT_BENT", "SPA does not report the articulated model for this player");
                return;
            }

            double[] left = new double[3];
            double[] right = new double[3];
            double[] root = new double[3];
            // SPA 2.1.0 fills a 3x3 orientation matrix (9 floats), not a quaternion.
            float[] rotation = new float[9];
            if (!readAnchor(player, "left_hand", left, rotation)) {
                report("MISSING_LEFT_HAND", "No fresh left-hand anchor; static fallback");
                return;
            }
            if (!readAnchor(player, "right_hand", right, rotation)) {
                report("MISSING_RIGHT_HAND", "No fresh right-hand anchor; static fallback");
                return;
            }
            if (!readAnchor(player, "root", root, rotation)) {
                report("MISSING_ROOT", "No fresh root anchor; static fallback");
                return;
            }

            double dx = 0.5 * (left[0] + right[0]) - root[0];
            double dy = 0.5 * (left[1] + right[1]) - root[1];
            double dz = 0.5 * (left[2] + right[2]) - root[2];
            if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) {
                report("INVALID_ANCHOR_COORDINATES", "At least one coordinate was non-finite");
                return;
            }

            // Remove yaw to avoid the block moving sideways when turning in place.
            double yaw = Math.toRadians(player.yBodyRot);
            double cos = Math.cos(yaw), sin = Math.sin(yaw);
            double localX = cos * dx + sin * dz;
            double localZ = -sin * dx + cos * dz;

            long now = System.nanoTime();
            Sample sample = SAMPLES.computeIfAbsent(state.id, ignored -> new Sample());
            if (sample.lastAt == 0 || now - sample.lastAt > RESET_AFTER_NS) {
                sample.x0 = localX;
                sample.y0 = dy;
                sample.z0 = localZ;
                sample.sx = sample.sy = sample.sz = 0;
            }
            sample.lastAt = now;

            // Follow changes in the hand midpoint relative to the first valid
            // position. Keep the user's approved block offsets, scale and yaw.
            double tx = localX - sample.x0;
            double ty = dy - sample.y0;
            double tz = localZ - sample.z0;
            double magnitude = Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (magnitude > MAX_MOTION) {
                double factor = MAX_MOTION / magnitude;
                tx *= factor;
                ty *= factor;
                tz *= factor;
            }
            sample.sx += FOLLOW * (tx - sample.sx);
            sample.sy += FOLLOW * (ty - sample.sy);
            sample.sz += FOLLOW * (tz - sample.sz);

            // Relative offsets have ALREADY been unrotated into the player's
            // local space above. The item layer's PoseStack is likewise in
            // entity-local coordinates. Do not invert the (camera-dependent)
            // PoseStack or rotate the delta a second time.
            //
            // Entity model Y grows downward. SPA world-anchor Y grows upward.
            Vector3f local = new Vector3f(
                    (float) sample.sx * DELTA_GAIN,
                    -(float) sample.sy * DELTA_GAIN,
                    (float) sample.sz * DELTA_GAIN);
            if (Float.isFinite(local.x) && Float.isFinite(local.y) && Float.isFinite(local.z)) {
                float beforeClamp = local.length();
                if (beforeClamp > MAX_MOTION) {
                    local.mul((float) (MAX_MOTION / beforeClamp));
                }
                poses.translate(local.x, local.y, local.z);
                report("ANCHORS_APPLIED", String.format(java.util.Locale.ROOT,
                        "player=%d, handDelta=(%.4f, %.4f, %.4f), local=(%.4f, %.4f, %.4f), localClamped=%s",
                        state.id, tx, ty, tz, local.x, local.y, local.z,
                        beforeClamp > MAX_MOTION));
            } else {
                report("RENDER_TRANSFORM_REJECTED", "Local hand offset was non-finite");
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            failed = true;
            TwoHandedBlocks.LOGGER.warn("Optional SPA dynamic hold disabled; using fixed F5 pose", exception);
        }
    }

    private static boolean readAnchor(AbstractClientPlayer player, String id,
            double[] out, float[] rotation) throws ReflectiveOperationException {
        return Boolean.TRUE.equals(anchor.invoke(null, player, id, out, rotation));
    }

    private static final class Sample {
        double x0, y0, z0, sx, sy, sz;
        long lastAt;
    }
}
