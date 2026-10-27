package com.zurrtum.create.neoforge;

import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.model.obj.ObjLoader;
import com.zurrtum.create.client.flywheel.backend.compile.FlwProgramsReloader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.foundation.gui.render.*;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import com.zurrtum.create.client.content.trains.TrainHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Supplier;

// Клиентская часть входа NeoForge (отдельный класс, чтобы сервер не грузил клиентские классы).
final class CreateNeoForgeClient {
    private CreateNeoForgeClient() {
    }

    static void init(IEventBus modBus) {
        new com.zurrtum.create.client.Create().onInitializeClient();
        modBus.addListener(CreateNeoForgeClient::registerPictureInPicture);
        modBus.addListener(CreateNeoForgeClient::registerGuiLayers);
        modBus.addListener(CreateNeoForgeClient::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(CreateNeoForgeClient::hideContextualBar);
    }

    // Те же слушатели, что на Fabric регистрирует MinecraftMixin в конструкторе Minecraft.
    private static void addReloadListeners(AddClientReloadListenersEvent event) {
        if (RenderSystem.getDevice().getDeviceInfo().backendName().equals("OpenGL")) {
            event.addListener(Identifier.fromNamespaceAndPath("create", "flywheel_programs"), FlwProgramsReloader.INSTANCE);
        }
        event.addListener(Identifier.fromNamespaceAndPath("create", "obj_loader"), ObjLoader.INSTANCE);
        event.addListener(Identifier.fromNamespaceAndPath("create", "client_resources"), com.zurrtum.create.client.Create.RESOURCE_RELOAD_LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath("create", "train_hats"), TrainHatInfoReloadListener.LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath("create", "ponder"), Ponder.RESOURCE_RELOAD_LISTENER);
    }

    // На Fabric HUD поезда рисует HudMixin вместо полосы опыта/контекста (nextContextualInfoState → EMPTY).
    // В NeoForge HUD собран из слоёв: свой слой ставим перед фоном контекстной полосы и скрываем её.
    private static boolean trainHudShown;

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
            VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND,
            Identifier.fromNamespaceAndPath("create", "train_hud"),
            (graphics, deltaTracker) -> trainHudShown = TrainHUD.renderOverlay(Minecraft.getInstance(), graphics, deltaTracker)
        );
    }

    private static void hideContextualBar(RenderGuiLayerEvent.Pre event) {
        Identifier name = event.getName();
        if (trainHudShown && (name.equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND) || name.equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR))) {
            event.setCanceled(true);
        }
    }

    // На Fabric те же рендереры добавляет GuiRendererMixin в конструкторе GuiRenderer; NeoForge этот
    // конструктор заменил пулом рендереров из события RegisterPictureInPictureRenderersEvent.
    private static void registerPictureInPicture(RegisterPictureInPictureRenderersEvent event) {
        register(event, ItemTransformRenderState.class, ItemTransformElementRenderer::new);
        register(event, BlockTransformRenderState.class, BlockTransformElementRenderer::new);
        register(event, EntityBlockRenderState.class, EntityBlockRenderer::new);
        register(event, PartialRenderState.class, PartialElementRenderer::new);
        register(event, BlazeBurnerRenderState.class, BlazeBurnerElementRenderer::new);
        register(event, PressBasinRenderState.class, PressBasinRenderer::new);
        register(event, PressRenderState.class, PressRenderer::new);
        register(event, MixingBasinRenderState.class, MixingBasinRenderer::new);
        register(event, BasinBlazeBurnerRenderState.class, BasinBlazeBurnerRenderer::new);
        register(event, MillstoneRenderState.class, MillstoneRenderer::new);
        register(event, SawRenderState.class, SawRenderer::new);
        register(event, CrushWheelRenderState.class, CrushWheelRenderer::new);
        register(event, DeployerRenderState.class, DeployerRenderer::new);
        register(event, ManualBlockRenderState.class, ManualBlockRenderer::new);
        register(event, SpoutRenderState.class, SpoutRenderer::new);
        register(event, CrafterRenderState.class, CrafterRenderer::new);
        register(event, DrainRenderState.class, DrainRenderer::new);
        register(event, SandPaperRenderState.class, SandPaperRenderer::new);
        register(event, TitleTextRenderState.class, TitleTextRenderer::new);
        register(event, SceneRenderState.class, SceneRenderer::new);
        register(event, FanRenderState.class, FanRenderer::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(
        RegisterPictureInPictureRenderersEvent event,
        Class<? extends PictureInPictureRenderState> state,
        Supplier<? extends PictureInPictureRenderer<?>> factory
    ) {
        event.register((Class) state, (Supplier) factory);
    }
}
