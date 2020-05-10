
package net.mcreator.pmtinfai.item;

import net.mcreator.pmtinfai.MKLGItems;
import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.block.BlockState;

import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.mcreator.pmtinfai.PMTINFAIElements;

@PMTINFAIElements.ModElement.Tag
public class CustomCardItem extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:customcard")
	public static final Item block = null;
	public CustomCardItem(PMTINFAIElements instance) {
		super(instance, 28);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemCustom());
	}
	public static class ItemCustom extends StandardCardItem.ItemCustom {
		public ItemCustom() {
			super();
			setRegistryName("customcard");
			MKLGItems.CustomCardItem=this;
		}
	}
}
