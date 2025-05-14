package com.minecraft.forge.veinmining;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = VeinMiningMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ToolBehaviourHandler {
    private static final Queue<DecayEntry> decayQueue = new LinkedList<>();

    private static class DecayEntry {
        final Level level;
        final BlockPos pos;

        DecayEntry(Level level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }
    }


    @SubscribeEvent
    public static void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getMainHandItem();
        BlockState state = event.getState();
        Level level = (Level) event.getLevel();

        if (event.getPlayer().isCreative()) return; // check gamemode
        if (!tool.isCorrectToolForDrops(state)) return; // check tool is correct

        if (state.is(ModBlockTags.WOOD)) {
            List<BlockPos> matchingBlocks = getMatchingBlocks(event.getPos(), state, level, true);
            List<DecayEntry> leaves = new ArrayList<>();
            for (BlockPos pos : matchingBlocks) {
                if (level.getBlockState(pos).is(ModBlockTags.WOOD)) handleBreaking(level, pos, event.getPlayer(), tool);
                else leaves.add(new DecayEntry(level, pos));
            }
            VeinMiningMod.logInfo("Amount of Leaves: " + leaves.size());
            decayQueue.addAll(leaves);

        } else if (state.is(ModBlockTags.ORES)) {
            List<BlockPos> matchingBlocks = getMatchingBlocks(event.getPos(), state, level, false);
            handleBreaking(level, matchingBlocks, event.getPlayer(), tool);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !decayQueue.isEmpty()) {
            DecayEntry entry = decayQueue.poll();
            handleDecay(entry.level, entry.pos);
        }
    }


    public static void handleBreaking(Level level, List<BlockPos> matchingBlocks, Player player, ItemStack tool) {
        for (BlockPos pos : matchingBlocks) {
            handleBreaking(level, pos, player, tool);
        }
    }

    public static void handleBreaking(Level level, BlockPos pos, Player player, ItemStack tool) {
        if (tool.getCount() <= 0) return;
        VeinMiningMod.logInfo("Breaking: " + pos.toShortString());
        breakBlock(level, pos, tool);
        tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    public static void handleDecay(Level level, BlockPos pos) {
        Block.dropResources(level.getBlockState(pos), level, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
        VeinMiningMod.logInfo("Decayed: " + pos.toShortString());
    }


    public static BlockState getLeavesOfWood(BlockState wood) {
        Map<Block, Block> LOG_TO_LEAVES = new HashMap<>();
        LOG_TO_LEAVES.put(Blocks.OAK_LOG, Blocks.OAK_LEAVES);
        LOG_TO_LEAVES.put(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES);
        LOG_TO_LEAVES.put(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES);
        LOG_TO_LEAVES.put(Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES);
        LOG_TO_LEAVES.put(Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES);
        LOG_TO_LEAVES.put(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES);
        LOG_TO_LEAVES.put(Blocks.MANGROVE_LOG, Blocks.MANGROVE_LEAVES);
        LOG_TO_LEAVES.put(Blocks.CHERRY_LOG, Blocks.CHERRY_LEAVES);
        return LOG_TO_LEAVES.get(wood.getBlock()).defaultBlockState();
    }

    public static List<BlockPos> getMatchingBlocks(BlockPos pos, BlockState state, Level level, boolean includeLeaves) {
        List<BlockPos> visited = new LinkedList<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            List<BlockPos> neighbours = getSurroundingBlocks(currentPos, state, level);
            if (includeLeaves) neighbours.addAll(getSurroundingBlocks(currentPos, getLeavesOfWood(state), level));
            for (BlockPos neighbour : neighbours) {
                if (visited.contains(neighbour)) continue;
                visited.add(neighbour);
                queue.add(neighbour);
            }
        }

        return visited;
    }

    public static List<BlockPos> getSurroundingBlocks(BlockPos pos, BlockState state, Level level) {
        List<BlockPos> neighbours = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos neighbour = pos.offset(x, y, z);
                    if (neighbour.equals(pos)) continue;
                    if (level.getBlockState(neighbour).is(state.getBlock())) neighbours.add(neighbour);
                }
            }
        }
        return neighbours;
    }

    public static void breakBlock(Level level, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Block.dropResources(state, level, pos, blockEntity, null, tool);

        level.destroyBlock(pos, false);
    }
}