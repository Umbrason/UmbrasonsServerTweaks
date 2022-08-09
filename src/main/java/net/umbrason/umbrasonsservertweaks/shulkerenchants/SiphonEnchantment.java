package net.umbrason.umbrasonsservertweaks.shulkerenchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SiphonEnchantment extends Enchantment {
    public SiphonEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.BREAKABLE, new EquipmentSlot[]{});
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {

        return ShulkerEnchantsUtility.isShulkerBox(stack);
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
}
