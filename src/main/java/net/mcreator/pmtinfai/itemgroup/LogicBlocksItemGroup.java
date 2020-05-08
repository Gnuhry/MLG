
package net.mcreator.pmtinfai.itemgroup;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.block.Blocks;

import net.mcreator.pmtinfai.PMTINFAIElements;

@PMTINFAIElements.ModElement.Tag
public class LogicBlocksItemGroup extends PMTINFAIElements.ModElement {
	public LogicBlocksItemGroup(PMTINFAIElements instance) {
		super(instance, 2);
	}

	@Override
	public void initElements() {
		tab = new ItemGroup("tablogicblocks") {
			@OnlyIn(Dist.CLIENT)
			@Override
			public ItemStack createIcon() {
				return new ItemStack(Blocks.REDSTONE_WIRE, 1);
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		};
	}
	public static ItemGroup tab;
}
