package com.zurrtum.create.neoforge.mixin;

import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.foundation.block.BouncinessControlBlock;
import com.zurrtum.create.foundation.block.BreakControlBlock;
import com.zurrtum.create.foundation.block.ResistanceControlBlock;
import com.zurrtum.create.foundation.block.ScaffoldingControlBlock;
import com.zurrtum.create.foundation.block.SlipperinessControlBlock;
import com.zurrtum.create.foundation.block.SoundControlBlock;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

// NeoForge: Create Fly на Fabric подменяет вызовы ванили миксинами (*ControlBlock), а NeoForge уже
// заменил эти вызовы своими хуками IBlockExtension — старые инъекции здесь не находят цель
// (у них require = 0), а поведение Create задаём переопределением хуков прямо в Block.
// Для остальных блоков — тело по умолчанию из IBlockExtension NeoForge 26.3.0.99.
@Mixin(Block.class)
public abstract class BlockHooksMixin {
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (this instanceof SlipperinessControlBlock control) {
            return control.getSlipperiness(level, pos);
        }
        return ((Block) (Object) this).getFriction();
    }

    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (this instanceof SoundControlBlock control) {
            return control.getSoundGroup(level, pos);
        }
        return state.getSoundType();
    }

    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (this instanceof ResistanceControlBlock control) {
            return control.getResistance(level, pos);
        }
        return ((Block) (Object) this).getExplosionResistance();
    }

    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        if (this instanceof WeakPowerControlBlock control) {
            return control.shouldCheckWeakPower(state, level, pos, side);
        }
        return state.isRedstoneConductor(level, pos);
    }

    public boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return this instanceof ScaffoldingControlBlock || state.is(Blocks.SCAFFOLDING);
    }

    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if ((Object) this instanceof CopycatBlock copycat) {
            return copycat.getLuminance(level, pos);
        }
        return state.getLightEmission();
    }

    public float getBounceRestitution(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (this instanceof BouncinessControlBlock control && control.skipBounciness(state)) {
            return 0;
        }
        return ((Block) (Object) this).getBounceRestitution();
    }

    public boolean onDestroyedByPlayer(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        ItemStack toolStack,
        boolean willHarvest,
        FluidState fluid
    ) {
        if (this instanceof BreakControlBlock control && !control.onDestroyedByPlayer(state, level, pos, player)) {
            return false;
        }
        if (level.isClientSide()) {
            return level.setBlock(pos, fluid.createLegacyBlock(), 11);
        }
        return level.removeBlock(pos, false);
    }
}
