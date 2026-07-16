package me.mogdop;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
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

        if (state.getBlock() instanceof ChromakeyBlock) {
            if (!world.isClient() && context.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
                // Отправляем пакет клиенту для открытия меню и передаем позицию блока
                ServerPlayNetworking.send(serverPlayer, new OpenColorScreenPayload(pos));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}