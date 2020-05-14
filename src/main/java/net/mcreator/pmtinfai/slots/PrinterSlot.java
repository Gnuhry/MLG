
package net.mcreator.pmtinfai.slots;

import net.mcreator.pmtinfai.MKLGItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PrinterSlot extends Slot {
    private boolean Tag = false;
    private Item item = MKLGItems.StandardcardItem;
    private Item item2 = MKLGItems.CustomCardItem;

    public PrinterSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return Tag ? (stack.getItem() == item || stack.getItem() == item2) && stack.hasTag() && stack.getTag().contains("logic") : (stack.getItem() == item || stack.getItem() == item2);
    }

    public void setTag(boolean tag) {
        Tag = tag;
    }
}
