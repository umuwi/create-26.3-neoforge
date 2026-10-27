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
        markRegistriesSynced();
        NeoForge.EVENT_BUS.addListener(CreateNeoForge::onBreathe);
        NeoForge.EVENT_BUS.addListener(CreateNeoForge::addReloadListeners);
        if (dist.isClient()) {
            CreateNeoForgeClient.init(modBus);
        }
    }

    // Create кладёт свои реестры прямо в BuiltInRegistries, минуя RegistryBuilder, и NeoForge считает их
    // несинхронизируемыми: ByteBufCodecs.registry() по таким падает «Cannot use ID syncing for non-synced
    // built-in registry». 27.10.2026 на сервере так отключало игрока при открытии окна расписания поезда
    // (OpenScreenPacket → create:menu_type); то же грозило фильтрам (item_attribute_type). setSync закрытый.
    private static void markRegistriesSynced() {
        try {
            java.lang.reflect.Method setSync = net.neoforged.neoforge.registries.BaseMappedRegistry.class
                .getDeclaredMethod("setSync", boolean.class);
            setSync.setAccessible(true);
            for (net.minecraft.core.Registry<?> registry : net.minecraft.core.registries.BuiltInRegistries.REGISTRY) {
                if ("create".equals(registry.key().identifier().getNamespace())
                    && registry instanceof net.neoforged.neoforge.registries.BaseMappedRegistry<?> mapped) {
                    setSync.invoke(mapped, true);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Create: не удалось включить синхронизацию реестров", e);
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
