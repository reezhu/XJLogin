package org.xjcraft.login.manager;

import com.zaxxer.hikari.HikariDataSource;
import org.xjcraft.login.bean.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Manager {

    private HikariDataSource source;

    public Manager(HikariDataSource source) {
        this.source = source;
        try {
            initTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initTable() throws SQLException {
        try (Connection connection = source.getConnection()) {
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
        }
    }


    protected void updateAccount(Account account) {
        if (account == null) return;
        try (Connection connection = source.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("INSERT INTO `CrazyLogin_accounts` " +
                    "( `name`, `password`, `ips`, `lastAction`, `loginFails`, `passwordExpired`, `playerType`, `inviter` ) VALUES " +
                    "( ?, ?, ?, ?, ?, ? , ?, ? )  ON DUPLICATE KEY UPDATE `password` = ?, `ips` =?, `lastAction` = ?, `loginFails` = ?, `passwordExpired` = ?, `playerType` = ?, `inviter` = ?;");
            statement.setString(1, account.getName());
            statement.setString(2, account.getPassword());
            statement.setString(3, account.getIps());
            statement.setTimestamp(4, account.getLastAction());
            statement.setInt(5, account.getLoginFails());
            statement.setBoolean(6, account.getPasswordExpired());
            statement.setInt(7, account.getPlayerType());
            statement.setString(8, account.getInviter());
            statement.setString(9, account.getPassword());
            statement.setString(10, account.getIps());
            statement.setTimestamp(11, account.getLastAction());
            statement.setInt(12, account.getLoginFails());
            statement.setBoolean(13, account.getPasswordExpired());
            statement.setInt(14, account.getPlayerType());
            statement.setString(15, account.getInviter());
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected Account getAccount(String name) {
        try (Connection connection = source.getConnection()) {

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
                        resultSet.getBoolean("passwordExpired"),
                        resultSet.getInt("playerType"),
                        resultSet.getString("inviter")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return null;
    }

    protected List<String> cachePlayerNames() {
        ArrayList<String> list = new ArrayList<>();
        try (Connection connection = source.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("select `name` from CrazyLogin_accounts");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


}
