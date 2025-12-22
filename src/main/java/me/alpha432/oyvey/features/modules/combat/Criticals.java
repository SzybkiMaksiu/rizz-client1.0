package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.event.system.Subscribe;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

public class Criticals extends Module {
    private final Setting<Mode> mode = enumSetting("Mode", Mode.GRIM_V2);
    private final Setting<Boolean> tinyJump = bset("TinyJump", false);

    private enum Mode {
        PACKET, GRIM, GRIM_V2, GRIM_V3, STRICT, NONE
    }

    public Criticals() {
        super("Bomby Tromby", "Makes you do critical hits", Category.COMBAT);
    }

    @Subscribe
    private void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundInteractPacket packet && packet.action.getType() == ServerboundInteractPacket.ActionType.ATTACK) {
            Entity entity = mc.level.getEntity(packet.entityId);
            
            // Walidacja celu (pomiń krytyki na kryształach i gdy gracz jest w powietrzu)
            if (entity == null || entity instanceof EndCrystal || !mc.player.isOnGround() || !(entity instanceof LivingEntity)) return;

            if (mode.getValue() == Mode.NONE) return;

            // Opcja wizualnego/fizycznego podskoku (niektóre AC tego wymagają)
            if (tinyJump.getValue()) {
                mc.player.push(0, 0.1, 0);
            }

            doCrit(entity);
        }
    }

    private void doCrit(Entity entity) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        switch (mode.getValue()) {
            case PACKET:
                sendPos(x, y + 0.11, z, false);
                sendPos(x, y, z, false);
                break;
                
            case GRIM: // Podstawowy Grim bypass
                sendPos(x, y + 0.0000001, z, false);
                sendPos(x, y, z, false);
                break;

            case GRIM_V2: // Bardziej stabilny na 1.19+
                sendPos(x, y + 0.01, z, false);
                sendPos(x, y, z, false);
                break;

            case GRIM_V3: // Tryb "Delayed" pod Grim
                sendPos(x, y + 0.0006, z, false);
                sendPos(x, y + 0.0004, z, false);
                break;

            case STRICT: // Pod antycheaty typu Vulcan/AAC
                sendPos(x, y + 0.05, z, false);
                sendPos(x, y, z, false);
                sendPos(x, y + 0.01, z, false);
                sendPos(x, y, z, false);
                break;
        }
        mc.player.crit(entity);
    }

    private void sendPos(double x, double y, double z, boolean onGround) {
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().name();
    }
}
