package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;

public class NameTags extends Module {
    // Ustawienia konfiguracji
    private final Setting<Float> scale = num("Scale", 1.0f, 0.1f, 5.0f);
    private final Setting<Boolean> name = bset("Name", true);
    private final Setting<Boolean> enchantments = bset("Enchants", true);
    private final Setting<Boolean> ping = bset("Ping", true);
    private final Setting<Boolean> gamemode = bset("Gamemode", true);
    private final Setting<Boolean> effects = bset("Effects", true);

    public NameTags() {
        super("NameTagsy", "Better nametags for players", Category.RENDER);
    }

    @Override
    public void onRender3D() {
        if (nullCheck()) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;

            double x = player.getX() - mc.getEntityRenderDispatcher().camera.getPosition().x;
            double y = player.getY() + player.getBbHeight() + 0.5 - mc.getEntityRenderDispatcher().camera.getPosition().y;
            double z = player.getZ() - mc.getEntityRenderDispatcher().camera.getPosition().z;

            renderNameTag(player, x, y, z);
        }
    }

    private void renderNameTag(Player player, double x, double y, double z) {
        PoseStack matrixStack = new PoseStack();
        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        
        // Obracanie w stronę gracza (Billboarding)
        Quaternion rotation = mc.getEntityRenderDispatcher().cameraOrientation();
        matrixStack.mulPose(rotation);
        
        // Skalowanie
        float f = scale.getValue() * 0.025f;
        matrixStack.scale(-f, -f, f);

        Font font = mc.font;
        String displayTag = buildTag(player);
        float width = font.width(displayTag) / 2f;

        // Renderowanie tła i tekstu
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        font.drawInBatch(displayTag, -width, 0, -1, false, matrixStack.last().pose(), bufferSource, false, 0, 15728880);

        // Wyświetlanie efektów pod nazwą
        if (effects.getValue()) {
            float offset = 10;
            for (MobEffectInstance effect : player.getActiveEffects()) {
                String effectStr = effect.getEffect().getDisplayName().getString() + " " + (effect.getDuration() / 20) + "s";
                font.drawInBatch(effectStr, -font.width(effectStr) / 2f, offset, -1, false, matrixStack.last().pose(), bufferSource, false, 0, 15728880);
                offset += 10;
            }
        }

        // Logika Enchantów (opcjonalnie nad głową)
        if (enchantments.getValue()) {
            // Tutaj można dodać pętlę sprawdzającą player.getArmorSlots() i ich enchanty
        }

        matrixStack.popPose();
    }

    private String buildTag(Player player) {
        StringBuilder sb = new StringBuilder();

        if (name.getValue()) {
            sb.append(player.getGameProfile().getName()).append(" ");
        }

        if (ping.getValue() && mc.getConnection().getPlayerInfo(player.getUUID()) != null) {
            sb.append("[").append(mc.getConnection().getPlayerInfo(player.getUUID()).getLatency()).append("ms] ");
        }

        if (gamemode.getValue() && mc.getConnection().getPlayerInfo(player.getUUID()) != null) {
            sb.append("[").append(mc.getConnection().getPlayerInfo(player.getUUID()).getGameMode().getName()).append("] ");
        }

        return sb.toString().trim();
    }
}
