package net.umbrason.umbrasonsservertweaks.dispenseroverhauls;

import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.InstrumentTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.event.GameEvent;

import java.util.Iterator;
import java.util.Optional;

public class GoatHornDispenserBehaviour implements DispenserBehavior {

    @Override
    public ItemStack dispense(BlockPointer pointer, ItemStack stack) {
        var goatHorn = (GoatHornItem) stack.getItem();
        stack.getOrCreateNbt();
        var optionalInstrument = getInstrument(stack);
        if (optionalInstrument.isEmpty())
            return stack;
        var instrument = optionalInstrument.get().value();
        var world = pointer.getWorld();
        SoundEvent soundEvent = instrument.soundEvent();
        float f = instrument.range() / 16.0f;
        world.playSound(null, pointer.getX(), pointer.getY(), pointer.getZ(), soundEvent, SoundCategory.RECORDS, f, 1.0f);
        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, pointer.getPos(), GameEvent.Emitter.of(pointer.getBlockState()));
        return stack;
    }

    private Optional<RegistryEntry<Instrument>> getInstrument(ItemStack stack) {
        Identifier identifier;
        NbtCompound nbtCompound = stack.getNbt();
        if (nbtCompound != null && (identifier = Identifier.tryParse(nbtCompound.getString("instrument"))) != null) {
            return Registry.INSTRUMENT.getEntry(RegistryKey.of(Registry.INSTRUMENT_KEY, identifier));
        }
        Iterator<RegistryEntry<Instrument>> iterator = Registry.INSTRUMENT.iterateEntries(InstrumentTags.GOAT_HORNS).iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }


}
