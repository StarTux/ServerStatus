package com.winthier.serverstatus;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

final class BlockConfiguration {
    final Block block;
    final BlockData onData;
    final BlockData offData;

    BlockConfiguration(final Block block, final BlockData onData, final BlockData offData) {
        this.block = block;
        this.onData = onData;
        this.offData = offData;
    }
}
