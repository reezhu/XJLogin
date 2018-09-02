package org.xjcraft.login.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.xjcraft.login.Manager;

public class CommandRegister extends Command {
    private Manager manager;

    public CommandRegister(String name, Manager manager) {
        super(name);
        this.manager = manager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if (((ProxiedPlayer) sender).getServer().getInfo().getName().equalsIgnoreCase("login"))
                manager.register((ProxiedPlayer) sender, args);
        }
    }
}
