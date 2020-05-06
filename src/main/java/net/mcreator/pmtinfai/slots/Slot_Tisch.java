
package net.mcreator.pmtinfai.slots;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.IInventory;

public class Slot_Tisch extends Slot {
	private String item="input";
	public Slot_Tisch(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (stack.getItem().toString().equals(item)) {
			return true;
		}
		return false;
	}

	public void SetItem(String item){
		this.item=item;
		this.setBackgroundName("blocks:arrow_up");
	}
	public String GetItem(){
		return item;
	}
}
