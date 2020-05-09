
package net.mcreator.pmtinfai.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PrinterSlot extends Slot {
    private Item item;
    private boolean Tag=false;
    public PrinterSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)  {
        return Tag ? item != null && stack.getItem() == item&&stack.hasTag()&&stack.getTag().contains("logic") : item != null && stack.getItem() == item;
    }

    public void SetItem(Item item) {
        this.item = item;
    }

    public Item GetItem() {
        return item;
    }

    public void setTag(boolean tag) {
        Tag = tag;
    }
}
