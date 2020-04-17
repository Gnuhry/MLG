
package net.mcreator.pmtinfai.block;

import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.mcreator.pmtinfai.LogicBlock;

import net.mcreator.pmtinfai.PMTINFAIElements;

import java.util.List;
import java.util.Collections;

@PMTINFAIElements.ModElement.Tag
public class TestInterfaceBlock extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:testinterface")
	public static final Block block = null;
	public TestInterfaceBlock(PMTINFAIElements instance) {
		super(instance, 3);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}
	public static class CustomBlock extends LogicBlock {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public CustomBlock() {
			setRegistryName("testinterface");
		}
	public int logic(int first_value, int second_value){
		if(first_value<=0||second_value<=0)
			return 0;
		if(first_value<second_value)
			return second_value;
		return first_value;
	}
	}
}
