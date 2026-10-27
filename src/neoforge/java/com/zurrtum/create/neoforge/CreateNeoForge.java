package com.zurrtum.create.neoforge;

import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.foundation.recipe.trie.RecipeTrieFinder;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

// Вход мода на NeoForge: вызывает те же инициализаторы, что Fabric по точкам входа main/client,
// и подключает события NeoForge там, где Create Fly на Fabric обходится миксинами.
@Mod("create")
public final class CreateNeoForge {
    public CreateNeoForge(IEventBus modBus, ModContainer container, Dist dist) {
        new com.zurrtum.create.Create().onInitialize();
        NeoForge.EVENT_BUS.addListener(CreateNeoForge::onBreathe);
        NeoForge.EVENT_BUS.addListener(CreateNeoForge::addReloadListeners);
        if (dist.isClient()) {
            CreateNeoForgeClient.init(modBus);
        }
    }

    // Те же слушатели, что на Fabric добавляет ReloadableServerResourcesMixin.listeners().
    private static void addReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath("create", "recipe_finder"), RecipeFinder.LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath("create", "recipe_trie_finder"), RecipeTrieFinder.LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath("create", "belt_helper"), BeltHelper.LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath("create", "configs"), AllConfigs.LISTENER);
    }

    // NeoForge решает дыхание в CommonHooks.onLivingBreathe, а не в LivingEntity.baseTick,
    // куда на Fabric встроен водолазный шлем (LivingEntityMixin). Логика та же.
    private static void onBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getAbilities().invulnerable) {
            return;
        }
        ServerLevel level = player.level();
        if (player.isInLava()) {
            DivingHelmetItem.breatheInLava(player, level);
        }
        if (!event.canBreathe() && player.isEyeInFluid(FluidTags.WATER) && DivingHelmetItem.breatheUnderwater(player, level)) {
            event.setCanBreathe(true);
        }
    }
}
