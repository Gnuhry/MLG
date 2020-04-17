
package net.mcreator.pmtinfai;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IWorldReader;
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
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import java.util.List;
import java.util.Collections;

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
		return (!(side == Direction.UP || side == Direction.DOWN || state.get(FACING) == side.getOpposite()));
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
		return blockState.get(FACING) == side ? blockState.get(POWER) : 0;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
		super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
		world.setBlockState(pos, world.getBlockState(pos).with(POWER, getPowerOnSides(world, pos, state)), 2);
		Direction direction = state.get(FACING);
		BlockPos blockpos = pos.offset(direction.getOpposite());
		if (net.minecraftforge.event.ForgeEventFactory
				.onNeighborNotify(world, pos, world.getBlockState(pos), java.util.EnumSet.of(direction.getOpposite()), false).isCanceled())
			return;
		world.neighborChanged(blockpos, this, pos);
		world.notifyNeighborsOfStateExcept(blockpos, this, direction);
	}

	protected int getPowerOnSides(IWorldReader worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.get(FACING);
		Direction direction1 = direction.rotateY();
		Direction direction2 = direction.rotateYCCW();
		return logic(this.getPowerOnSide(worldIn, pos.offset(direction1), direction1),
				this.getPowerOnSide(worldIn, pos.offset(direction2), direction2));
	}

	protected int getPowerOnSide(IWorldReader worldIn, BlockPos pos, Direction side) {
		BlockState blockstate = worldIn.getBlockState(pos);
		Block block = blockstate.getBlock();
		if (this.isAlternateInput(blockstate)) {
			if (block == Blocks.REDSTONE_BLOCK) {
				return 15;
			} else {
				return block == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : worldIn.getStrongPower(pos, side);
			}
		} else {
			return 0;
		}
	}

	protected boolean isAlternateInput(BlockState state) {
		return state.canProvidePower();
	}

	public abstract int logic(int first_value, int second_value);
}
