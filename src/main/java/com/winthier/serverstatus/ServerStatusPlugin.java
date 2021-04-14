package com.winthier.serverstatus;

import com.winthier.connect.Connect;
import com.winthier.connect.event.ConnectRemoteConnectEvent;
import com.winthier.connect.event.ConnectRemoteDisconnectEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class ServerStatusPlugin extends JavaPlugin implements Listener {
    private final Map<String, ServerConfiguration> servers;

    public ServerStatusPlugin() {
        this.servers = new LinkedHashMap<String, ServerConfiguration>();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadConfiguration();
        this.updateServers();
        this.getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
    }

    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1 && "Load".equalsIgnoreCase(array[0])) {
            this.loadConfiguration();
            commandSender.sendMessage("[ServerStatus] configuration reloaded");
            return true;
        }
        if (array.length == 1 && "Save".equalsIgnoreCase(array[0])) {
            this.saveConfiguration();
            commandSender.sendMessage("[ServerStatus] configuration saved");
            return true;
        }
        if (array.length == 1 && "Update".equalsIgnoreCase(array[0])) {
            this.updateServers();
            commandSender.sendMessage("[ServerStatus] servers updated");
            return true;
        }
        if (array.length == 2 && "On".equalsIgnoreCase(array[0])) {
            final String s2 = array[1];
            this.switchServer(s2, true);
            commandSender.sendMessage("[ServerStatus] " + s2 + " turned on");
            return true;
        }
        if (array.length == 2 && "Off".equalsIgnoreCase(array[0])) {
            final String s3 = array[1];
            this.switchServer(s3, false);
            commandSender.sendMessage("[ServerStatus] " + s3 + " turned off");
            return true;
        }
        return false;
    }

    public void loadConfiguration() {
        this.reloadConfig();
        this.servers.clear();
        final ConfigurationSection configurationSection = this.getConfig().getConfigurationSection("servers");
        try {
            if (configurationSection != null) {
                for (final String s : configurationSection.getKeys(false)) {
                    final ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(s);
                    final ServerConfiguration serverConfiguration = new ServerConfiguration(this, s);
                    serverConfiguration.loadConfiguration(configurationSection2);
                    this.servers.put(s, serverConfiguration);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveConfiguration() {
        final ConfigurationSection section = this.getConfig().createSection("servers");
        for (final String s : this.servers.keySet()) {
            this.servers.get(s).saveConfiguration(section.createSection(s));
        }
        this.saveConfig();
    }

    public void switchServer(final String s, final boolean b) {
        final ServerConfiguration serverConfiguration = this.servers.get(s);
        if (serverConfiguration == null) return;
        if (b) {
            serverConfiguration.turnOn();
        } else {
            serverConfiguration.turnOff();
        }
    }

    public void updateServers() {
        List<String> online = Connect.getInstance().listServers();
        for (final String remote: this.servers.keySet()) {
            if (online.contains(remote)) {
                this.servers.get(remote).turnOn();
            } else {
                this.servers.get(remote).turnOff();
            }
        }
    }

    @EventHandler
    public void onRemoteConnect(final ConnectRemoteConnectEvent event) {
        final ServerConfiguration serverConfiguration = this.servers.get(event.getRemote());
        if (serverConfiguration == null) {
            return;
        }
        serverConfiguration.turnOn();
        if (event.getRemote().equals("cavetale")) {
            List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
            if (players.isEmpty()) return;
            Collections.shuffle(players);
            new BukkitRunnable() {
                @Override public void run() {
                    if (players.isEmpty()) {
                        cancel();
                        return;
                    }
                    Player player = players.remove(players.size() - 1);
                    if (!player.isOnline()) return;
                    sendPlayerServer(player, "cavetale");
                }
            }.runTaskTimer(this, 100L, 10L);
        }
    }

    void sendPlayerServer(Player player, String serverName) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeUTF("Connect");
            dataOutputStream.writeUTF(serverName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        player.sendPluginMessage(this, "BungeeCord", byteArrayOutputStream.toByteArray());
    }

    @EventHandler
    public void onRemoteDisconnect(final ConnectRemoteDisconnectEvent event) {
        final ServerConfiguration serverConfiguration = this.servers.get(event.getRemote());
        if (serverConfiguration == null) {
            return;
        }
        serverConfiguration.turnOff();
    }
}
