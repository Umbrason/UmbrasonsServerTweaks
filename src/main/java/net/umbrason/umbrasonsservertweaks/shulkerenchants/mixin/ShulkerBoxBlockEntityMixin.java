package net.umbrason.umbrasonsservertweaks.shulkerenchants.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.registry.Registry;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.CustomEnchantmentHolder;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin implements CustomEnchantmentHolder {

    @Unique
    public Map<Enchantment, Integer> enchantmentMap = new HashMap<>();

    private static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantmentMap;
    }


    @Override
    public void setEnchantments(Map<Enchantment, Integer> enchMap) {
        this.enchantmentMap = enchMap;
    }


    @Override
    public void setEnchantments(NbtList enchNbt) {
        setEnchantments(EnchantmentHelper.fromNbt(enchNbt));
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    void readNbt(NbtCompound nbt, CallbackInfo ci) {
        enchantmentMap = EnchantmentHelper.fromNbt(nbt.getList(ItemStack.ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE));
    }

    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        var enchantmentList = new NbtList();
        for (Map.Entry<Enchantment, Integer> entry : this.enchantmentMap.entrySet()) {

            var enchantment = entry.getKey();
            var lvl = entry.getValue();
            var id = Registry.ENCHANTMENT.getId(enchantment);
            enchantmentList.add(EnchantmentHelper.createNbt(id, lvl));
        }
        nbt.put(ItemStack.ENCHANTMENTS_KEY, enchantmentList);
    }

}
