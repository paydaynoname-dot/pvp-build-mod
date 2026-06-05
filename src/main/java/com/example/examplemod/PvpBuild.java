package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
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
        }

        @SubscribeEvent
        public static void onWorldRender(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            PoseStack poseStack = event.getPoseStack();
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

            if (PvpBuild.espEnabled) {
                for (Player enemyPlayer : mc.level.players()) {
                    if (enemyPlayer == mc.player) continue;

                    poseStack.pushPose();
                    double x = enemyPlayer.getX() - cameraPos.x;
                    double y = enemyPlayer.getY() - cameraPos.y;
                    double z = enemyPlayer.getZ() - cameraPos.z;

                    AABB rawBox = enemyPlayer.getBoundingBox();
                    AABB localBox = new AABB(
                            rawBox.minX - enemyPlayer.getX() + x,
                            rawBox.minY - enemyPlayer.getY() + y,
                            rawBox.minZ - enemyPlayer.getZ() + z,
                            rawBox.maxX - enemyPlayer.getX() + x,
                            rawBox.maxY - enemyPlayer.getY() + y,
                            rawBox.maxZ - enemyPlayer.getZ() + z
                    );

                    render3DBox(poseStack, localBox, 1.0f, 0.0f, 0.0f, 1.0f);
                    poseStack.popPose();
                }
            }

            if (PvpBuild.blockOverlayEnabled) {
                HitResult hit = mc.hitResult;
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();

                    poseStack.pushPose();
                    double x = blockPos.getX() - cameraPos.x;
                    double y = blockPos.getY() - cameraPos.y;
                    double z = blockPos.getZ() - cameraPos.z;

                    AABB blockBox = new AABB(x, y, z, x + 1.0, y + 1.0, z + 1.0);

                    render3DBox(poseStack, blockBox, 0.0f, 0.8f, 1.0f, 1.0f);
                    poseStack.popPose();
                }
            }
        }

        private static void render3DBox(PoseStack poseStack, AABB box, float r, float g, float b, float a) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            RenderSystem.depthFunc(515);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            var matrix = poseStack.last().pose();

            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).setColor(r, g, b, a);

            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).setColor(r, g, b, a);

            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).setColor(r, g, b, a);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }
}