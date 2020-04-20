
package net.mcreator.pmtinfai.block;

import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.block.Block;

import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.mcreator.pmtinfai.PMTINFAIElements;
import java.util.Collections;
import java.util.List;

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

		protected int logic(List<Integer> inputs) {
			return inputs.contains(0) ? 0 : Collections.max(inputs);
		}
	}
}
