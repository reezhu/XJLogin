package org.xjcraft.login;

import com.zaxxer.hikari.HikariDataSource;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.xjcraft.login.bean.Account;

import java.sql.*;

public class Manager {
    private XJLogin plugin;
    private HikariDataSource source;

    public Manager(XJLogin plugin, HikariDataSource source) {
        this.plugin = plugin;
        this.source = source;
        try {
            initTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initTable() throws SQLException {
        Connection connection = null;
        try {
            connection = source.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS CrazyLogin_accounts (" +
                            " NAME CHAR ( 255 ) CHARACTER  SET utf8 COLLATE utf8_bin NOT NULL, " +
                            "PASSWORD CHAR ( 255 ) CHARACTER  SET utf8 NOT NULL, " +
                            "ips text CHARACTER  SET latin1 NOT NULL, " +
                            "lastAction TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                            "loginFails INT ( 11 ) NOT NULL DEFAULT '0', " +
                            "passwordExpired bit ( 1 ) NOT NULL DEFAULT '0', " +
                            "PRIMARY KEY ( NAME ), KEY NAME ( NAME ) USING BTREE  " +
                            ") ENGINE = INNODB DEFAULT CHARSET = utf8;");
            statement.execute();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (connection != null)
                connection.close();
        }
    }


    public void login(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            player.chat("输入/login <passcode> 来登陆");
        }

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                Account account = getAccount(player.getName());

                if (validAccount(player, args[0], account)) {
                    String ip = player.getAddress().getHostString();
                    sendPlayer(player);
                    account.setLastAction(new Timestamp(System.currentTimeMillis()));
                    account.setLoginFails(0);
                    if (!account.getIps().contains(ip)) {
                        account.setIps(account.getIps() + (account.getIps().length() > 0 ? "," : "") + ip);
                    }
                } else {
                    if (account == null) return;
                    account.setLoginFails(account.getLoginFails() + 1);
                }
                updateAccount(account);


            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    private void sendPlayer(ProxiedPlayer player) {
        ServerInfo target = ProxyServer.getInstance().getServerInfo("main");
        player.connect(target);
    }

    public void register(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "输入/register <password> 来注册");
        }

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                Account account = getAccount(player.getName());

                if (account != null) {
                    player.sendMessage(ChatColor.YELLOW + "已有账号，请用/login <password>登陆");
                    return;
                }
                account = new Account(player.getName(), args[0]);
                updateAccount(account);
                player.sendMessage(ChatColor.YELLOW + "注册成功，即将转移……");
                sendPlayer(player);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }


    private boolean validAccount(ProxiedPlayer player, String arg, Account account) {
        if (account == null) {
            player.sendMessage(ChatColor.YELLOW + "账号不存在！请使用/register来注册");
            return false;
        }
        if (account.getPasswordExpired()) {
            player.sendMessage(ChatColor.YELLOW + "账号过期！");
            return false;
        }
        if (!account.getPassword().equals(arg)) {
            player.sendMessage(ChatColor.YELLOW + "密码错误！");
            return false;
        }
        player.sendMessage(ChatColor.YELLOW + "登陆成功！正在转移……");
        return true;
    }

    private void updateAccount(Account account) throws SQLException {
        if (account == null) return;
        Connection connection = null;
        try {

            connection = source.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `CrazyLogin_accounts` " +
                    "( `name`, `password`, `ips`, `lastAction`, `loginFails`, `passwordExpired` ) VALUES " +
                    "( ?, ?, ?, ?, ?, ? )  ON DUPLICATE KEY UPDATE `password` = ?, `ips` =?, `lastAction` = ?, `loginFails` = ?, `passwordExpired` = ?;");
            statement.setString(1, account.getName());
            statement.setString(2, account.getPassword());
            statement.setString(3, account.getIps());
            statement.setTimestamp(4, account.getLastAction());
            statement.setInt(5, account.getLoginFails());
            statement.setBoolean(6, account.getPasswordExpired());
            statement.setString(7, account.getPassword());
            statement.setString(8, account.getIps());
            statement.setTimestamp(9, account.getLastAction());
            statement.setInt(10, account.getLoginFails());
            statement.setBoolean(11, account.getPasswordExpired());
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.close();
        }
    }

    private Account getAccount(String name) throws SQLException {
        Connection connection = null;
        try {

            connection = source.getConnection();
            PreparedStatement statement = connection.prepareStatement("select * from CrazyLogin_accounts where name=?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Account(
                        resultSet.getString("name"),
                        resultSet.getString("password"),
                        resultSet.getString("ips"),
                        resultSet.getTimestamp("lastAction"),
                        resultSet.getInt("loginFails"),
                        resultSet.getBoolean("passwordExpired")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (connection != null)
                connection.close();
        }
        return null;
    }

    public void adminCmd(CommandSender sender, String[] args) {
        if (args.length < 1) return;
        switch (args[0]) {
            case "status":
                if (args.length > 1)
                    plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                        try {
                            Account account = getAccount(args[1]);
                            if (account == null) {
                                sender.sendMessage(ChatColor.YELLOW + "玩家不存在!");
                                return;
                            }
                            sender.sendMessage(ChatColor.BLUE + "=====================================================");
                            sender.sendMessage(ChatColor.YELLOW + "玩家：" + account.getName());
                            sender.sendMessage(ChatColor.YELLOW + "上次登陆：" + account.getLastAction());
                            sender.sendMessage(ChatColor.YELLOW + "登陆失败：" + account.getLoginFails());
                            sender.sendMessage(ChatColor.YELLOW + "曾用ip：" + account.getIps());
                            sender.sendMessage(ChatColor.YELLOW + "过期：" + account.getPasswordExpired());
                            sender.sendMessage(ChatColor.BLUE + "=====================================================");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                break;
            case "create":
                if (args.length > 2)
                    plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                        String name = args[1];
                        String pwd = args[2];
                        try {
                            Account account = getAccount(name);
                            if (account != null) {
                                sender.sendMessage(ChatColor.YELLOW + "玩家已存在!");
                                return;
                            }
                            account = new Account(name, pwd);
                            updateAccount(account);
                            sender.sendMessage(ChatColor.YELLOW + "创建成功!");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                break;
        }
    }
}
