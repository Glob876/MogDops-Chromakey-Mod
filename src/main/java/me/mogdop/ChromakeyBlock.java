package me.mogdop;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ChromakeyBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<ChromakeyColor> COLOR = EnumProperty.create("color", ChromakeyColor.class);

    public ChromakeyBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(COLOR, ChromakeyColor.GREEN));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Если в любой из рук контроллер, передаем управление ему
        if (player.getMainHandItem().getItem() instanceof ChromakeyControllerItem ||
            player.getOffhandItem().getItem() instanceof ChromakeyControllerItem) {
            return InteractionResult.PASS;
        }

        // Включаем/выключаем только если игрок сидит (Shift)
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                BlockState newState = state.cycle(LIT);
                level.setBlock(pos, newState, 3);
                propagateState(level, pos, newState); // Распространяем сигнал на соседние блоки
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        // Если игрок зажал Shift, блок ломается мгновенно (даже рукой)
        if (player.isShiftKeyDown()) {
            return 1.0f;
        }
        return super.getDestroyProgress(state, player, world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, COLOR);
    }

    // Алгоритм поиска в ширину (Flood Fill) для одновременного переключения стакающихся блоков
    public void propagateState(Level world, BlockPos startPos, BlockState targetState) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = world.getBlockState(current);

            if (currentState.getBlock() == this) {
                boolean needsUpdate = currentState.getValue(LIT) != targetState.getValue(LIT) ||
                                      currentState.getValue(COLOR) != targetState.getValue(COLOR);

                if (needsUpdate) {
                    world.setBlock(current, currentState
                        .setValue(LIT, targetState.getValue(LIT))
                        .setValue(COLOR, targetState.getValue(COLOR)),
                        3
                    );
                }

                // Проверяем все 6 направлений вокруг блока
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && world.getBlockState(neighbor).getBlock() == this) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }
}