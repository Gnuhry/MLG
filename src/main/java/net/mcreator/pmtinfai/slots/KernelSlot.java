package net.mcreator.pmtinfai.slots;

import net.mcreator.pmtinfai.MKLGItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class KernelSlot extends Slot {
    public boolean logic_ = false;

    public KernelSlot(IInventory internal, int i, int i1, int i2) {
        super(internal, i, i1, i2);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return logic_ ? stack.getItem() == MKLGItems.StandardcardItem && stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("logic") && stack.getTag().contains("logic_") : stack.getItem() == MKLGItems.StandardcardItem && stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("logic") && !stack.getTag().contains("logic_");
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
