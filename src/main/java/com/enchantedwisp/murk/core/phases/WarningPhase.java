package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarningPhase implements PhaseHandler {
    private static final Map<UUID, Integer> warningAnimationFrame = new HashMap<>();
    private static final Map<UUID, Integer> warningAnimationTickCounter = new HashMap<>();

    @Override
    public void onEnter(ServerPlayerEntity player) {
        if (ConfigCache.isWarningTextEnabled() && !PlayerLightTracker.isWarned(player.getUuid())) {
            PlayerLightTracker.setWarned(player.getUuid(), true);
            startWarningAnimation(player.getUuid());
        }
    }

    @Override
    public void onExit(ServerPlayerEntity player) {
        // Stop animation if interrupted (e.g., light up mid-warning)
        clearWarningAnimation(player.getUuid());
    }

    @Override
    public void tick(ServerPlayerEntity player, ServerWorld world) {
        PlayerLightTracker.incrementLowLightTicks(player.getUuid());
        advanceWarningAnimation(player.getUuid(), player);
    }

    public static void startWarningAnimation(UUID id) {
        warningAnimationFrame.put(id, 0);
        warningAnimationTickCounter.put(id, 0);
    }

    public static void advanceWarningAnimation(UUID id, ServerPlayerEntity player) {
        Integer frame = warningAnimationFrame.get(id);
        if (frame == null || frame >= 28) {
            return;
        }

        Integer counter = warningAnimationTickCounter.get(id);
        counter = (counter == null ? 0 : counter) + 1;
        warningAnimationTickCounter.put(id, counter);

        if (counter >= 2) {  // Frame interval
            warningAnimationTickCounter.put(id, 0);

            boolean isGlitchPhase = frame < 28 * 0.4;  // 40% glitchy
            String fullMessage = "An evil presence lurks in the dark nearby...";
            StringBuilder sb = new StringBuilder();
            for (char c : fullMessage.toCharArray()) {
                if (c == ' ') {
                    sb.append(' ');
                } else if (isGlitchPhase && Math.random() < 0.2) {
                    sb.append("§k").append(c).append("§r");
                } else {
                    sb.append(c);
                }
            }

            player.sendMessageToClient(Text.literal(sb.toString()).styled(s -> s.withColor(0xFF5555)), true);

            int newFrame = frame + 1;
            warningAnimationFrame.put(id, newFrame);
        }
    }

    public static void clearWarningAnimation(UUID id) {
        warningAnimationFrame.remove(id);
        warningAnimationTickCounter.remove(id);
    }
}
