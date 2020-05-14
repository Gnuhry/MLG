
package net.mcreator.pmtinfai.block;

import net.mcreator.pmtinfai.MKLGBlock;
import net.mcreator.pmtinfai.MKLGItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.mcreator.pmtinfai.PMTINFAIElements;

import java.util.List;
import java.util.Collections;

@PMTINFAIElements.ModElement.Tag
public class TischModelBlock extends PMTINFAIElements.ModElement {
    @ObjectHolder("pmtinfai:tisch")
    public static final Block block = null;

    public TischModelBlock(PMTINFAIElements instance) {
        super(instance, 40);
    }

    @Override
    public void initElements() {
        elements.blocks.add(() -> new CustomBlock());
        elements.items
                .add(() -> new BlockItem(block, new Item.Properties().group(LogicBlocksItemGroup.tab)).setRegistryName(block.getRegistryName()));
    }

    public static class CustomBlock extends Block {
        protected static final VoxelShape SWL_CORNER = Block.makeCuboidShape(0.2D, 0.0D, 0.2D, 2.2D, 6.0D, 2.2D);
        protected static final VoxelShape NWL_CORNER = Block.makeCuboidShape(0.2D, 0.0D, 15.8D, 2.0D, 6.0D, 13.8D);
        protected static final VoxelShape NEL_CORNER = Block.makeCuboidShape(15.8D, 0.0D, 15.8D, 13.8D, 6.0D, 13.8D);
        protected static final VoxelShape SEL_CORNER = Block.makeCuboidShape(15.8D, 0.0D, 0.2D, 13.8D, 6.0D, 2.2D);
        protected static final VoxelShape PLATE = Block.makeCuboidShape(0.2D, 6.0D, 0.2D, 15.8D, 8.0D, 15.8D);
        protected static final VoxelShape COMPLETE = VoxelShapes.or(SWL_CORNER, NWL_CORNER, NEL_CORNER, SEL_CORNER, PLATE);
        public CustomBlock() {
            super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(1f, 10f).lightValue(0));
            setRegistryName("tisch");
        }

        /**
         * Gibt den VoxelShape(Aussehen) des Blockes zur�ck
         *
         * @param state   Blockstate des Blockes
         * @param worldIn Teil der Welt des Blockes
         * @param pos     Position des Blockes
         * @param context Kontext
         * @return VoxelShape des Blockes
         */

        public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
            return COMPLETE;
        }
        @OnlyIn(Dist.CLIENT)
        @Override
        public BlockRenderLayer getRenderLayer() {
            return BlockRenderLayer.CUTOUT;
        }

        @Override
        public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
            return false;
        }

        @Override
        public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
            return true;
        }

        @Override
        public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
            List<ItemStack> dropsOriginal = super.getDrops(state, builder);
            if (!dropsOriginal.isEmpty())
                return dropsOriginal;
            return Collections.singletonList(new ItemStack(this, 1));
        }

        @Override
        public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            Item item = player.inventory.getStackInSlot(player.inventory.currentItem).getItem();
            if (item == MKLGItems.PrinterItem) {
                player.inventory.getStackInSlot(player.inventory.currentItem).shrink(1);
                worldIn.removeBlock(pos, false);
                BlockState bs = MKLGBlock.Printer.getDefaultState();
                worldIn.setBlockState(pos, bs);
            }
            else if (item == MKLGItems.CodeBenchItem) {
                player.inventory.getStackInSlot(player.inventory.currentItem).shrink(1);
                worldIn.removeBlock(pos, false);
                BlockState bs = MKLGBlock.Codebench.getDefaultState();
                worldIn.setBlockState(pos, bs);
            }
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        }
    }
}
