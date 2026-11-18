package com.nkramzher.twohandedblocks.client.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.TwoHandedBlocks;
import java.lang.reflect.Field;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional compatibility with Smooth Player Animations 2.1.0 for MC 26.3.
 *
 * SPA uses a RigPlayerModel subclass. Its setupAnim calls PlayerModel.setupAnim
 * (including our existing PlayerModelMixin), then replaces the arm pose with
 * its own rig/CEM result. Reapply the approved two-hand rotations ONLY after
 * SPA's CEM pose is finished. For normal items we leave SPA entirely alone.
 *
 * CEM mode's saved frame is also used for armor, so synchronise the rotations
 * there. The bending rig uses skinning matrices rather than vanilla ModelParts;
 * this intentionally does not interfere with that different path.
 *
 * No class from the SPA JAR is linked here: loading without SPA is safe.
 */
@Pseudo
@Mixin(targets = "com.huaxiaosheng.spapacks.client.RigPlayerModel", priority = 500)
public abstract class SmoothPlayerAnimationsRigMixin extends PlayerModel {
    @Unique private static volatile SpaFields twohandedblocks$spaFields;
    @Unique private static volatile boolean twohandedblocks$spaReflectionFailed;

    protected SmoothPlayerAnimationsRigMixin(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At("TAIL"), require = 0)
    private void twohandedblocks$restoreCemCarryPose(AvatarRenderState state, CallbackInfo ci) {
        if (state.isSpectator) return;
        ItemStack main = state.mainArm == HumanoidArm.RIGHT
                ? state.rightHandItemStack : state.leftHandItemStack;
        ItemStack off = state.mainArm == HumanoidArm.RIGHT
                ? state.leftHandItemStack : state.rightHandItemStack;
        if (BlockRules.carriedHand(main, off) == null) return;

        try {
            SpaFields fields = twohandedblocks$spaFields;
            if (fields == null) {
                fields = new SpaFields((Object) this);
                twohandedblocks$spaFields = fields;
            }
            Object frame = fields.shown.get(this);
            // In bend-model mode, SPA draws from a skinned mesh instead of
            // these ModelParts. Do not corrupt the bend rig's arm matrices.
            if (frame == null || fields.custom.getBoolean(frame)
                    || !fields.vanillaPosed.getBoolean(frame)) return;

            float[][] parts = (float[][]) fields.cemParts.get(frame);
            if (parts == null || parts.length < 4
                    || parts[2].length < 3 || parts[3].length < 3) return;

            // The same F5 rotations as PlayerModelMixin (already tested
            // without SPA). Do not move or rotate the held item itself.
            twohandedblocks$setRotations(this.rightArm, parts[2],
                    -1.28f, -0.12f, -0.18f);
            twohandedblocks$setRotations(this.leftArm, parts[3],
                    -1.28f, 0.12f, 0.18f);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            if (!twohandedblocks$spaReflectionFailed) {
                twohandedblocks$spaReflectionFailed = true;
                TwoHandedBlocks.LOGGER.warn(
                        "Smooth Player Animations CEM arm compatibility unavailable; "
                        + "falling back to normal rendering", exception);
            }
        }
    }

    @Unique
    private static void twohandedblocks$setRotations(ModelPart arm, float[] saved,
            float x, float y, float z) {
        arm.xRot = saved[0] = x;
        arm.yRot = saved[1] = y;
        arm.zRot = saved[2] = z;
    }

    @Unique
    private static final class SpaFields {
        private final Field shown;
        private final Field custom;
        private final Field vanillaPosed;
        private final Field cemParts;

        private SpaFields(Object rig) throws ReflectiveOperationException {
            Class<?> rigType = rig.getClass();
            this.shown = twohandedblocks$field(rigType, "shown");
            Class<?> frameType = shown.getType();
            this.custom = twohandedblocks$field(frameType, "custom");
            this.vanillaPosed = twohandedblocks$field(frameType, "vanillaPosed");
            this.cemParts = twohandedblocks$field(frameType, "cemParts");
        }

        private static Field twohandedblocks$field(Class<?> type, String name)
                throws ReflectiveOperationException {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        }
    }
}
