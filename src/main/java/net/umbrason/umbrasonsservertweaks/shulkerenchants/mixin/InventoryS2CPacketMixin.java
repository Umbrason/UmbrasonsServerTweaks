package net.umbrason.umbrasonsservertweaks.shulkerenchants.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.umbrason.umbrasonsservertweaks.UmbrasonsServerTweaksMod;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.ShulkerEnchantsUtility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.IntStream;

@Mixin(InventoryS2CPacket.class)
public class InventoryS2CPacketMixin {

    @Shadow
    @Final
    private List<ItemStack> contents;

    @Inject(method = "<init>(IILnet/minecraft/util/collection/DefaultedList;Lnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    void addCustomEnchantmentLore(int syncId, int revision, DefaultedList<ItemStack> contents, ItemStack cursorStack, CallbackInfo ci) {
        for (var itemStack : this.contents) {
            var enchantmentsNbt = itemStack.isOf(Items.ENCHANTED_BOOK) ? EnchantmentHelper.fromNbt(EnchantedBookItem.getEnchantmentNbt((itemStack))) : EnchantmentHelper.fromNbt(itemStack.getEnchantments());
            ShulkerEnchantsUtility.appendCustomEnchantmentToLore(itemStack,enchantmentsNbt);
        }
    }


}
