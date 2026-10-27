package com.zurrtum.create.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import com.zurrtum.create.foundation.item.CustomBlockEntityTagItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryPlacementContext;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @WrapOperation(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "NEW", target = "(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/item/context/BlockPlaceContext;"))
    private BlockPlaceContext replaceContext(UseOnContext context, Operation<BlockPlaceContext> original) {
        if (context instanceof SymmetryPlacementContext placementContext) {
            return placementContext;
        }
        return original.call(context);
    }

    // require = 0: на NeoForge эта цель заменена его хуком (см. src/neoforge), инъекция пропускается.
    @WrapOperation(require = 0, method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType checkSound(
        BlockState instance,
        Operation<SoundType> original,
        @Local(argsOnly = true) BlockPlaceContext placeContext,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        if (placeContext instanceof ItemPlacementSoundContext context) {
            group.set(context);
            return null;
        }
        return original.call(instance);
    }

    // require = 0: на NeoForge эта цель заменена его хуком (см. src/neoforge), инъекция пропускается.
    @WrapOperation(require = 0, method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent getGroup(
        BlockItem instance,
        BlockState blockState,
        Operation<SoundEvent> original,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        ItemPlacementSoundContext context = group.get();
        if (context != null) {
            SoundEvent sound = context.getSound();
            if (sound != null) {
                return sound;
            }
        }
        return original.call(instance, blockState);
    }

    // NeoForge: те же два обработчика для его вызовов с уровнем — getSoundType(level, pos, entity)
    // и getPlaceSound(state, level, pos, player); на Fabric этих вызовов нет, отсюда require = 0.
    @WrapOperation(require = 0, method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType checkSoundNeoForge(
        BlockState instance,
        LevelReader level,
        BlockPos pos,
        Entity entity,
        Operation<SoundType> original,
        @Local(argsOnly = true) BlockPlaceContext placeContext,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        if (placeContext instanceof ItemPlacementSoundContext context) {
            group.set(context);
            return null;
        }
        return original.call(instance, level, pos, entity);
    }

    @WrapOperation(require = 0, method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent getGroupNeoForge(
        BlockItem instance,
        BlockState blockState,
        Level level,
        BlockPos pos,
        Player player,
        Operation<SoundEvent> original,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        ItemPlacementSoundContext context = group.get();
        if (context != null) {
            SoundEvent sound = context.getSound();
            if (sound != null) {
                return sound;
            }
        }
        return original.call(instance, blockState, level, pos, player);
    }

    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getVolume()F"))
    private float getVolume(
        SoundType instance,
        Operation<Float> original,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        if (instance == null) {
            return group.get().getVolume();
        }
        return original.call(instance);
    }

    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getPitch()F"))
    private float getPitch(
        SoundType instance,
        Operation<Float> original,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        if (instance == null) {
            return group.get().getPitch();
        }
        return original.call(instance);
    }

    // 26.3: updateCustomBlockEntityTag стал статическим — отдаём вызов предметам Create.
    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean customBlockEntityTag(
        Level level,
        @Nullable Player player,
        BlockPos pos,
        ItemStack stack,
        Operation<Boolean> original
    ) {
        if ((Object) this instanceof CustomBlockEntityTagItem item) {
            return item.updateCustomBlockEntityTag(pos, level, player, stack, level.getBlockState(pos));
        }
        return original.call(level, player, pos, stack);
    }
}
