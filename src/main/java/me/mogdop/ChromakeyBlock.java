package me.mogdop;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ChromakeyBlock extends Block {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final EnumProperty<ChromakeyColor> COLOR = EnumProperty.of("color", ChromakeyColor.class);

    public ChromakeyBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(LIT, false).with(COLOR, ChromakeyColor.GREEN));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Если в любой из рук контроллер, передаем управление ему
        if (player.getMainHandStack().getItem() instanceof ChromakeyControllerItem ||
            player.getOffHandStack().getItem() instanceof ChromakeyControllerItem) {
            return ActionResult.PASS;
        }

        // Включаем/выключаем только если игрок сидит (Shift)
        if (player.isSneaking()) {
            if (!world.isClient()) {
                BlockState newState = state.cycle(LIT);
                world.setBlockState(pos, newState);
                propagateState(world, pos, newState); // Распространяем сигнал на соседние блоки
            }
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, COLOR);
    }

    // Алгоритм поиска в ширину (Flood Fill) для одновременного переключения стакающихся блоков
    public void propagateState(World world, BlockPos startPos, BlockState targetState) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = world.getBlockState(current);

            if (currentState.getBlock() == this) {
                boolean needsUpdate = currentState.get(LIT) != targetState.get(LIT) ||
                                      currentState.get(COLOR) != targetState.get(COLOR);

                if (needsUpdate) {
                    world.setBlockState(current, currentState
                        .with(LIT, targetState.get(LIT))
                        .with(COLOR, targetState.get(COLOR))
                    );
                }

                // Проверяем все 6 направлений вокруг блока
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.offset(dir);
                    if (!visited.contains(neighbor) && world.getBlockState(neighbor).getBlock() == this) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }
}