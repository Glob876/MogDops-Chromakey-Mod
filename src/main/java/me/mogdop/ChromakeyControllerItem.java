package me.mogdop;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ChromakeyControllerItem extends Item {
    public ChromakeyControllerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.getBlock() instanceof ChromakeyBlock) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer, new OpenColorScreenPayload(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
