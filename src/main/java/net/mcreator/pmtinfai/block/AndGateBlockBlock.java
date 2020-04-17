
package net.mcreator.pmtinfai.block;

import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.block.Block;

import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.LogicBlock;

@PMTINFAIElements.ModElement.Tag
public class AndGateBlockBlock extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:andgateblock")
	public static final Block block = null;
	public AndGateBlockBlock(PMTINFAIElements instance) {
		super(instance, 1);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}
	public static class CustomBlock extends LogicBlock {
		public CustomBlock() {
			setRegistryName("andgateblock");
		}

		protected int logic(int first_value, int second_value){
			return first_value==0||second_value==0 ? 0 : first_value < second_value ? second_value : first_value;
		}
	}
}
