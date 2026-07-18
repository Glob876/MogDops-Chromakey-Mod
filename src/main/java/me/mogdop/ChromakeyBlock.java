package me.mogdop;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand; // Добавлен импорт Hand
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ChromakeyBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final EnumProperty<ChromakeyColor> COLOR = EnumProperty.of("color", ChromakeyColor.class);

    public ChromakeyBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(LIT, false).with(COLOR, ChromakeyColor.GREEN));
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChromakeyBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // Исправлено: в 1.20.1 метод onUse() является public и принимает аргумент Hand hand
        if (player.getMainHandStack().getItem() instanceof ChromakeyControllerItem ||
            player.getOffHandStack().getItem() instanceof ChromakeyControllerItem) {
            return ActionResult.PASS;
        }

        if (player.isSneaking()) {
            if (!world.isClient) {
                boolean newLit = !state.get(LIT);
                int existingColor = findExistingCustomColor(world, pos);
                propagateState(world, pos, newLit, existingColor != -1 ? existingColor : null);
            }
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        // Исправлено: в 1.20.1 метод calcBlockBreakingDelta() должен быть public
        if (player.isSneaking()) { return 1.0f; }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, COLOR);
    }

    private int findExistingCustomColor(World world, BlockPos startPos) {
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
                        return color;
                    }
                }

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.offset(dir);
                    if (!visited.contains(neighbor) && world.getBlockState(neighbor).getBlock() == this) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return -1;
    }

    public void propagateState(World world, BlockPos startPos, @Nullable Boolean targetLit, @Nullable Integer targetColor) {
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

                if (targetLit != null && currentState.get(LIT) != targetLit) {
                    world.setBlockState(current, currentState.with(LIT, targetLit));
                }

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