
package net.mcreator.pmtinfai.block;

import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.block.Block;

import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.mcreator.pmtinfai.PMTINFAIElements;

@PMTINFAIElements.ModElement.Tag
public class AndGateBlockBlock extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:andgateblock")
	public static final Block block = null;
	public AndGateBlockBlock(PMTINFAIElements instance) {
		super(instance, 3);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items
				.add(() -> new BlockItem(block, new Item.Properties().group(LogicBlocksItemGroup.tab)).setRegistryName(block.getRegistryName()));
	}
	public static class CustomBlock extends net.mcreator.pmtinfai.LogicBlock {
		public CustomBlock() {
			setRegistryName("andgateblock");
		}

		protected int logic(int first_value, int second_value) {
			return first_value == 0 || second_value == 0 ? 0 : first_value < second_value ? second_value : first_value;
		}
	}
}
