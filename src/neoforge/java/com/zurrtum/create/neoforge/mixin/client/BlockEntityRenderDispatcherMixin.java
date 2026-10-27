package com.zurrtum.create.neoforge.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// NeoForge отсекает каждую блок-сущность по renderer.getRenderBoundingBox(), а по умолчанию это куб самого
// блока. В ванили 26.3 и на Fabric такой проверки нет, и рендереры Create рамок не задают: изгибы путей,
// ремни, цепные конвейеры рисуются далеко за своим блоком и пропадали, когда блок-начало уходил из кадра
// (27.10.2026, «железнодорожный путь пропадает иногда»). Для Create — как в ванили, без этой проверки.
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @WrapOperation(method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;ZLnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;getRenderBoundingBox(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/world/phys/AABB;"))
    private AABB create$noUnitCubeCulling(BlockEntityRenderer<BlockEntity, ?> renderer, BlockEntity blockEntity, Operation<AABB> original) {
        if (renderer.getClass().getName().startsWith("com.zurrtum.create.")) {
            return AABB.INFINITE;
        }
        return original.call(renderer, blockEntity);
    }
}
