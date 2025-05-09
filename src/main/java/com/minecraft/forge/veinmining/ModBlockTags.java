package com.minecraft.forge.veinmining;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


public class ModBlockTags {
    public static final TagKey<Block> ORES = createTag("ores");
    public static final TagKey<Block> WOOD = createTag("wood");

    private static TagKey<Block> createTag(String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(VeinMiningMod.MOD_ID, name));
    }
}
