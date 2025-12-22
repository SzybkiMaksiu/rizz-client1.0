package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import java.awt.Color;

public class Tracers extends Module {
    // Konfiguracja zasięgu i kolorów w zakładce Render
    private final Setting<Float> range = num("Range", 50f, 1f, 256f);
    private final Setting<Integer> red = num("Red", 255, 0, 255);
    private final Setting<Integer> green = num("Green", 255, 0, 255);
    private final Setting<Integer> blue = num("Blue", 255, 0, 255);
    private final Setting<Integer> alpha = num("Alpha", 255, 0, 255);
    private final Setting<Float> thickness = num("Width", 1.0f, 0.1f, 5.0f);

    public Tracers() {
        // Ustawienie kategorii na RENDER
        super("Tracers", "Draws lines to nearby players", Category.RENDER);
    }

    @Override
    public void onRender3D() {
        if (nullCheck()) return;

        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();
        
        // Punkt początkowy: przód kamery (symuluje wyjście linii z celownika/dołu ekranu)
        Vec3 startVec = new Vec3(0, 0, 1)
                .xRot(-(float) Math.toRadians(mc.player.getXRot()))
                .yRot(-(float) Math.toRadians(mc.player.getYRot()))
                .add(cameraPos);

        for (Player target : mc.level.players()) {
            if (target == mc.player) continue;

            // Sprawdzanie dystansu
            float distance = mc.player.distanceTo(target);
            if (distance > range.getValue()) continue;

            // Pozycjonowanie celu względem kamery
            double targetX = target.getX() - cameraPos.x;
            double targetY = target.getY() + (target.getBbHeight() / 2) - cameraPos.y;
            double targetZ = target.getZ() - cameraPos.z;

            // Rysowanie linii
            renderLine(0, 0, 0, targetX, targetY, targetZ);
        }
    }

    private void renderLine(double x, double y, double z, double tx, double ty, double tz) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); 
        RenderSystem.lineWidth(thickness.getValue());

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        
        int r = red.getValue(), g = green.getValue(), b = blue.getValue(), a = alpha.getValue();

        // Linia od środka widoku (0,0,0 w relatywnym renderowaniu 3D) do celu
        buffer.vertex(x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(tx, ty, tz).color(r, g, b, a).endVertex();

        tessel
