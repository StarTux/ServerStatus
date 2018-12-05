package com.winthier.serverstatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

final class ServerConfiguration {
    private final ServerStatusPlugin plugin;
    private final String name;
    private boolean up;
    private List<BlockConfiguration> blocks;
    private String onAnnouncement;
    private String offAnnouncement;

    ServerConfiguration(final ServerStatusPlugin plugin, final String name) {
        this.blocks = new ArrayList<BlockConfiguration>();
        this.plugin = plugin;
        this.name = name;
    }

    void loadConfiguration(final ConfigurationSection configurationSection) throws Exception {
        final Iterator<Map<?, ?>> iterator = configurationSection.getMapList("blocks").iterator();
        while (iterator.hasNext()) {
            final ConfigurationSection section = new YamlConfiguration().createSection("tmp", (Map)iterator.next());
            final String[] split = section.getString("Location").split(",");
            final Block block = this.plugin.getServer().getWorld(split[0]).getBlockAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            BlockData onData = Material.GLOWSTONE.createBlockData();
            BlockData offData = Material.BLACK_STAINED_GLASS.createBlockData();
            if (section.isString("Up")) onData = plugin.getServer().createBlockData(section.getString("Up"));
            if (section.isString("Down")) offData = plugin.getServer().createBlockData(section.getString("Down"));
            this.blocks.add(new BlockConfiguration(block, onData, offData));
        }
        final ConfigurationSection configurationSection2;
        if (null != (configurationSection2 = configurationSection.getConfigurationSection("announcement"))) {
            this.onAnnouncement = configurationSection2.getString("Up");
            this.offAnnouncement = configurationSection2.getString("Down");
        }
    }

    void saveConfiguration(final ConfigurationSection configurationSection) {
        final ArrayList<LinkedHashMap<String, String>> list = new ArrayList<LinkedHashMap<String, String>>();
        for (final BlockConfiguration blockConfiguration : this.blocks) {
            final LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
            final Block block = blockConfiguration.block;
            linkedHashMap.put("Location", String.format("%s,%d,%d,%d", block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
            linkedHashMap.put("Up", blockConfiguration.onData.getAsString());
            linkedHashMap.put("Down", blockConfiguration.offData.getAsString());
            list.add(linkedHashMap);
        }
        if (!list.isEmpty()) {
            configurationSection.set("blocks", (Object)list);
        }
        if (this.onAnnouncement != null) {
            configurationSection.set("announcement.Up", (Object)this.onAnnouncement);
        }
        if (this.offAnnouncement != null) {
            configurationSection.set("announcement.Down", (Object)this.offAnnouncement);
        }
    }

    void turnOn() {
        if (this.up) {
            return;
        }
        this.up = true;
        this.plugin.getLogger().info("Turning on server " + this.name);
        for (final BlockConfiguration blockConfiguration : this.blocks) {
            blockConfiguration.block.setBlockData(blockConfiguration.onData, false);
        }
        if (this.onAnnouncement != null) {
            for (Player player: this.plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.onAnnouncement));
            }
        }
    }

    void turnOff() {
        if (!this.up) {
            return;
        }
        this.up = false;
        this.plugin.getLogger().info("Turning off server " + this.name);
        for (final BlockConfiguration blockConfiguration : this.blocks) {
            blockConfiguration.block.setBlockData(blockConfiguration.offData, false);
        }
        if (this.offAnnouncement != null) {
            for (Player player: this.plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.offAnnouncement));
            }
        }
    }
}
