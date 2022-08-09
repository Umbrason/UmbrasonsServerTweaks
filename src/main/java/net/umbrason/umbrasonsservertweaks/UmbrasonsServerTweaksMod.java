package net.umbrason.umbrasonsservertweaks;

import net.fabricmc.api.ModInitializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.ReservoirEnchantment;
import net.umbrason.umbrasonsservertweaks.shulkerenchants.SiphonEnchantment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UmbrasonsServerTweaksMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "umbrasonsservertweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Enchantment SIPHON = new SiphonEnchantment();
    public static final Enchantment RESERVOIR = new ReservoirEnchantment();

    public static final Enchantment[] ENCHANTMENTS = {SIPHON, RESERVOIR};

    @Override
    public void onInitialize() {
        Registry.register(Registry.ENCHANTMENT, new Identifier(MODID, "siphon"), SIPHON);
        Registry.register(Registry.ENCHANTMENT, new Identifier(MODID, "reservoir"), RESERVOIR);
    }
}
