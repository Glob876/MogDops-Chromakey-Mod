package me.mogdop;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChromakeyControllerItem extends Item {
    public ChromakeyControllerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        if (state.getBlock() instanceof ChromakeyBlock chromakeyBlock) {
            if (!world.isClient()) {
                ChromakeyColor currentColor = state.get(ChromakeyBlock.COLOR);
                ChromakeyColor nextColor = switch (currentColor) {
                    case GREEN -> ChromakeyColor.BLUE;
                    case BLUE -> ChromakeyColor.RED;
                    case RED -> ChromakeyColor.GREEN;
                };

                BlockState newState = state.with(ChromakeyBlock.COLOR, nextColor);
                world.setBlockState(pos, newState);
                chromakeyBlock.propagateState(world, pos, newState); // Перекрашиваем всю стену!

                if (player != null) {
                    player.sendMessage(Text.translatable("message.mogdops-chromakey-mod.color_changed", nextColor.asString()).formatted(Formatting.GREEN), true);
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}