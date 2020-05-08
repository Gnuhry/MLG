
package net.mcreator.pmtinfai.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Slot_Tisch extends Slot {
    private Item item;
    private int Stack_Limit = 64;

    public Slot_Tisch(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return item != null && stack.getItem() == item;
    }

    public void SetItem(Item item) {
        this.item = item;
    }

    public Item GetItem() {
        return item;
    }

    public void SetSlotStackLimit(int set) {
        Stack_Limit = set;
    }

    @Override
    public int getSlotStackLimit() {
        return Stack_Limit;
    }
}
