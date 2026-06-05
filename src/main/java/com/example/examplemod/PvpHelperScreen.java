package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PvpHelperScreen extends Screen {

    public PvpHelperScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int buttonWidth = 180;
        int buttonHeight = 20;
        int startX = this.width / 2 - buttonWidth / 2;
        int startY = this.height / 2 - 50;

        this.addRenderableWidget(Button.builder(
                Component.literal("ESP Игроки: " + (PvpBuild.espEnabled ? "ВКЛ" : "ВЫКЛ")),
                button -> {
                    PvpBuild.espEnabled = !PvpBuild.espEnabled;
                    button.setMessage(Component.literal("ESP Игроки: " + (PvpBuild.espEnabled ? "ВКЛ" : "ВЫКЛ")));
                }
        ).bounds(startX, startY, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("HUD Брони: " + (PvpBuild.armorHudEnabled ? "ВКЛ" : "ВЫКЛ")),
                button -> {
                    PvpBuild.armorHudEnabled = !PvpBuild.armorHudEnabled;
                    button.setMessage(Component.literal("HUD Брони: " + (PvpBuild.armorHudEnabled ? "ВКЛ" : "ВЫКЛ")));
                }
        ).bounds(startX, startY + 25, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Подсветка блоков: " + (PvpBuild.blockOverlayEnabled ? "ВКЛ" : "ВЫКЛ")),
                button -> {
                    PvpBuild.blockOverlayEnabled = !PvpBuild.blockOverlayEnabled;
                    button.setMessage(Component.literal("Подсветка блоков: " + (PvpBuild.blockOverlayEnabled ? "ВКЛ" : "ВЫКЛ")));
                }
        ).bounds(startX, startY + 50, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Индикатор Reach: " + (PvpBuild.reachDisplayEnabled ? "ВКЛ" : "ВЫКЛ")),
                button -> {
                    PvpBuild.reachDisplayEnabled = !PvpBuild.reachDisplayEnabled;
                    button.setMessage(Component.literal("Индикатор Reach: " + (PvpBuild.reachDisplayEnabled ? "ВКЛ" : "ВЫКЛ")));
                }
        ).bounds(startX, startY + 75, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Закрыть"),
                button -> this.onClose()
        ).bounds(startX, startY + 110, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 75, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}