package me.mogdop;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ChromakeyBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<ChromakeyColor> COLOR = EnumProperty.create("color", ChromakeyColor.class);

    public ChromakeyBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(COLOR, ChromakeyColor.GREEN));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChromakeyBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.getMainHandItem().getItem() instanceof ChromakeyControllerItem ||
            player.getOffhandItem().getItem() instanceof ChromakeyControllerItem) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                boolean newLit = !state.getValue(LIT);
                // Ищем уже существующий кастомный цвет в структуре перед активацией
                int existingColor = findExistingCustomColor(world, pos);
                propagateState(world, pos, newLit, existingColor != -1 ? existingColor : null);
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (player.isShiftKeyDown()) { return 1.0f; }
        return super.getDestroyProgress(state, player, world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, COLOR);
    }

    // Вспомогательный алгоритм для поиска первого попавшегося кастомного цвета в структуре
    private int findExistingCustomColor(Level world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = world.getBlockState(current);

            if (currentState.getBlock() == this) {
                BlockEntity be = world.getBlockEntity(current);
                if (be instanceof ChromakeyBlockEntity chromakeyBe) {
                    int color = chromakeyBe.getCustomColor();
                    if (color != -1) {
                        return color; // Цвет найден!
                    }
                }

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && world.getBlockState(neighbor).getBlock() == this) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return -1; // Кастомного цвета в структуре пока нет
    }

    public void propagateState(Level world, BlockPos startPos, @Nullable Boolean targetLit, @Nullable Integer targetColor) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = world.getBlockState(current);

            if (currentState.getBlock() == this) {
                if (targetColor != null) {
                    BlockEntity be = world.getBlockEntity(current);
                    if (be instanceof ChromakeyBlockEntity chromakeyBe) {
                        if (chromakeyBe.getCustomColor() != targetColor) {
                            chromakeyBe.setCustomColor(targetColor);
                        }
                    }
                }

                if (targetLit != null && currentState.getValue(LIT) != targetLit) {
                    world.setBlock(current, currentState.setValue(LIT, targetLit), 3);
                }

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