package me.mogdop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromakeyMod implements ModInitializer {
    public static final String MOD_ID = "mogdops-chromakey-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Ключ и регистрация Блока Хромакея
    public static final ResourceKey<Block> GREEN_CHROMAKEY_BLOCK_KEY = ResourceKey.create(
        Registries.BLOCK,
        Identifier.fromNamespaceAndPath(MOD_ID, "green_chromakey_block")
    );

    public static final Block GREEN_CHROMAKEY_BLOCK = Registry.register(
        BuiltInRegistries.BLOCK,
        GREEN_CHROMAKEY_BLOCK_KEY,
        new ChromakeyBlock(BlockBehaviour.Properties.of()
            .setId(GREEN_CHROMAKEY_BLOCK_KEY)
            .destroyTime(1.5f)
            .sound(SoundType.STONE)
            .lightLevel(state -> state.getValue(ChromakeyBlock.LIT) ? 15 : 0)
        )
    );

    public static final net.minecraft.world.level.block.entity.BlockEntityType<ChromakeyBlockEntity> CHROMAKEY_BLOCK_ENTITY = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "chromakey_block_entity"),
        new net.minecraft.world.level.block.entity.BlockEntityType<>(ChromakeyBlockEntity::new, java.util.Set.of(GREEN_CHROMAKEY_BLOCK)));

    // Ключ и регистрация Предмета блока
    public static final ResourceKey<Item> GREEN_CHROMAKEY_BLOCK_ITEM_KEY = ResourceKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(MOD_ID, "green_chromakey_block")
    );

    public static final Item GREEN_CHROMAKEY_BLOCK_ITEM = Registry.register(
        BuiltInRegistries.ITEM,
        GREEN_CHROMAKEY_BLOCK_ITEM_KEY,
        new BlockItem(
            GREEN_CHROMAKEY_BLOCK,
            new Item.Properties()
                .setId(GREEN_CHROMAKEY_BLOCK_ITEM_KEY)
                .useBlockDescriptionPrefix() // Заменяет useBlockPrefixedTranslationKey
        )
    );

    // Ключ и регистрация Предмета Контроллера
    public static final ResourceKey<Item> CHROMAKEY_CONTROLLER_KEY = ResourceKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(MOD_ID, "chromakey_controller")
    );

    public static final Item CHROMAKEY_CONTROLLER = Registry.register(
        BuiltInRegistries.ITEM,
        CHROMAKEY_CONTROLLER_KEY,
        new ChromakeyControllerItem(
            new Item.Properties()
                .setId(CHROMAKEY_CONTROLLER_KEY)
                .stacksTo(1)
        )
    );

    // Ключи и регистрация Процессора и Заготовки
    public static final ResourceKey<Item> CHROMAKEY_PROCESSOR_KEY = ResourceKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(MOD_ID, "chromakey_processor")
    );

    public static final Item CHROMAKEY_PROCESSOR = Registry.register(
        BuiltInRegistries.ITEM,
        CHROMAKEY_PROCESSOR_KEY,
        new Item(
            new Item.Properties()
                .setId(CHROMAKEY_PROCESSOR_KEY)
        )
    );

    public static final ResourceKey<Item> CHROMAKEY_BLANK_KEY = ResourceKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(MOD_ID, "chromakey_blank")
    );

    public static final Item CHROMAKEY_BLANK = Registry.register(
        BuiltInRegistries.ITEM,
        CHROMAKEY_BLANK_KEY,
        new Item(
            new Item.Properties()
                .setId(CHROMAKEY_BLANK_KEY)
        )
    );

    // Вкладка творческого режима
    public static final ResourceKey<CreativeModeTab> CHROMAKEY_ITEM_GROUP_KEY = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(MOD_ID, "chromakey_item_group")
    );

    public static final CreativeModeTab CHROMAKEY_ITEM_GROUP = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        CHROMAKEY_ITEM_GROUP_KEY,
        FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(GREEN_CHROMAKEY_BLOCK_ITEM))
            .title(Component.translatable("itemGroup.mogdops-chromakey-mod.chromakey_item_group"))
            .displayItems((context, entries) -> {
                entries.accept(GREEN_CHROMAKEY_BLOCK_ITEM);
                entries.accept(CHROMAKEY_CONTROLLER);
                entries.accept(CHROMAKEY_PROCESSOR);
                entries.accept(CHROMAKEY_BLANK);
            })
            .build()
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing MogDops Chromakey Mod for Minecraft 26.3...");
        ChromakeyConfig.load();
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay().register(OpenColorScreenPayload.ID, OpenColorScreenPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.serverboundPlay().register(ApplyColorPayload.ID, ApplyColorPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(ApplyColorPayload.ID, (payload, context) -> {
            var player = context.player();
            var world = player.level();
            var pos = payload.pos();
            if (player.isSpectator() || player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 64
                || (player.getMainHandItem().getItem() != CHROMAKEY_CONTROLLER && player.getOffhandItem().getItem() != CHROMAKEY_CONTROLLER)
                || !world.hasChunkAt(pos)) return;
            if (world.getBlockState(pos).getBlock() instanceof ChromakeyBlock block) {
                block.propagateState(world, pos, null, payload.rgb() & 0xFFFFFF);
            }
        });
    }
}