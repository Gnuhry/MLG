
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
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class LogicBlock extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	private ArrayList<Direction> directions = new ArrayList();
	
	public LogicBlock() {
		super(Block.Properties.create(Material.REDSTONE_LIGHT).sound(SoundType.STEM).hardnessAndResistance(1f, 10f).lightValue(0));
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWER, Integer.valueOf(0)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING).add(POWER);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		return !dropsOriginal.isEmpty() ?  dropsOriginal :  Collections.singletonList(new ItemStack(this, 1));
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
		return (state.get(FACING) == side || directions.contains(side.getOpposite()));
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
		if ((!n.getBlock().canProvidePower(n)) && n.isSolid())
			world.notifyNeighborsOfStateExcept(blockpos, this, direction);
	}

	/*
	 * @Override public void onPlayerDestroy(IWorld worldIn, BlockPos pos,
	 * BlockState state) { System.out.println("Test"); }
	 */
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
		if ((!n.getBlock().canProvidePower(n)) && n.isSolid())
			world.notifyNeighborsOfStateExcept(blockpos, this, direction);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		this.neighborChanged(state, worldIn, pos, null, null, false);
		directions.clear();
		
		//directions.add(state.get(FACING).rotateY());
		directions.add(state.get(FACING).rotateYCCW());
		directions.add(state.get(FACING));
	}

	protected int getPowerOnSides(World worldIn, BlockPos pos, BlockState state) {
		ArrayList<Integer> inputs = new ArrayList();
		for(Direction direct : directions) {
			inputs.add(this.getPowerOnSide(worldIn, pos.offset(direct), direct));
		}

		return inputs.size()<=0 ? 0 : logic(inputs);
	}

	protected int getPowerOnSide(World worldIn, BlockPos pos, Direction side) {
		return (worldIn.getBlockState(pos).canProvidePower() || worldIn.getBlockState(pos).isSolid()) ? worldIn.getRedstonePower(pos, side) : 0;
	}

	protected abstract int logic(List<Integer> inputs);
}
