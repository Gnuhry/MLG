package net.mcreator.pmtinfai;

import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.IInventory;

public class Slot_IO extends Slot {
	public boolean input=true, output=true;
	public Slot_IO(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		if ((input && stack.getItem().toString() == "input") || (output&&stack.getItem().toString() == "output"|| (output&&stack.getItem().toString() == "redstone")) {
			return true;
		}
		return false;
	}
}
