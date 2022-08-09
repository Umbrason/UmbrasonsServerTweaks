package net.umbrason.umbrasonsservertweaks.shulkerenchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NbtList;

import java.util.Map;

public interface CustomEnchantmentHolder {
    public Map<Enchantment, Integer> getEnchantments();

    public void setEnchantments(Map<Enchantment, Integer> enchMap);

    public void setEnchantments(NbtList enchNbt);
}
