package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    // require = 0: у NeoForge эта перегрузка только передаёт вызов новой (с StandaloneModelLoader) — для неё ниже.
    @Inject(require = 0, method = "discoverModelDependencies(Ljava/util/Map;Lnet/minecraft/client/resources/model/BlockStateModelLoader$LoadedModels;Lnet/minecraft/client/resources/model/ClientItemInfoLoader$LoadedClientInfos;)Lnet/minecraft/client/resources/model/ModelManager$ResolvedModels;", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ResolvedModel;Ljava/util/Map;)Lnet/minecraft/client/resources/model/ModelManager$ResolvedModels;"))
    private static void collect(
        Map<Identifier, UnbakedModel> allModels,
        BlockStateModelLoader.LoadedModels blockStateModels,
        ClientItemInfoLoader.LoadedClientInfos itemInfos,
        CallbackInfoReturnable<ModelManager.ResolvedModels> cir,
        @Local ModelDiscovery result
    ) {
        PartialModelEventHandler.getRegisterAdditional().keySet().forEach(result::getOrCreateModel);
    }

    @Inject(require = 0, method = "discoverModelDependencies(Ljava/util/Map;Lnet/minecraft/client/resources/model/BlockStateModelLoader$LoadedModels;Lnet/minecraft/client/resources/model/ClientItemInfoLoader$LoadedClientInfos;Lnet/neoforged/neoforge/client/model/standalone/StandaloneModelLoader$LoadedModels;)Lnet/minecraft/client/resources/model/ModelManager$ResolvedModels;", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ResolvedModel;Ljava/util/Map;)Lnet/minecraft/client/resources/model/ModelManager$ResolvedModels;"))
    private static void collectNeoForge(CallbackInfoReturnable<ModelManager.ResolvedModels> cir, @Local ModelDiscovery result) {
        PartialModelEventHandler.getRegisterAdditional().keySet().forEach(result::getOrCreateModel);
    }
}
