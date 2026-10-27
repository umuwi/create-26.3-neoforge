package com.zurrtum.create.client.infrastructure.gui;

import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

// Кнопка-очки рядом с кнопками главного меню и меню паузы — открывает CreateMainMenuScreen (как в оригинале).
// Строка и сдвиг берутся из тех же настроек клиента, что у Create: mainMenuConfigButtonRow/OffsetX,
// ingameMenuConfigButtonRow/OffsetX. Ряды меню 26.3 описаны ключами подписей кнопок, по ним и ищем.
public class OpenCreateMenuButton extends Button {
    // 26.3: главное меню — «Одиночная», «Сетевая», «Realms», «Настройки | Выйти».
    private static final List<String[]> MAIN_MENU = List.of(
        new String[]{"menu.singleplayer", "menu.singleplayer"},
        new String[]{"menu.multiplayer", "menu.multiplayer"},
        new String[]{"menu.online", "menu.online"},
        new String[]{"menu.options", "menu.quit"}
    );
    // Меню паузы 26.3 — «Вернуться», «Достижения | Статистика», «Настройки | Настройки мира», «Выйти»; ряд значков
    // (отзыв, ошибки и т.д.) между ними не считаем — иначе по умолчанию (ряд 3) очки вставали посреди значков.
    private static final List<String[]> INGAME_MENU = List.of(
        new String[]{"menu.returnToGame", "menu.returnToGame"},
        new String[]{"gui.advancements", "gui.stats"},
        new String[]{"menu.options", "menu.options"},
        new String[]{"menu.returnToMenu", "menu.returnToMenu"}
    );

    private static final Identifier GOGGLES_TEXTURE = Identifier.fromNamespaceAndPath("create", "textures/item/goggles.png");

    public OpenCreateMenuButton(int x, int y) {
        super(x, y, 20, 20, Component.empty(), b -> ScreenOpener.open(new CreateMainMenuScreen(Minecraft.getInstance().gui.screen())), DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractDefaultSprite(graphics);
        // Текстурой, а не предметом: в главном меню 26.3 компоненты предметов ещё не загружены, и ItemStack
        // падал «Components not bound yet» (27.10.2026). Иконка та же — текстура очков Create.
        graphics.blit(RenderPipelines.GUI_TEXTURED, GOGGLES_TEXTURE, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
    }

    /** Кнопка для экрана или null, если на нём её быть не должно (не то меню, ряд 0, нужная кнопка не нашлась). */
    public static @Nullable OpenCreateMenuButton forScreen(Screen screen, List<? extends GuiEventListener> widgets) {
        List<String[]> rows;
        int rowIdx;
        int offsetX;
        if (screen instanceof TitleScreen) {
            rows = MAIN_MENU;
            rowIdx = AllConfigs.client().mainMenuConfigButtonRow.get();
            offsetX = AllConfigs.client().mainMenuConfigButtonOffsetX.get();
        } else if (screen instanceof PauseScreen) {
            rows = INGAME_MENU;
            rowIdx = AllConfigs.client().ingameMenuConfigButtonRow.get();
            offsetX = AllConfigs.client().ingameMenuConfigButtonOffsetX.get();
        } else {
            return null;
        }
        if (rowIdx <= 0 || rowIdx > rows.size()) {
            return null;
        }
        boolean onLeft = offsetX < 0;
        String target = Component.translatable(rows.get(rowIdx - 1)[onLeft ? 0 : 1]).getString();
        for (GuiEventListener listener : widgets) {
            if (listener instanceof AbstractWidget w && w.getMessage().getString().equals(target)) {
                return new OpenCreateMenuButton(w.getX() + offsetX + (onLeft ? -20 : w.getWidth()), w.getY());
            }
        }
        return null;
    }
}
