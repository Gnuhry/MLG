
package net.mcreator.pmtinfai.item;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.block.BlockState;

import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.mcreator.pmtinfai.PMTINFAIElements;

import javax.annotation.Nullable;

import java.util.List;

@PMTINFAIElements.ModElement.Tag
public class StandardcardItem extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:standardcard")
	public static final Item block = null;
	public StandardcardItem(PMTINFAIElements instance) {
		super(instance, 10);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemCustom());
	}
	public static class ItemCustom extends Item {
		public ItemCustom() {
			super(new Item.Properties().group(LogicBlocksItemGroup.tab).maxStackSize(64));
			setRegistryName("standardcard");
		}

		@Override
		public int getItemEnchantability() {
			return 0;
		}

		@Override
		public int getUseDuration(ItemStack itemstack) {
			return 0;
		}

		@Override
		public float getDestroySpeed(ItemStack par1ItemStack, BlockState par2Block) {
			return 1F;
		}

		@OnlyIn(Dist.CLIENT)
		public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		 super.addInformation(stack, worldIn, tooltip, flagIn);
		         ITextComponent itextcomponent = stack.getDisplayName().deepCopy();
		         if(stack.hasTag())
                 itextcomponent.appendText("logic: ").appendText(stack.getTag().getString("logic"));
                 tooltip.add(itextcomponent);
		}
	}
}
