package net.umbrason.umbrasonsservertweaks.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Optional;

@Mixin(Item.class)
public class RecoveryCompassMixin {

    private final static int[] heartbeatTimings = {0, 25, 46, 62, 74, 83, 88, 92, 94, 95};

    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at = @At("HEAD"), cancellable = true)
    void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        var itemStack = user.getStackInHand(hand);
        if (!itemStack.getItem().equals(Items.RECOVERY_COMPASS)) return;
        user.setCurrentHand(hand);
        cir.setReturnValue(TypedActionResult.consume(itemStack));
    }

    @Inject(method = "getUseAction(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/UseAction;", at = @At("HEAD"), cancellable = true)
    public void getUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
        if (!stack.getItem().equals(Items.RECOVERY_COMPASS)) return;
        cir.setReturnValue(UseAction.SPEAR);
    }

    @Inject(method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V", at = @At("HEAD"))
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (!stack.getItem().equals(Items.RECOVERY_COMPASS) || world.isClient) return;
        var currentTick = getMaxUseTime(stack) - remainingUseTicks;
        if (Arrays.stream(heartbeatTimings).anyMatch(value -> value == currentTick))
            world.playSoundFromEntity(null, user, SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, .5f, 1f + (currentTick / 180f));
    }

    @Inject(method = "getMaxUseTime(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    public void getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!stack.getItem().equals(Items.RECOVERY_COMPASS)) return;
        cir.setReturnValue(100);
    }

    @Shadow
    public int getMaxUseTime(ItemStack stack) {
        throw new NotImplementedException();
    }

    @Inject(method = "finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"))
    public void OnFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!stack.getItem().equals(Items.RECOVERY_COMPASS)) return;

        var server = world.getServer();
        if (world.isClient || server == null)
            return;
        world.playSoundFromEntity(null, user, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, .5f, 1.33f);
        var serverPlayerEntity = (ServerPlayerEntity) user;
        var serverWorld = server.getWorld(serverPlayerEntity.getSpawnPointDimension());
        if (serverWorld == null)
            serverWorld = server.getOverworld();
        Optional<Vec3d> teleportTarget = Optional.empty();

        var playerSpawnPointPosition = serverPlayerEntity.getSpawnPointPosition();
        if (playerSpawnPointPosition != null)
            teleportTarget = PlayerEntity.findRespawnPosition(serverWorld, playerSpawnPointPosition, 0, false, true);
        if (teleportTarget.isEmpty())
            teleportTarget = PlayerEntity.findRespawnPosition(serverWorld, serverWorld.getSpawnPos(), 0, true, true);
        if (teleportTarget.isEmpty())
            return;
        serverPlayerEntity.teleport(serverWorld, teleportTarget.get().getX(), teleportTarget.get().getY(), teleportTarget.get().getZ(), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch());
        while (!serverWorld.isSpaceEmpty(serverPlayerEntity) && serverPlayerEntity.getY() < (double) serverWorld.getTopY()) {
            serverPlayerEntity.setPosition(serverPlayerEntity.getX(), serverPlayerEntity.getY() + 1.0, serverPlayerEntity.getZ());
        }
        serverPlayerEntity.getItemCooldownManager().set(stack.getItem(), 100);
    }

    /*@Inject(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", at = @At("HEAD"))
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (!stack.getItem().equals(Items.RECOVERY_COMPASS)) return;
    }*/
}
