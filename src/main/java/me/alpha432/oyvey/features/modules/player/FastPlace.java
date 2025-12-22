package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.world.item.Items;

public class FastPlace extends Module {
    public FastPlace() {
        // Zaktualizowałem opis, aby pasował do nowych przedmiotów
        super("FastPlace", "Makes you place cobwebs and planks faster", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        // Sprawdza, czy gracz trzyma pajęczynę LUB dębowe deski
        if (mc.player.isHolding(Items.COBWEB) || mc.player.isHolding(Items.OAK_PLANKS)) {
            mc.rightClickDelay = 0;
        }
    }
}
