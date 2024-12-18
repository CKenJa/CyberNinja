package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.item.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cyninja.MODID);

    public static final DeferredItem<NinjaArmorItem> NINJA_HELMET = ITEMS.register("ninja_helmet", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.HELMET, (new Item.Properties()).durability(ArmorItem.Type.HELMET.getDurability(22))));
    public static final DeferredItem<NinjaArmorItem> NINJA_CHESTPLATE = ITEMS.register("ninja_chestplate", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.CHESTPLATE, (new Item.Properties()).durability(ArmorItem.Type.CHESTPLATE.getDurability(22))));
    public static final DeferredItem<NinjaArmorItem> NINJA_LEGGINGS = ITEMS.register("ninja_leggings", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.LEGGINGS, (new Item.Properties()).durability(ArmorItem.Type.LEGGINGS.getDurability(22))));
    public static final DeferredItem<NinjaArmorItem> NINJA_BOOTS = ITEMS.register("ninja_boots", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.BOOTS, (new Item.Properties()).durability(ArmorItem.Type.BOOTS.getDurability(22))));
    public static final DeferredItem<Item> SHURIKEN = ITEMS.register("shuriken", () -> new ShurikenItem((new Item.Properties())));
    public static final DeferredItem<Item> SMOKE_BOMB = ITEMS.register("smoke_bomb", () -> new SmokebombItem((new Item.Properties())));
    //createAttributesの引数は、tier、攻撃力補正、攻撃速度補正
    public static final DeferredItem<Item> KATANA = ITEMS.register("katana", () -> new KatanaItem(new Item.Properties().durability(100).attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F))));
    public static final DeferredItem<Item> CHAIN_SICKLE = ITEMS.register("chain_sickle", () -> new ChainAndSickleItem(new Item.Properties().durability(200)));

    public static final DeferredItem<Item> CYBER_NINJA_SPAWN_EGG = ITEMS.register("cyber_ninja_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.CYBER_ILLAGER, 0x8E9393, 0x3C3C6F, new Item.Properties()));


    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    private static final Component ARMOR_TRIM_APPLIES_TO = Component.translatable(
                    Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.applies_to"))
            )
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component ARMOR_TRIM_INGREDIENTS = Component.translatable(
                    Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.ingredients"))
            )
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component ARMOR_TRIM_BASE_SLOT_DESCRIPTION = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.base_slot_description"))
    );
    private static final Component ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.additions_slot_description"))
    );

    public static final DeferredItem<Item> CYBER_TRIM_SMITHING_TEMPLATE = ITEMS.register("cyber_trim_smithing_template", () -> createArmorTrimTemplate(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "cyber")));


    public static SmithingTemplateItem createArmorTrimTemplate(ResourceLocation p_266880_, FeatureFlag... p_334025_) {
        return new SmithingTemplateItem(
                ARMOR_TRIM_APPLIES_TO,
                ARMOR_TRIM_INGREDIENTS,
                Component.translatable(Util.makeDescriptionId("trim_pattern", p_266880_)).withStyle(TITLE_FORMAT),
                ARMOR_TRIM_BASE_SLOT_DESCRIPTION,
                ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION,
                createTrimmableArmorIconList(),
                createTrimmableMaterialIconList(),
                p_334025_
        );
    }

    private static final ResourceLocation EMPTY_SLOT_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_INGOT = ResourceLocation.withDefaultNamespace("item/empty_slot_ingot");
    private static final ResourceLocation EMPTY_SLOT_REDSTONE_DUST = ResourceLocation.withDefaultNamespace("item/empty_slot_redstone_dust");
    private static final ResourceLocation EMPTY_SLOT_QUARTZ = ResourceLocation.withDefaultNamespace("item/empty_slot_quartz");
    private static final ResourceLocation EMPTY_SLOT_EMERALD = ResourceLocation.withDefaultNamespace("item/empty_slot_emerald");
    private static final ResourceLocation EMPTY_SLOT_DIAMOND = ResourceLocation.withDefaultNamespace("item/empty_slot_diamond");
    private static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli");
    private static final ResourceLocation EMPTY_SLOT_AMETHYST_SHARD = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");


    private static List<ResourceLocation> createTrimmableArmorIconList() {
        return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS);
    }

    private static List<ResourceLocation> createTrimmableMaterialIconList() {
        return List.of(
                EMPTY_SLOT_INGOT,
                EMPTY_SLOT_REDSTONE_DUST,
                EMPTY_SLOT_LAPIS_LAZULI,
                EMPTY_SLOT_QUARTZ,
                EMPTY_SLOT_DIAMOND,
                EMPTY_SLOT_EMERALD,
                EMPTY_SLOT_AMETHYST_SHARD
        );
    }
}
