package com.nkramzher.twohandedblocks.client.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.client.ProtectedHeldModels;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Third-person offhand two-handed blocks share the selected MAIN-hand model
 * orientation. First-person models are resolved separately when F1 renders.
 * This changes no inventory, movement, collision or placement rules.
 *
 * The original item transforms and model overrides (including resource packs)
 * are applied as though the block were rendered from the player's primary hand.
 * Left-handed players use their left main-hand model as the reference instead.
 */
@Mixin(ItemModelResolver.class)
public abstract class CarriedItemModelContextMixin {

    @Inject(method = "updateForLiving", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$useMainHandModelForOffhandBlock(
            ItemStackRenderState output, ItemStack item, ItemDisplayContext context,
            LivingEntity entity, CallbackInfo ci) {
        // Leave F1 and all non-player entities alone. Only the held two-hand
        // item is eligible; the ordinary hand remains suppressed by its layer.
        if (!(entity instanceof Player player)) return;
        InteractionHand carrying = BlockRules.carriedHand(player);
        if (carrying == null || item != player.getItemInHand(carrying)) return;

        HumanoidArm mainArm = player.getMainArm();
        ItemDisplayContext reference = switch (context) {
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
                    mainArm == HumanoidArm.RIGHT
                            ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                            : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            default -> null;
        };
        if (reference == null) return;

        // This call handles the previous successful offhand context fix,
        // and uses the protected namespace for the approved models only.
        // No inventory component is edited: visualCopy is disposable.
        ItemStack visual = ProtectedHeldModels.visualCopy(item);
        if (carrying == InteractionHand.MAIN_HAND && visual == item) return;
        ((ItemModelResolver) (Object) this).updateForTopItem(
                output, visual, reference, entity.level(), entity,
                entity.getId() + reference.ordinal());
        ci.cancel();
    }
}
