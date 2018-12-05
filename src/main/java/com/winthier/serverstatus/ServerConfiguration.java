package com.winthier.serverstatus;

import org.bukkit.block.BlockState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.LinkedHashMap;
import org.bukkit.block.Block;
import java.util.Iterator;
import org.bukkit.Material;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;

public class ServerConfiguration {
    private final ServerStatusPlugin plugin;
    private final String name;
    private boolean up;
    private List<BlockConfiguration> blocks;
    private String onAnnouncement;
    private String offAnnouncement;

    public ServerConfiguration(final ServerStatusPlugin plugin, final String name) {
        this.blocks = new ArrayList<BlockConfiguration>();
        this.plugin = plugin;
        this.name = name;
    }

    public void loadConfiguration(final ConfigurationSection configurationSection) throws Exception {
        final Iterator<Map<?,?>> iterator = configurationSection.getMapList("blocks").iterator();
        while (iterator.hasNext()) {
            final ConfigurationSection section = new YamlConfiguration().createSection("tmp", (Map)iterator.next());
            final String[] split = section.getString("Location").split(",");
            final Block block = this.plugin.getServer().getWorld(split[0]).getBlockAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            Material material = Material.GLOWSTONE;
            byte b = 0;
            Material material2 = Material.WHITE_STAINED_GLASS;
            byte b2 = 15;
            if (section.isString("Up")) {
                final String[] split2 = section.getString("Up").split(":");
                material = Material.matchMaterial(split2[0]);
                b = (byte)Integer.parseInt(split2[1]);
            }
            if (section.isString("Down")) {
                final String[] split3 = section.getString("Down").split(":");
                material2 = Material.matchMaterial(split3[0]);
                b2 = (byte)Integer.parseInt(split3[1]);
            }
            this.blocks.add(new BlockConfiguration(block, material, b, material2, b2));
        }
        final ConfigurationSection configurationSection2;
        if (null != (configurationSection2 = configurationSection.getConfigurationSection("announcement"))) {
            this.onAnnouncement = configurationSection2.getString("Up");
            this.offAnnouncement = configurationSection2.getString("Down");
        }
    }

    public void saveConfiguration(final ConfigurationSection configurationSection) {
        final ArrayList<LinkedHashMap<String, String>> list = new ArrayList<LinkedHashMap<String, String>>();
        for (final BlockConfiguration blockConfiguration : this.blocks) {
            final LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
            final Block block = blockConfiguration.block;
            linkedHashMap.put("Location", String.format("%s,%d,%d,%d", block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
            linkedHashMap.put("Up", String.format("%s:%d", blockConfiguration.onMaterial.name(), blockConfiguration.onData));
            linkedHashMap.put("Down", String.format("%s:%d", blockConfiguration.offMaterial.name(), blockConfiguration.offData));
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

    public void turnOn() {
        if (this.up) {
            return;
        }
        this.up = true;
        this.plugin.getLogger().info("Turning on server " + this.name);
        for (final BlockConfiguration blockConfiguration : this.blocks) {
            final BlockState state = blockConfiguration.block.getState();
            state.setType(blockConfiguration.onMaterial);
            state.setRawData(blockConfiguration.onData);
            state.update(true, false);
        }
        if (this.onAnnouncement != null) {
            for (Player player: this.plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.onAnnouncement));
            }
        }
    }

    public void turnOff() {
        if (!this.up) {
            return;
        }
        this.up = false;
        this.plugin.getLogger().info("Turning off server " + this.name);
        for (final BlockConfiguration blockConfiguration : this.blocks) {
            final BlockState state = blockConfiguration.block.getState();
            state.setType(blockConfiguration.offMaterial);
            state.setRawData(blockConfiguration.offData);
            state.update(true, false);
        }
        if (this.offAnnouncement != null) {
            for (Player player: this.plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.offAnnouncement));
            }
        }
    }
}
