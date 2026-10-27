package com.zurrtum.create.client.blauncher;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.util.List;

// Компактная подпись в левом нижнем углу главного меню (над строкой версии): три коротких строки,
// чтобы не наезжать на кнопки даже в маленьком окне; авторы — во всплывающей подсказке.
// Щелчок — подтверждение и переход на blauncher.ru, «×» — скрыть навсегда (см. BLauncherNotice).
public class BLauncherNoticeWidget extends AbstractWidget {
    private static final int PADDING = 4;
    private static final int LINE = 10;
    private static final Component CLOSE = Component.literal("×");

    private final Screen screen;
    private final Font font;
    private final List<Component> lines;
    private final Runnable onHide;

    public BLauncherNoticeWidget(Screen screen, Font font, int left, int bottom, Runnable onHide) {
        super(0, 0, 0, 0, Component.translatable("create.blauncher.notice.title"));
        this.screen = screen;
        this.font = font;
        this.onHide = onHide;
        lines = List.of(
            Component.translatable("create.blauncher.notice.title").withStyle(ChatFormatting.GOLD),
            Component.translatable("create.blauncher.notice.by"),
            Component.literal("blauncher.ru").withStyle(ChatFormatting.AQUA)
        );
        int textWidth = 0;
        for (Component line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        int width = PADDING + textWidth + PADDING + font.width(CLOSE) + PADDING;
        int height = PADDING + lines.size() * LINE + PADDING - 2;
        setRectangle(width, height, left, bottom - height);
        setTooltip(Tooltip.create(Component.translatable("create.blauncher.notice.tooltip")));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + getWidth(), y + getHeight(), 0xA0000000);
        graphics.outline(x, y, getWidth(), getHeight(), isHovered() ? 0xFFFFAA00 : 0x60FFFFFF);
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (i == lines.size() - 1 && isHovered() && !overClose(mouseX, mouseY)) {
                line = line.copy().withStyle(ChatFormatting.UNDERLINE);
            }
            graphics.text(font, line, x + PADDING, y + PADDING + i * LINE, 0xFFFFFFFF);
        }
        int closeColor = overClose(mouseX, mouseY) ? 0xFFFF5555 : 0xFFAAAAAA;
        graphics.text(font, CLOSE, x + getWidth() - PADDING - font.width(CLOSE), y + PADDING, closeColor);
    }

    private boolean overClose(double mouseX, double mouseY) {
        int closeX = getX() + getWidth() - PADDING - font.width(CLOSE) - 2;
        return mouseX >= closeX && mouseY <= getY() + PADDING + LINE;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (overClose(event.x(), event.y())) {
            BLauncherNotice.hideForever();
            visible = false;
            active = false;
            onHide.run();
            return;
        }
        ConfirmLinkScreen.confirmLinkNow(screen, URI.create(BLauncherNotice.SITE));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
