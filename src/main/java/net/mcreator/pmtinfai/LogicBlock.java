
package net.mcreator.pmtinfai;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import java.util.List;
import java.util.Collections;
import net.minecraft.world.IWorld;

public abstract class LogicBlock extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	public LogicBlock() {
		super(Block.Properties.create(Material.REDSTONE_LIGHT).sound(SoundType.STEM).hardnessAndResistance(1f, 10f).lightValue(0));
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWER, Integer.valueOf(0)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
		builder.add(POWER);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		if (!dropsOriginal.isEmpty())
			return dropsOriginal;
			//TODO:
		return Collections.singletonList(new ItemStack(this, 1));
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
		return (state.get(FACING) == side || state.get(FACING).rotateY() == side || state.get(FACING).rotateYCCW() == side);
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(FACING) == side ? blockState.get(POWER) : 0;
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.getWeakPower(blockAccess, pos, side);
	}

	
   @Deprecated
   public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
         world.removeTileEntity(pos);
      }
      Direction direction = state.get(FACING);
		BlockPos blockpos = pos.offset(direction.getOpposite());
		BlockState n = world.getBlockState(blockpos); 
		if((!n.getBlock().canProvidePower(n))&&n.isSolid())
			world.notifyNeighborsOfStateExcept(blockpos, this, direction);
   }
   
	/*@Override
	public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state) {
		System.out.println("Test");
   	}*/


	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
		super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
		Direction direction = state.get(FACING);
		BlockPos blockpos = pos.offset(direction.getOpposite());
		if (net.minecraftforge.event.ForgeEventFactory
				.onNeighborNotify(world, pos, world.getBlockState(pos), java.util.EnumSet.of(direction.getOpposite()), false).isCanceled())
			return;
		world.setBlockState(pos, world.getBlockState(pos).with(POWER, getPowerOnSides(world, pos, state)), 2);
		world.neighborChanged(blockpos, this, pos); 
		BlockState n = world.getBlockState(blockpos); 
		if((!n.getBlock().canProvidePower(n))&&n.isSolid())
			world.notifyNeighborsOfStateExcept(blockpos, this, direction);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		this.neighborChanged(state, worldIn, pos, null, null, false);
	}

	protected int getPowerOnSides(World worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.get(FACING);
		Direction direction1 = direction.rotateY();
		Direction direction2 = direction.rotateYCCW();
		return logic(this.getPowerOnSide(worldIn, pos.offset(direction1), direction1),
				this.getPowerOnSide(worldIn, pos.offset(direction2), direction2));
	}

	protected int getPowerOnSide(World worldIn, BlockPos pos, Direction side) {
		BlockState blockstate = worldIn.getBlockState(pos);
		Block block = blockstate.getBlock();
		if (this.isAlternateInput(blockstate)) {
			if (block == Blocks.REDSTONE_BLOCK) {
				return 15;
			} else if (block == Blocks.DAYLIGHT_DETECTOR) {
				return block.getWeakPower(blockstate, null, pos, side);
				// } else if (block == Blocks.TRAPPED_CHEST) {
				// return block.getStrongPower(blockstate, null, pos, side);
				// Don't work ------------------------
			} else if (IsPreasurePlate(block) && blockstate.get(PressurePlateBlock.POWERED)) {
				return block.getWeakPower(blockstate, null, pos, side);
			} else {
				return block == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : worldIn.getStrongPower(pos, side);
			}
		} else if (blockstate.isSolid()) {
			if (worldIn.isBlockPowered(pos)) {
				return worldIn.getRedstonePower(pos, side);
			}
		}
		return 0;
	}

	protected boolean isAlternateInput(BlockState state) {
		return state.canProvidePower();
	}

	protected abstract int logic(int first_value, int second_value);

	private boolean IsPreasurePlate(Block block) {
		return block == Blocks.STONE_PRESSURE_PLATE || block == Blocks.OAK_PRESSURE_PLATE || block == Blocks.SPRUCE_PRESSURE_PLATE
				|| block == Blocks.BIRCH_PRESSURE_PLATE || block == Blocks.JUNGLE_PRESSURE_PLATE || block == Blocks.ACACIA_PRESSURE_PLATE
				|| block == Blocks.DARK_OAK_PRESSURE_PLATE || block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
				|| block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE;
	}
}
