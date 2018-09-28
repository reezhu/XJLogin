package org.xjcraft.login.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.xjcraft.login.Bungee;
import org.xjcraft.login.manager.Manager;

public class LoginListener implements Listener {
    private Bungee plugin;
    private Manager manager;

    public LoginListener(Bungee plugin, Manager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void login(ServerConnectedEvent event) {
        if (event.getServer().getInfo().getName().equalsIgnoreCase("login")) {
            event.getPlayer().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("输入/l <password>来登陆").color(ChatColor.YELLOW).create());
            event.getPlayer().sendMessage(ChatMessageType.CHAT, new ComponentBuilder("输入/l <password>来登陆").color(ChatColor.YELLOW).create());
        }
    }
}
