package com.zurrtum.create.neoforge.mixin;

import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import net.minecraft.network.Connection;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// NeoForge сам зовёт IBlockEntityExtension.onDataPacket(Connection, ValueInput) — направляем в метод Create.
// handleUpdateTag(ValueInput) у SyncedBlockEntity совпадает с хуком NeoForge и переопределяет его без миксина.
@Mixin(SyncedBlockEntity.class)
public abstract class SyncedBlockEntityMixin {
    @Shadow(remap = false)
    public abstract void onDataPacket(ValueInput view);

    public void onDataPacket(Connection net, ValueInput input) {
        onDataPacket(input);
    }
}
