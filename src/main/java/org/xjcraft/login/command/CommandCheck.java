package org.xjcraft.login.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.xjcraft.login.Manager;

public class CommandCheck extends Command {
    private Manager manager;

    public CommandCheck(Manager manager) {
        super("xl", "permission.check");
        this.manager = manager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if ((sender instanceof ProxiedPlayer) && ((ProxiedPlayer) sender).getServer().getInfo().getName().equalsIgnoreCase("login"))
            return;
        manager.adminCmd(sender, args);
    }
}
