package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class Scaffold extends Module {
    // Ustawienia modułu
    private final Setting<RotationMode> rotation = enumSetting("Rotation", RotationMode.GRIM);
    private final Setting<Boolean> swing = bset("Swing Arm", true);

    private enum RotationMode {
        NONE, GRIM, NORMAL
    }

    public Scaffold() {
        super("Scaffold", "Automatically places blocks under you", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        // Znajdź blok pod graczem (uwzględniając niewielki margines)
        BlockPos pos = new BlockPos(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ());
        
        if (mc.level.getBlockState(pos).isAir()) {
            int slot = getBlockSlot();
            if (slot == -1) return; // Brak bloków w hotbarze

            int lastSlot = mc.player.getInventory().selected;
            mc.player.getInventory().selected = slot;

            // Logika rotacji
            applyRotations(pos);

            // Mechanizm stawiania bloku (bypassowanie delayów)
            placeBlock(pos);

            if (swing.getValue()) mc.player.swing(InteractionHand.MAIN_HAND);
            mc.player.getInventory().selected = lastSlot;
        }
    }

    private void applyRotations(BlockPos pos) {
        if (rotation.getValue() == RotationMode.NONE) return;

        float[] rots = calculateRotations(pos);
        
        if (rotation.getValue() == RotationMode.GRIM) {
            // Grim Bypass często wymaga wysyłania pakietów rotacji przed interakcją
            mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(rots[0], rots[1], mc.player.isOnGround()));
        } else if (rotation.getValue() == RotationMode.NORMAL) {
            mc.player.setYRot(rots[0]);
            mc.player.setXRot(rots[1]);
        }
    }

    private void placeBlock(BlockPos pos) {
        // Prosta logika stawiania bloku na podstawie kierunku
        Vec3 hitVec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, 
            new net.minecraft.world.phys.BlockHitResult(hitVec, Direction.UP, pos, false));
    }

    private int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    private float[] calculateRotations(BlockPos pos) {
        // Podstawowy algorytm obliczania kąta patrzenia w dół na blok
        double x = pos.getX() + 0.5 - mc.player.getX();
        double y = pos.getY() - (mc.player.getY() + mc.player.getEyeHeight());
        double z = pos.getZ() + 0.5 - mc.player.getZ();
        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(y, dist));
        return new float[]{yaw, pitch};
    }
}
