package com.nkramzher.twohandedblocks.client.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pose both arms around the selected block in third-person. */
@Mixin(value = PlayerModel.class, priority = 500)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
    protected PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void twohandedblocks$poseArms(AvatarRenderState state, CallbackInfo ci) {
        ItemStack selected = state.mainArm == net.minecraft.world.entity.HumanoidArm.RIGHT
                ? state.rightHandItemStack : state.leftHandItemStack;
        if (state.isSpectator || !BlockRules.isCarriedBlock(selected)) return;

        // Rest pose: elbows forward and wrists turned in toward the middle of the chest.
        this.rightArm.xRot = -1.28f;
        this.leftArm.xRot = -1.28f;
        this.rightArm.yRot = -0.12f;
        this.leftArm.yRot = 0.12f;
        this.rightArm.zRot = -0.18f;
        this.leftArm.zRot = 0.18f;
    }
}
