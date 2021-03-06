
package net.mcreator.pmtinfai.item;

import net.mcreator.pmtinfai.MKLGItems;
import net.minecraft.client.resources.I18n;
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
import net.mcreator.pmtinfai.enums.LogicSpecies;

import javax.annotation.Nullable;

import java.util.List;

@PMTINFAIElements.ModElement.Tag
public class CustomCardItem extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:customcard")
	public static final Item block = null;
	public CustomCardItem(PMTINFAIElements instance) {
		super(instance, 10);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemCustom());
	}
	public static class ItemCustom extends Item {
		public ItemCustom() {
			super(new Item.Properties().group(LogicBlocksItemGroup.tab).maxStackSize(16));
			setRegistryName("customcard");
			MKLGItems.CustomCardItem=this;
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
			if (stack.hasTag()&&stack.getTag().contains("logic")) {
				assert stack.getTag() != null;
				String help = stack.getTag().getString("logic");
				boolean b = stack.getTag().getBoolean("logic_");
				if (b) {
					help = LogicSpecies.GetEnum(help).getTranslationName();
				}
				itextcomponent.appendText(I18n.format("item.pmtinfai.logic")+": ").appendText(help);
				tooltip.add(itextcomponent);
			}
		}
	}
}
