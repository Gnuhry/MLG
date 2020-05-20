
package net.mcreator.pmtinfai.slots;

import net.mcreator.pmtinfai.MKLGItems;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.IInventory;

public class LogicSlot extends Slot {
    public boolean input = true, output = true;

    public LogicSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (input && stack.getItem() == MKLGItems.InputItem || output && stack.getItem() == MKLGItems.OutputItem) {
            return true;
        }
        return false;
    }
}
