package highfire1.diceplugin.diceplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class Diceplugin extends JavaPlugin {

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {
        if (command.getName().equalsIgnoreCase("roll")) {

            if (args.length > 1) {
                sender.sendMessage("Too many parameters!");
            } else if (args.length == 0) {
                sender.sendMessage("At least 1 parameter required!");
            } else {
                Random random_generator = new Random();
                String[] out_msg = diceparser.parsedice(args[0], random_generator);
                for (String line : out_msg){
                    sender.sendMessage(line);
                }
            }
            return true;
        }
        return false;
    }
}
