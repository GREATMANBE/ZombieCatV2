package zombiecat.client.module.modules.legit;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zombiecat.client.module.Module;

import java.io.*;
import java.util.*;

public class WindowDetect extends Module {

    private final Map<BlockPos, String> trackedCoords = new HashMap<>();
    private final Set<Integer> seenEntityIds = new HashSet<>();

    public WindowDetect() {
        super("WindowDetect", ModuleCategory.legit);
        loadCoordinates();
    }

    private static class BlockPos {
        public final int x, y, z;

        public BlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BlockPos)) return false;
            BlockPos other = (BlockPos) o;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private void loadCoordinates() {
        File file = new File("esp_coords.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 3) continue;

                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    String name = (parts.length > 3)
                            ? line.substring(line.indexOf(parts[2]) + parts[2].length()).trim()
                            : "Unknown";

                    trackedCoords.put(new BlockPos(x, y, z), name);
                } catch (NumberFormatException ignored) {}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.isOn() || Minecraft.getMinecraft().theWorld == null) return;

        for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (!(entity instanceof IMob)) continue;
            if (seenEntityIds.contains(entity.getEntityId())) continue;

            int x = (int) Math.floor(entity.posX);
            int y = (int) Math.floor(entity.posY);
            int z = (int) Math.floor(entity.posZ);
            BlockPos pos = new BlockPos(x, y, z);

            if (trackedCoords.containsKey(pos)) {
                String name = trackedCoords.get(pos);
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new ChatComponentText("Mob spawned at: §a" + name));
                seenEntityIds.add(entity.getEntityId());
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        seenEntityIds.clear();
    }
}
