package net.mcreator.pmtinfai.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;

public class Recipe_MKLG {
    public static NonNullList<ItemStack> And_Gate(){
        return null;
    }

    public static ItemStack CheckRecipe(NonNullList<ItemStack>itemstack){
        if(itemstack.get(0).getItem().toString()=="input"&&itemstack.get(0).getCount()==1)
            return new ItemStack(Items.PUMPKIN,1);
        return null;
    }
}
