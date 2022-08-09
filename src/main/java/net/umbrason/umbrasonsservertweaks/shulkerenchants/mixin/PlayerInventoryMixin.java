package net.umbrason.umbrasonsservertweaks.shulkerenchants.mixin;


import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.umbrason.umbrasonsservertweaks.UmbrasonsServerTweaksMod;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.ShulkerEnchantsUtility;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Final
    @Shadow
    private List<DefaultedList<ItemStack>> combinedInventory;

    @Shadow
    private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        throw new NotImplementedException();
    }

    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    void insertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        UmbrasonsServerTweaksMod.LOGGER.debug("reached this");
        if (!stack.isStackable() || combinedInventory == null)
            return;

        var inventoryStacks = combinedInventory.stream().flatMap(Collection::stream).collect(Collectors.toList());
        var siphonShulkerBoxItemStacks = ShulkerEnchantsUtility.filterShulkerBoxesWithEnchantments(inventoryStacks, UmbrasonsServerTweaksMod.SIPHON, 1);
        for (var shulkerBoxItemStack : siphonShulkerBoxItemStacks) {
            var inventory = ShulkerEnchantsUtility.getShulkerInventory(shulkerBoxItemStack);
            if (inventory == null) continue;
            var inventoryChanged = false;
            var suitableStacks = inventory.stream().filter(shulkerStack -> canStackAddMore(shulkerStack, stack)).collect(Collectors.toCollection(LinkedList::new));
            while (!suitableStacks.isEmpty()) {
                var targetStack = suitableStacks.removeFirst();
                var availableSpace = targetStack.getMaxCount() - targetStack.getCount();
                if (stack.getCount() > availableSpace) {
                    stack.setCount(stack.getCount() - availableSpace);
                    targetStack.setCount(targetStack.getMaxCount());
                    inventoryChanged = true;
                } else {
                    targetStack.setCount(stack.getCount() + targetStack.getCount());
                    stack.setCount(0);
                    inventoryChanged = true;
                    break;
                }
            }

            if (suitableStacks.isEmpty() && inventory.stream().anyMatch(ItemStack::isEmpty) && inventory.stream().anyMatch(shulkerStack -> ItemStack.canCombine(shulkerStack, stack))) {
                for (int i = 0; i < inventory.size(); i++) {
                    if (inventory.get(i).isEmpty()) {
                        inventory.set(i, stack.copy());
                        stack.setCount(0);
                        inventoryChanged = true;
                        break;
                    }
                }
            }

            if (inventoryChanged) {
                var nbt = new NbtCompound();
                Inventories.writeNbt(nbt, inventory); //generate new NBT
                BlockItem.setBlockEntityNbt(shulkerBoxItemStack, BlockEntityType.SHULKER_BOX, nbt); //write NBT back
            }
            if (stack.getCount() == 0) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

}
