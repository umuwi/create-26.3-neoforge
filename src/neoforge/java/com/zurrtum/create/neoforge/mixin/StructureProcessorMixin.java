package com.zurrtum.create.neoforge.mixin;

import com.zurrtum.create.foundation.blockEntity.EntityControlStructureProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import com.zurrtum.create.content.schematics.SchematicProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

// NeoForge: фильтр сущностей структур Create (на Fabric — StructureTemplateMixin со своим итератором)
// через родной хук processEntity; null — сущность не ставится. StructureProcessor в 26.3 — интерфейс,
// поэтому переопределяем хук прямо в процессоре Create (он единственный с EntityControlStructureProcessor).
@Mixin(SchematicProcessor.class)
public abstract class StructureProcessorMixin {
    public StructureTemplate.@Nullable StructureEntityInfo processEntity(
        LevelReader world,
        BlockPos seedPos,
        StructureTemplate.StructureEntityInfo rawEntityInfo,
        StructureTemplate.StructureEntityInfo entityInfo,
        StructurePlaceSettings placementSettings,
        StructureTemplate template
    ) {
        if (this instanceof EntityControlStructureProcessor control && world instanceof Level level && control.skip(level, rawEntityInfo)) {
            return null;
        }
        return entityInfo;
    }
}
