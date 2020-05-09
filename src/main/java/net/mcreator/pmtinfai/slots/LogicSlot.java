
package net.mcreator.pmtinfai.slots;

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
		if ((input && stack.getItem().toString().equals("input")) || (output && stack.getItem().toString().equals("output"))
				|| (output && stack.getItem().toString().equals("standardcard"))) {
			return true;
		}
		return false;
	}
}
