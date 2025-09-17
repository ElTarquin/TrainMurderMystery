package dev.doctor4t.trainmurdermystery.index;

import dev.doctor4t.ratatouille.util.registrar.ItemRegistrar;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.item.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public interface TMMItems {
    ItemRegistrar registrar = new ItemRegistrar(TMM.MOD_ID);

    RegistryKey<ItemGroup> BUILDING_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, TMM.id("building"));
    RegistryKey<ItemGroup> DECORATION_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, TMM.id("decoration"));
    RegistryKey<ItemGroup> EQUIPMENT_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, TMM.id("equipment"));

    Item KEY = registrar.create("key", new KeyItem(new Item.Settings().maxCount(1)), EQUIPMENT_GROUP);
    Item LOCKPICK = registrar.create("lockpick", new LockpickItem(new Item.Settings().maxCount(1)), EQUIPMENT_GROUP);
    Item KNIFE = registrar.create("knife", new KnifeItem(new Item.Settings().maxCount(1)), EQUIPMENT_GROUP);
    Item REVOLVER = registrar.create("revolver", new RevolverItem(new Item.Settings().maxCount(1)), EQUIPMENT_GROUP);
    Item BODY_BAG = registrar.create("body_bag", new BodyBagItem(new Item.Settings().maxCount(1)), EQUIPMENT_GROUP);
    Item LETTER = registrar.create("letter", new Item(new Item.Settings().maxCount(1)), EQUIPMENT_GROUP);

    static void initialize() {
        registrar.registerEntries();

        Registry.register(Registries.ITEM_GROUP, BUILDING_GROUP, FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.trainmurdermystery.building"))
                .icon(() -> new ItemStack(TMMBlocks.TARNISHED_GOLD_PILLAR))
                .build());
        Registry.register(Registries.ITEM_GROUP, DECORATION_GROUP, FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.trainmurdermystery.decoration"))
                .icon(() -> new ItemStack(TMMBlocks.TARNISHED_GOLD_VENT_SHAFT))
                .build());
        Registry.register(Registries.ITEM_GROUP, EQUIPMENT_GROUP, FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.trainmurdermystery.equipment"))
                .icon(() -> new ItemStack(TMMItems.KEY))
                .build());
    }
}
