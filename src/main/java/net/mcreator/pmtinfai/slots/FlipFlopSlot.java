
package net.mcreator.pmtinfai.slots;

import net.mcreator.pmtinfai.MKLGItems;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.IInventory;

public class FlipFlopSlot extends Slot {
    public boolean set = true, reset = true, clock = true, output = true;

    public FlipFlopSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (set && stack.getItem() == MKLGItems.InputItem || output && stack.getItem() == MKLGItems.OutputItem
                || reset && stack.getItem() == MKLGItems.ResetItem || clock && stack.getItem() == MKLGItems.ClockItem) {
            return true;
        }
        return false;
    }
}
