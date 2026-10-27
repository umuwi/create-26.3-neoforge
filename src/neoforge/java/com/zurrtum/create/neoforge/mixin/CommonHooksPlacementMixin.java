package com.zurrtum.create.neoforge.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// NeoForge: на сервере установка предмета идёт через CommonHooks.onPlaceItemIntoWorld, а не через
// ItemStack.useOn, где Create Fly ловит её для суперклея и жезла симметрии (см. ItemStackMixin).
@Mixin(CommonHooks.class)
public class CommonHooksPlacementMixin {
    @Inject(method = "onPlaceItemIntoWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private static void create$cacheState(
        UseOnContext context,
        CallbackInfoReturnable<InteractionResult> cir,
        @Share("place") LocalRef<BlockPlaceContext> place
    ) {
        if (context.getItemInHand().getItem() instanceof BlockItem && !context.getLevel().isClientSide()) {
            place.set(new BlockPlaceContext(context));
        }
    }

    @Inject(method = "onPlaceItemIntoWorld", at = @At("RETURN"))
    private static void create$useOnBlock(
        UseOnContext context,
        CallbackInfoReturnable<InteractionResult> cir,
        @Share("place") LocalRef<BlockPlaceContext> place
    ) {
        BlockPlaceContext placementContext = place.get();
        if (placementContext == null || context.getPlayer() == null || !cir.getReturnValue().consumesAction()) {
            return;
        }
        ServerLevel world = (ServerLevel) context.getLevel();
        BlockPos pos = placementContext.getClickedPos();
        SuperGlueHandler.glueListensForBlockPlacement(world, context.getPlayer(), pos);
        if (!context.getItemInHand().components.has(DataComponents.CONSUMABLE)) {
            SymmetryHandler.onBlockPlaced(world, context.getPlayer(), pos, placementContext);
        }
    }
}
