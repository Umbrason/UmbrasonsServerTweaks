package net.umbrason.umbrasonsservertweaks.dispenseroverhauls.mixins;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Items;
import net.umbrason.umbrasonsservertweaks.dispenseroverhauls.GoatHornDispenserBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBehavior.class)
public interface DispenserBehaviourMixin {
    @Inject(method = "registerDefaults()V", at = @At("HEAD"))
    private static void registerDefaults(CallbackInfo ci) {
        DispenserBlock.registerBehavior(Items.GOAT_HORN, new GoatHornDispenserBehaviour());
    }
}
