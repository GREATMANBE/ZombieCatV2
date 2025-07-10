package zombiecat.client.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import zombiecat.client.module.modules.legit.ESP;

public class CommandESPAdd extends CommandBase {

    private final ESP espModule;

    public CommandESPAdd(ESP espModule) {
        this.espModule = espModule;
    }

    @Override
    public String getName() {
        return "esp";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/esp add <x> <y> <z>";
    }

    @Override
     public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            try {
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                int z = Integer.parseInt(args[3]);

                espModule.addTrackedCoordinate(x, y, z);
                sender.addChatMessage(new ChatComponentText("Added ESP tracked coordinate: " + x + ", " + y + ", " + z));  // Changed here
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText("Invalid coordinates! Use integers."));
            }
        } else {
            sender.addChatMessage(new ChatComponentText("Usage: /esp add <x> <y> <z>"));
        }
    }
}
