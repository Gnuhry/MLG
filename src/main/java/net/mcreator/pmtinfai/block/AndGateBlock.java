
package net.mcreator.pmtinfai.block;

import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.BlockPos;
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

import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.block.RepeaterBlock;

import net.mcreator.pmtinfai.PMTINFAIElements;

import java.util.List;
import java.util.Collections;
import com.mojang.authlib.properties.Property;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.block.RedstoneBlock;

@PMTINFAIElements.ModElement.Tag
public class AndGateBlock extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:andgate")
	public static final Block block = null;
	public AndGateBlock(PMTINFAIElements instance) {
		super(instance, 1);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
		public int output = 0;
		public CustomBlock() {
			super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(1f, 10f).lightValue(0));
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWERED, false));
			setRegistryName("andgate");
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING);
			builder.add(POWERED);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
		}

		@Override
		public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
			if(side==Direction.UP||side==Direction.DOWN)
				return false;
				
			if(state.get(FACING)==Direction.NORTH&&side==Direction.SOUTH){
				return false;
			}
			if(state.get(FACING)==Direction.SOUTH&&side==Direction.NORTH){
				return false;
			}
			if(state.get(FACING)==Direction.WEST&&side==Direction.EAST){
				return false;
			}
			if(state.get(FACING)==Direction.EAST&&side==Direction.WEST){
				return false;
			}
			return true;
		}

		@Override
		public boolean canProvidePower(BlockState state){
			return true;
		}

		@Override
		public int getWeakPower(BlockState blockState,IBlockReader blockAccess,BlockPos pos,Direction side){
				if(blockState.get(FACING) == side)
					return output;
				return 0;
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@Override
		public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moving) {
			super.onBlockAdded(state, world, pos, oldState, moving);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			{
				java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
				$_dependencies.put("x", x);
				$_dependencies.put("y", y);
				$_dependencies.put("z", z);
				$_dependencies.put("world", world);
				//AndGateRedstoneOnProcedure.executeProcedure($_dependencies);
			}
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			if(!neighborBlock.canProvidePower(state))
				return;
			BlockPos input1_pos = pos.north();
			BlockPos input2_pos = pos.south();
			if(state.get(FACING) == Direction.NORTH||state.get(FACING) == Direction.SOUTH){
				input1_pos = pos.west();
				input2_pos = pos.east();
			}
			int input1_value=IsRedstonePowered(world.getBlockState(input1_pos));
			int input2_value=IsRedstonePowered(world.getBlockState(input2_pos));
			if(input1_value>0&&input2_value>0){
				output=input1_value;
				if(input1_value<input2_value){
					output=input2_value;}
					}
			else{
				output=0;}

			if(output>0){
                world.setBlockState(pos, world.getBlockState(pos).with(POWERED, Boolean.getBoolean("true")), 2);
            } else {
                world.setBlockState(pos, world.getBlockState(pos).with(POWERED, Boolean.getBoolean("false")), 2);
            }
			
			
		}

		public int IsRedstonePowered(BlockState bs){
			if(bs.has(BlockStateProperties.POWER_0_15)){
				return bs.get(BlockStateProperties.POWER_0_15)-1;}
			if(bs.has(BlockStateProperties.POWERED) && bs.get(BlockStateProperties.POWERED)){
				return 15;}
			if(bs.has(BlockStateProperties.LIT) && bs.get(BlockStateProperties.LIT)){
				return 15;}
			//if(bs.getBlock().instanceOf(RedstoneBlock))
			//	return 15;
			return 0;
		}
	}
}
