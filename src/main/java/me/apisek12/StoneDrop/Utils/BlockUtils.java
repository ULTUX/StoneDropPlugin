package me.apisek12.StoneDrop.Utils;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockUtils {
    public static boolean hasTheSameNeighbour(Block block, Material material) {
        for (int xi = -1; xi < 2; xi++) {
            for (int zi = -1; zi < 2; zi++) {
                if (xi * xi + zi * zi == 2) continue;
                if (block.getRelative(xi, 0, zi).getType().equals(material)) return true;
            }
        }
        return false;
    }
}
