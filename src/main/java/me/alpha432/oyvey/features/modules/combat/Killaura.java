package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;

public class Killaura extends Module {
    private final Setting<Float> range = num("Range", 3.8f, 1f, 6f);
    private final Setting<Boolean> rotate = bset("Rotate", true);
    private final Setting<Boolean> onlyPlayers = bset("OnlyPlayers", true);

    public Killaura() {
        super("AtakAura", "Attacks entities for you", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        Player target = getTarget();
        if (target == null) return;

        // Rotacje pod Grim (płynne patrzenie na cel)
        if (rotate.getValue()) {
            lookAtEntity(target);
        }

        // Atakuj tylko gdy cooldown broni na to pozwala (maksymalny Damage)
        if (mc.player.getAttackStrengthScale(0.5f) >= 0.9f) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private Player getTarget() {
        return mc.level.players().stream()
                .filter(p -> p != mc.player && !p.isDeadOrDying())
                .filter(p -> mc.player.distanceTo(p) <= range.getValue())
                .findFirst().orElse(null);
    }

    private void lookAtEntity(Player entity) {
        double diffX = entity.getX() - mc.player.getX();
        double diffY = entity.getEyeY() - (mc.player.getY() + mc.player.getEyeHeight());
        double diffZ = entity.getZ() - mc.player.getZ();
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));
        
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }
}
