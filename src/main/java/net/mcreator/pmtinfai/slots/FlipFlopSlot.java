
package net.mcreator.pmtinfai.slots;

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
		if ((set && stack.getItem().toString().equals("input")) || (output && stack.getItem().toString().equals("output"))
				|| (reset && stack.getItem().toString().equals("redstone")) || (clock && stack.getItem().toString().equals("redstone_torch"))
				|| (output && stack.getItem().toString().equals("standardcard"))) {
			return true;
		}
		return false;
	}
}
