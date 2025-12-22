package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;

import java.awt.Color;

public class ESP extends Module {
    private final Setting<Integer> red = num("Red", 255, 0, 255);
    private final Setting<Integer> green = num("Green", 255, 0, 255);
    private final Setting<Integer> blue = num("Blue", 255, 0, 255);
    private final Setting<Integer> alpha = num("Alpha", 100, 0, 255);
    private final Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.1f, 5.0f);

    public ESP() {
        super("ESP", "Highlights players through walls", Category.RENDER);
    }

    // W Twoim kliencie ta metoda powinna być wywoływana z głównego managera renderowania
    public void onRender3D() {
        if (nullCheck()) return;

        for (Player entity : mc.level.players()) {
            if (entity == mc.player) continue; // Nie rysuj ESP na sobie

            // Pobieranie pozycji encji i tworzenie ramki (Hitboxa)
            AABB bb = entity.getBoundingBox()
                .move(-mc.getEntityRenderDispatcher().camera.getPosition().x,
                      -mc.getEntityRenderDispatcher().camera.getPosition().y,
                      -mc.getEntityRenderDispatcher().camera.getPosition().z);

            renderESPBox(bb, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()));
        }
    }

    private void renderESPBox(AABB bb, Color color) {
        // Przygotowanie OpenGL do rysowania w 3D
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // To sprawia, że widzisz przez ściany
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // Rysowanie krawędzi (Outline)
        RenderSystem.lineWidth(lineWidth.getValue());
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        
        //
