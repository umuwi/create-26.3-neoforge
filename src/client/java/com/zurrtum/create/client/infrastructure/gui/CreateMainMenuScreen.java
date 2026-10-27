package com.zurrtum.create.client.infrastructure.gui;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiBlockStateRenderBuilder;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.ponder.foundation.ui.PonderTagIndexScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import com.mojang.blaze3d.Blaze3D;
import org.joml.Matrix3x2fStack;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

// Меню Create из главного меню и меню паузы — как в оригинальном Create (CreateMainMenuScreen); Create Fly его
// выбросил, владелец заметил пропажу кнопки (27.10.2026). Отличия порта: нет панорамы (в 26.3 другой API),
// экрана настроек в Create Fly нет — «Настроить...» открывает папку с файлами настроек, а «Нашли ошибку?»
// ведёт в репозиторий порта, не к авторам Create (ошибки порта — наши).
public class CreateMainMenuScreen extends AbstractSimiScreen {
    public static final String CURSEFORGE_LINK = "https://www.curseforge.com/minecraft/mc-mods/create";
    public static final String MODRINTH_LINK = "https://modrinth.com/mod/create";
    public static final String SUPPORT_LINK = "https://github.com/Creators-of-Create/Create/wiki/Supporting-the-Project";

    private static String issuesLink() {
        boolean neoforge = FabricLoader.getInstance().isModLoaded("neoforge");
        return "https://github.com/umuwi/create-26.3-" + (neoforge ? "neoforge" : "fabric") + "/issues";
    }

    protected final Screen parent;
    private final GuiBlockStateRenderBuilder[] cogs = new GuiBlockStateRenderBuilder[4];
    private Button ponderIndex;

    public CreateMainMenuScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        // Шестерни рисует Flywheel через ModelRenderHelper, а он создаётся при загрузке мира. Из главного меню
        // мира нет — без этого вызова экран падал NullPointerException (27.10.2026); при входе в мир Flywheel
        // пересоздаст помощников сам.
        if (minecraft.level == null) {
            com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper.onReloadLevelRenderer();
        }
        cogs[0] = GuiGameElement.of(AllBlocks.LARGE_COGWHEEL.defaultBlockState()).scale(2.2f).padding(40);
        cogs[1] = GuiGameElement.of(AllBlocks.COGWHEEL.defaultBlockState()).scale(2.2f).padding(18);
        cogs[2] = GuiGameElement.of(AllBlocks.LARGE_COGWHEEL.defaultBlockState()).scale(2.2f).padding(40);
        cogs[3] = GuiGameElement.of(AllBlocks.COGWHEEL.defaultBlockState()).scale(2.2f).padding(18);
        addButtons();
    }

    private void addButtons() {
        int yStart = height / 4 + 40;
        int center = width / 2;

        addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.return"), b -> onClose())
            .bounds(center - 100, yStart + 92, 200, 20).build());
        addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.configure"), b -> openConfigFolder())
            .bounds(center - 100, yStart + 8, 200, 20).build());

        ponderIndex = Button.builder(CreateLang.translateDirect("menu.ponder_index"), b -> ScreenOpener.open(new PonderTagIndexScreen()))
            .bounds(center + 2, yStart + 32, 98, 20).build();
        // Как в оригинале: указатель Ponder строит сцены из мира — из главного меню его не открыть.
        ponderIndex.active = !(parent instanceof TitleScreen);
        if (!ponderIndex.active) {
            ponderIndex.setTooltip(Tooltip.create(CreateLang.translateDirect("menu.only_ingame")));
        }
        addRenderableWidget(ponderIndex);

        addRenderableWidget(new PlatformIconButton(center - 100, yStart + 32, 49, 20, AllGuiTextures.CURSEFORGE_LOGO, 0.085f,
            b -> linkTo(CURSEFORGE_LINK),
            Tooltip.create(Component.literal("CurseForge").withStyle(s -> s.withColor(0xFC785C).withBold(true)))));
        addRenderableWidget(new PlatformIconButton(center - 50, yStart + 32, 49, 20, AllGuiTextures.MODRINTH_LOGO, 0.0575f,
            b -> linkTo(MODRINTH_LINK),
            Tooltip.create(Component.literal("Modrinth").withStyle(s -> s.withColor(0x3FD32B).withBold(true)))));

        addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.report_bugs"), b -> linkTo(issuesLink()))
            .bounds(center + 2, yStart + 68, 98, 20).build());
        addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.support"), b -> linkTo(SUPPORT_LINK))
            .bounds(center - 100, yStart + 68, 98, 20).build());
    }

    @Override
    protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        long ms = Util.getMillis();
        int center = width / 2;
        // Шестерни по бокам логотипа — большая и малая, крутятся навстречу друг другу, как в оригинале.
        // Порядок вызова для картинок-вставок не важен — слои задаёт nextStratum: малые внизу, большие поверх
        // (владелец, 27.10.2026: «нужно поменять местами наложение»).
        for (int i = 0; i < 2; i++) {
            int side = i == 0 ? -1 : 1;
            cogs[i * 2 + 1].rotate(30, ms / -16f * side + 22.5f, 0).at(center + side * 112 - 24 - 9, 48 - 9).render(graphics);
        }
        graphics.nextStratum();
        for (int i = 0; i < 2; i++) {
            int side = i == 0 ? -1 : 1;
            cogs[i * 2].rotate(30, ms / 32f * side, 0).at(center + side * 78 - 24 - 20, 30 - 20).render(graphics);
        }
        graphics.nextStratum();

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(center - 32, 12);
        pose.scale(0.25f, 0.25f);
        AllGuiTextures.LOGO.render(graphics, 0, 0);
        pose.popMatrix();

        // Название, версия и «порт BLauncher» одной строкой: на маленьком окне две строки налезали на кнопки.
        String version = FabricLoader.getInstance().getModContainer("create")
            .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
        graphics.centeredText(font, Component.literal("Create").withStyle(ChatFormatting.BOLD)
            .append(Component.literal(" 26.3" + (version.isEmpty() ? "" : " (" + version + ")")).withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.translatable("create.blauncher.notice.by").withStyle(ChatFormatting.GRAY)),
            center, height / 4 + 40 - 14, 0xFFE4BB67);
    }

    private void openConfigFolder() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        Path create = dir.resolve("create");
        Blaze3D.openPath(Files.isDirectory(create) ? create : dir);
    }

    private void linkTo(String url) {
        ConfirmLinkScreen.confirmLinkNow(this, URI.create(url), true);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void removed() {
        super.removed();
        for (GuiBlockStateRenderBuilder cog : cogs) {
            if (cog != null) {
                cog.clear();
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    protected static class PlatformIconButton extends Button {
        protected final AllGuiTextures icon;
        protected final float scale;

        public PlatformIconButton(int x, int y, int w, int h, AllGuiTextures icon, float scale, OnPress onPress, Tooltip tooltip) {
            super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
            this.icon = icon;
            this.scale = scale;
            setTooltip(tooltip);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            extractDefaultSprite(graphics);
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(getX() + width / 2f - (icon.getWidth() * scale) / 2, getY() + height / 2f - (icon.getHeight() * scale) / 2);
            pose.scale(scale, scale);
            icon.render(graphics, 0, 0);
            pose.popMatrix();
        }
    }
}
