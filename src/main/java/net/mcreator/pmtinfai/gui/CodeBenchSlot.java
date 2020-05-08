package net.mcreator.pmtinfai.gui;

import net.mcreator.pmtinfai.MKLGItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class CodeBenchSlot extends Slot {
    public CodeBenchSlot(IInventory internal, int i, int i1, int i2) {
        super(internal,i,i1,i2);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem()== MKLGItems.CustomCardItem;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
