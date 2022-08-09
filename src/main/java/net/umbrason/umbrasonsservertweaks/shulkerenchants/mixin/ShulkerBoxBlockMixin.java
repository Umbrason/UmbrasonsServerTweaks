package net.umbrason.umbrasonsservertweaks.shulkerenchants.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.CustomEnchantmentHolder;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.ShulkerEnchantsUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
    @Inject(
            method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContext$Builder;)Ljava/util/List;",
            at = @At("RETURN")
    )
    public void onGetDroppedStacks(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> ci) {
        for (ItemStack stack : ci.getReturnValue()) {
            if (ShulkerEnchantsUtility.isShulkerBox(stack)) {
                BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
                if (blockEntity instanceof CustomEnchantmentHolder) {
                    CustomEnchantmentHolder enchHolder = (CustomEnchantmentHolder) blockEntity;
                    EnchantmentHelper.set(enchHolder.getEnchantments(), stack);
                    ShulkerEnchantsUtility.appendCustomEnchantmentToLore(stack, enchHolder.getEnchantments());
                }
            }
        }
    }

    @Inject(method = "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",shift = At.Shift.BEFORE),locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci, BlockEntity blockEntity, ShulkerBoxBlockEntity shulkerBoxBlockEntity, ItemStack itemStack) {
        BlockEntity sbEntity = world.getBlockEntity(pos);
        if (sbEntity instanceof CustomEnchantmentHolder) {
            CustomEnchantmentHolder enchHolder = (CustomEnchantmentHolder) sbEntity;
            EnchantmentHelper.set(enchHolder.getEnchantments(), itemStack);
        }
    }

    @Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        BlockEntity blockEntity;
        if (itemStack.hasEnchantments() && (blockEntity = world.getBlockEntity(pos)) instanceof CustomEnchantmentHolder) {
            ((CustomEnchantmentHolder)blockEntity).setEnchantments(itemStack.getEnchantments());
        }
    }
}
