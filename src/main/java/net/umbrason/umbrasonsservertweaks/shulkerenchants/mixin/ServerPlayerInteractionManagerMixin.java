package net.umbrason.umbrasonsservertweaks.shulkerenchants.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.umbrason.umbrasonsservertweaks.UmbrasonsServerTweaksMod;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.ShulkerEnchantsUtility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.stream.IntStream;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {


    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "interactItem(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void interactItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir, int i) {
        var restockAmount = i - stack.getCount();
        if (restockAmount == 0) return;
        var shulkerBoxes = ShulkerEnchantsUtility.filterShulkerBoxesWithEnchantments(ShulkerEnchantsUtility.shulkerBoxesFromInventory(player.getInventory()), UmbrasonsServerTweaksMod.RESERVOIR, 1);
        while (shulkerBoxes.size() > 0 && restockAmount > 0)
            restockAmount -= ShulkerEnchantsUtility.restockFromShulkerBox(restockAmount, stack, shulkerBoxes.remove(0));
        var handler = player.networkHandler;
        handler.sendPacket(new InventoryS2CPacket(player.currentScreenHandler.syncId, player.currentScreenHandler.nextRevision(), player.currentScreenHandler.getStacks(), player.currentScreenHandler.getCursorStack()));
    }

    @Redirect(method = "interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    public ActionResult interactBlock(ItemStack instance, ItemUsageContext context) {
        int i = instance.getCount();
        var handler = player.networkHandler;
        var ActionResult = instance.useOnBlock(context);
        var restockAmount = i - instance.getCount();
        if (restockAmount == 0) return ActionResult;
        var shulkerBoxes = ShulkerEnchantsUtility.filterShulkerBoxesWithEnchantments(ShulkerEnchantsUtility.shulkerBoxesFromInventory(player.getInventory()), UmbrasonsServerTweaksMod.RESERVOIR, 1);
        while (shulkerBoxes.size() > 0 && restockAmount > 0)
            restockAmount -= ShulkerEnchantsUtility.restockFromShulkerBox(restockAmount, instance, shulkerBoxes.remove(0));
        handler.sendPacket(new InventoryS2CPacket(player.currentScreenHandler.syncId, player.currentScreenHandler.nextRevision(), player.currentScreenHandler.getStacks(), player.currentScreenHandler.getCursorStack()));
        return ActionResult;
    }

}
