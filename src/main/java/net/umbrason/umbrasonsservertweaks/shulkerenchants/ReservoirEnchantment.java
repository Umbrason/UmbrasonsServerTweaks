package net.umbrason.umbrasonsservertweaks.shulkerenchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ReservoirEnchantment extends Enchantment {
    public ReservoirEnchantment() {
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
