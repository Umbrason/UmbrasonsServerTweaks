package net.umbrason.umbrasonsservertweaks.shulkerenchants;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.collection.DefaultedList;
import net.umbrason.umbrasonsservertweaks.UmbrasonsServerTweaksMod;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ShulkerEnchantsUtility {
    public static boolean isShulkerBox(ItemStack stack) {
        return (stack.getItem() instanceof BlockItem) && ((BlockItem) stack.getItem()).getBlock().getDefaultState().isIn(BlockTags.SHULKER_BOXES);
    }

    public static List<ItemStack> filterShulkerBoxes(List<ItemStack> itemStackStream) {
        return itemStackStream.stream().filter(ShulkerEnchantsUtility::isShulkerBox).collect(Collectors.toList());
    }

    public static List<ItemStack> filterShulkerBoxesWithEnchantments(List<ItemStack> itemStackStream, Enchantment enchantment, int minLevel) {
        return itemStackStream.stream().filter(ShulkerEnchantsUtility::isShulkerBox).filter(box -> EnchantmentHelper.getLevel(enchantment, box) >= minLevel).collect(Collectors.toList());
    }

    public static List<ItemStack> shulkerBoxesFromInventory(Inventory inventory) {
        return IntStream.range(0, inventory.size()).mapToObj(inventory::getStack).collect(Collectors.toList());
    }

    public static int restockFromShulkerBox(int amount, ItemStack stackToRestock, ItemStack shulkerBox) {
        if (amount + stackToRestock.getCount() > stackToRestock.getMaxCount())
            amount = stackToRestock.getMaxCount() - stackToRestock.getCount(); //clamp to maxStackSize
        if (amount == 0) return 0;
        var inventory = getShulkerInventory(shulkerBox);
        if (inventory == null) return 0;
        var matchingStacks = getMatchingStacks(stackToRestock, inventory);
        if (matchingStacks.size() == 0) return 0;
        var i = amount;
        while (matchingStacks.size() > 0) {
            var itemStack = matchingStacks.remove(0);
            if (itemStack.getCount() >= i) {
                itemStack.decrement(i);
                stackToRestock.increment(i);
                break;
            } else {
                var j = itemStack.getCount();
                stackToRestock.increment(j);
                i -= j;
                itemStack.setCount(0);
            }
        }
        //at least one stack MUST have changed, since amount != 0 and matchingStacks.size() > 0 (i.e. at least one stack exists that lost an item now)
        setShulkerInventory(shulkerBox, inventory);
        return amount;
    }

    public static List<ItemStack> getMatchingStacks(ItemStack stack, List<ItemStack> others) {
        return others.stream().filter(other -> ItemStack.canCombine(stack, other)).collect(Collectors.toList());
    }

    public static void setShulkerInventory(ItemStack stack, DefaultedList<ItemStack> inventory) {
        var nbt = new NbtCompound();
        Inventories.writeNbt(nbt, inventory);
        BlockItem.setBlockEntityNbt(stack, BlockEntityType.SHULKER_BOX, nbt);
    }

    public static DefaultedList<ItemStack> getShulkerInventory(ItemStack stack) {
        var nbt = BlockItem.getBlockEntityNbt(stack);
        if (nbt == null)
            return null;
        var size = ShulkerBoxBlockEntity.field_31356;
        var inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
        Inventories.readNbt(nbt, inventory);
        return inventory;
    }

    public static void appendCustomEnchantmentToLore(ItemStack itemStack, Map<Enchantment, Integer> enchantments) {
        for (var customEnchantment : UmbrasonsServerTweaksMod.ENCHANTMENTS)
            if (enchantments.containsKey(customEnchantment)) {
                var nbtRoot = (NbtCompound) itemStack.getOrCreateNbt();
                var display = (NbtCompound) nbtRoot.get(ItemStack.DISPLAY_KEY);
                if (display == null)
                    nbtRoot.put(ItemStack.DISPLAY_KEY, display = new NbtCompound());
                var loreNbtList = (NbtList) display.get(ItemStack.LORE_KEY);
                if (loreNbtList == null)
                    display.put(ItemStack.LORE_KEY, loreNbtList = new NbtList());

                var enchantmentTextNbtString = loreFromEnchantment(customEnchantment, EnchantmentHelper.getLevel(customEnchantment, itemStack));
                var alreadyHas = loreNbtList.stream().map(nbtElement -> ((NbtString) nbtElement)).anyMatch(enchantmentTextNbtString::equals);
                if (!alreadyHas) loreNbtList.add(enchantmentTextNbtString);
            }
    }

    public static NbtString loreFromEnchantment(Enchantment enchantment, int level) {
        var enchantmentText = enchantment.getName(level).getString().split(" ")[0].split("\\.");
        var enchantmentName = enchantmentText[enchantmentText.length - 1];
        enchantmentName = enchantmentName.substring(0, 1).toUpperCase() + enchantmentName.substring(1);
        var enchantmentTextNbtString = NbtString.of("[{\"text\":\"" + enchantmentName + "\",\"color\":\"gray\",\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false}]");
        return enchantmentTextNbtString;
    }

}
