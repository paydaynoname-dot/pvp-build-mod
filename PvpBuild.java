package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

@Mod(PvpBuild.MOD_ID)
public class PvpBuild {
    public static final String MOD_ID = "examplemod";

    public static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.pvpbuild.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_4,
            "category.pvpbuild.main"
    );

    public static boolean espEnabled = true;
    public static boolean armorHudEnabled = true;
    public static boolean reachDisplayEnabled = true;
    public static boolean blockOverlayEnabled = true;

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_MENU_KEY);
        }
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameForgeEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                while (OPEN_MENU_KEY.consumeClick()) {
                    mc.setScreen(new PvpHelperScreen(Component.literal("PVP & Build Helper Menu")));
                }
            }
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            GuiGraphics graphics = event.getGuiGraphics();
            Font font = mc.font;

            if (PvpBuild.armorHudEnabled) {
                int yOffset = 10;
                graphics.drawString(font, "=== МОНИТОР БРОНИ ===", 10, yOffset, 0x00FF00, true);
                yOffset += 12;

                for (ItemStack armorItem : mc.player.getArmorSlots()) {
                    if (!armorItem.isEmpty()) {
                        int maxDamage = armorItem.getMaxDamage();
                        int currentDamage = maxDamage - armorItem.getDamageValue();
                        String itemText = armorItem.getHoverName().getString() + ": " + currentDamage + "/" + maxDamage;

                        int color = 0x00FF00;
                        if (maxDamage > 0) {
                            float durabilityRatio = (float) currentDamage / maxDamage;
                            if (durabilityRatio < 0.25f) {
                                color = 0xFF0000;
                            } else if (durabilityRatio < 0.60f) {
                                color = 0xFFFF00;
                            }
                        }

                        graphics.drawString(font, itemText, 10, yOffset, color, true);
                        yOffset += 12;
                    }
                }
            }

            if (PvpBuild.reachDisplayEnabled) {
                HitResult hit = mc.hitResult;
                if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) hit;
                    if (entityHit.getEntity() instanceof Player) {
                        double distance = mc.player.position().distanceTo(entityHit.getEntity().position());
                        String reachText = String.format("Reach Дистанция: %.2f блоков", distance);

                        int screenWidth = mc.getWindow().getGuiScaledWidth();
                        int screenHeight = mc.getWindow().getGuiScaledHeight();
                        int textWidth = font.width(reachText);

                        graphics.drawString(font, reachText, (screenWidth / 2) - (textWidth / 2), (screenHeight / 2) - 22, 0xFFAA00, true);
                    }
                }
            }

            if (PvpBuild.blockOverlayEnabled) {
                HitResult hit = mc.hitResult;
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    String blockText = "Target Block: " + blockHit.getBlockPos().toShortString();
                    graphics.drawString(font, blockText, 10, mc.getWindow().getGuiScaledHeight() - 15, 0x00FFFF, true);
                }
            }
            
            if (PvpBuild.espEnabled) {
                int activePlayers = mc.level != null ? mc.level.players().size() - 1 : 0;
                graphics.drawString(font, "Игроков рядом: " + activePlayers, 10, mc.getWindow().getGuiScaledHeight() - 27, 0xFF0000, true);
            }
        }
    }
}