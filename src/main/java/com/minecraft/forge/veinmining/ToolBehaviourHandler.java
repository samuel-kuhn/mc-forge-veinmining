package com.minecraft.forge.veinmining;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Mod.EventBusSubscriber(modid = VeinMiningMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ToolBehaviourHandler {

    @SubscribeEvent
    public static void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getMainHandItem();
        if (event.getPlayer().isCreative()) return; // check gamemode
        if (!event.getState().is(ModBlockTags.ORES) && !event.getState().is(ModBlockTags.WOOD)) return; // check block type
        if (!tool.isCorrectToolForDrops(event.getState())) return; // check tool is correct

        Level level = (Level) event.getLevel();
        List<BlockPos> matchingBlocks = getMatchingBlocks(event.getPos(), event.getState(), level);

        for (BlockPos pos : matchingBlocks) {
            VeinMiningMod.logInfo(pos.toShortString());
            breakBlock(level, pos, tool);
            tool.hurtAndBreak(1, event.getPlayer(), EquipmentSlot.MAINHAND);
            if (tool.getCount() <= 0) return;
        }
    }

    public static List<BlockPos> getMatchingBlocks(BlockPos pos, BlockState state, Level level) {
        List<BlockPos> visited = new LinkedList<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(pos);

        do {
            List<BlockPos> neighbours = getSurroundingBlocks(queue.poll(), state, level);
            for (BlockPos neighbour : neighbours) {
                if (visited.contains(neighbour)) continue;
                visited.add(neighbour);
                queue.add(neighbour);
            }
        } while (!queue.isEmpty());

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
