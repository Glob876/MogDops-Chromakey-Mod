package me.mogdop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChromakeyMod implements ModInitializer {
    public static final String MOD_ID = "mogdops-chromakey-mod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Identifier OPEN_SCREEN = id("open_color_screen");
    public static final Identifier APPLY_COLOR = id("apply_color");
    public static final Block GREEN_CHROMAKEY_BLOCK = Registry.register(Registry.BLOCK, id("green_chromakey_block"),
        new ChromakeyBlock(AbstractBlock.Settings.of(Material.STONE).strength(1.5f).luminance(state -> state.get(ChromakeyBlock.LIT) ? 15 : 0)));
    public static final BlockEntityType<ChromakeyBlockEntity> CHROMAKEY_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("chromakey_block_entity"),
        BlockEntityType.Builder.create(ChromakeyBlockEntity::new, GREEN_CHROMAKEY_BLOCK).build(null));
    public static final Item GREEN_CHROMAKEY_BLOCK_ITEM = Registry.register(Registry.ITEM, id("green_chromakey_block"), new BlockItem(GREEN_CHROMAKEY_BLOCK, new Item.Settings()));
    public static final Item CHROMAKEY_CONTROLLER = Registry.register(Registry.ITEM, id("chromakey_controller"), new ChromakeyControllerItem(new Item.Settings().maxCount(1)));
    public static final Item CHROMAKEY_PROCESSOR = Registry.register(Registry.ITEM, id("chromakey_processor"), new Item(new Item.Settings()));
    public static final Item CHROMAKEY_BLANK = Registry.register(Registry.ITEM, id("chromakey_blank"), new Item(new Item.Settings()));
    public static final ItemGroup CHROMAKEY_ITEM_GROUP = FabricItemGroupBuilder.create(id("chromakey_item_group"))
        .icon(() -> new ItemStack(GREEN_CHROMAKEY_BLOCK_ITEM)).appendItems(items -> {
            items.add(new ItemStack(GREEN_CHROMAKEY_BLOCK_ITEM)); items.add(new ItemStack(CHROMAKEY_CONTROLLER));
            items.add(new ItemStack(CHROMAKEY_PROCESSOR)); items.add(new ItemStack(CHROMAKEY_BLANK));
        }).build();

    private static Identifier id(String path) { return new Identifier(MOD_ID, path); }

    @Override
    public void onInitialize() {
        ChromakeyConfig.load();
        ServerPlayNetworking.registerGlobalReceiver(APPLY_COLOR, (server, player, handler, buf, responseSender) -> {
            final net.minecraft.util.math.BlockPos pos = buf.readBlockPos();
            final int rgb = buf.readInt() & 0xFFFFFF;
            server.execute(() -> {
                // Only allow editing a loaded block within interaction range while holding the controller.
                if (player.isSpectator() || player.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 64
                    || (player.getMainHandStack().getItem() != CHROMAKEY_CONTROLLER && player.getOffHandStack().getItem() != CHROMAKEY_CONTROLLER)
                    || !player.world.isChunkLoaded(pos)) return;
                BlockState state = player.world.getBlockState(pos);
                if (state.getBlock() instanceof ChromakeyBlock) {
                    ((ChromakeyBlock) state.getBlock()).propagateState(player.world, pos, null, rgb);
                }
            });
        });
    }
}
