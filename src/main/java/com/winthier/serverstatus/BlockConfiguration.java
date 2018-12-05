// 
// Decompiled by Procyon v0.5.30
// 

package com.winthier.serverstatus;

import org.bukkit.Material;
import org.bukkit.block.Block;

class BlockConfiguration
{
    public final Block block;
    public final Material onMaterial;
    public final Material offMaterial;
    public final byte onData;
    public final byte offData;
    
    public BlockConfiguration(final Block block, final Material onMaterial, final byte onData, final Material offMaterial, final byte offData) {
        this.block = block;
        this.onMaterial = onMaterial;
        this.onData = onData;
        this.offMaterial = offMaterial;
        this.offData = offData;
    }
}
