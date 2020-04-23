
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
import net.minecraft.state.EnumProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public abstract class LogicBlock extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	public static final EnumProperty<InputSide> INPUT1 = EnumProperty.create("input1_side", InputSide.class);
	public static final EnumProperty<InputSide> INPUT2 = EnumProperty.create("input2_side", InputSide.class);
	public static final EnumProperty<InputSide> INPUT3 = EnumProperty.create("input3_side", InputSide.class);
	public static final EnumProperty<RedstonePower> INPUT1_VALUE = EnumProperty.create("input1_value", RedstonePower.class);
	public static final EnumProperty<RedstonePower> INPUT2_VALUE = EnumProperty.create("input2_value", RedstonePower.class);
	public static final EnumProperty<RedstonePower> INPUT3_VALUE = EnumProperty.create("input3_value", RedstonePower.class);
	private static final boolean RedstoneValue = false;
	private static boolean aa = false, ab = false;
	private ArrayList<Direction> directions = new ArrayList<>();
	public LogicBlock() {
		super(Block.Properties.create(Material.REDSTONE_LIGHT).sound(SoundType.STEM).hardnessAndResistance(1f, 10f).lightValue(0));
		if (RedstoneValue)
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWER, Integer.valueOf(0))
					.with(INPUT1_VALUE, RedstonePower.ZERO).with(INPUT2_VALUE, RedstonePower.ZERO).with(INPUT3_VALUE, RedstonePower.ZERO));
		else
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWER, Integer.valueOf(0))
					.with(INPUT1, InputSide.NONE).with(INPUT2, InputSide.NONE).with(INPUT3, InputSide.NONE));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		if (RedstoneValue)
			builder.add(FACING).add(POWER).add(INPUT1_VALUE).add(INPUT1_VALUE).add(INPUT2_VALUE).add(INPUT3_VALUE);
		else
			builder.add(FACING).add(POWER).add(INPUT1).add(INPUT2).add(INPUT3);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
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
		return side == null ? false : (state.get(FACING) == side || directions.contains(side.getOpposite()));
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
		
		if ((!n.getBlock().canProvidePower(n)) && n.isSolid()&& (!(n.getBlock() instanceof LogicBlock))) {
			world.notifyNeighborsOfStateExcept(blockpos, this, direction);
		}
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
		super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
		Direction direction = null;
		if(state.has(HorizontalBlock.HORIZONTAL_FACING)){
			direction = state.get(HorizontalBlock.HORIZONTAL_FACING);
			if (net.minecraftforge.event.ForgeEventFactory
					.onNeighborNotify(world, pos, world.getBlockState(pos), java.util.EnumSet.of(direction.getOpposite()), false).isCanceled())
				return;
		}
		getPowerOnSides(world, pos, state);
		if(neighborBlock instanceof LogicBlock){
			if(world.getBlockState(fromPos).get(FACING)==state.get(FACING).getOpposite()){//connected
				System.out.println("Connected");
			}
			else{ //maybe input
				System.out.println("INPUT");
			}
		}
		else{
			if(direction!=null){
				BlockPos blockpos = pos.offset(direction.getOpposite());
				world.neighborChanged(blockpos, this, pos);
				BlockState n = world.getBlockState(blockpos);
				if ((!n.getBlock().canProvidePower(n)) && n.isSolid()) {
					world.notifyNeighborsOfStateExcept(blockpos, this, direction);
				}
			}			
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (aa)
			return;
		aa = true;
		this.neighborChanged(state, worldIn, pos, null, null, false);
		
		directions.clear();
		this.addInput(state.get(FACING).rotateY(), pos, worldIn);
		this.addInput(state.get(FACING).rotateYCCW(), pos, worldIn);
		this.addInput(state.get(FACING), pos, worldIn);
		aa = false;
	}

	private void addInput(Direction d, BlockPos pos, World world) {
		directions.add(d);
		if (!RedstoneValue)
			switch (directions.size()) {
				case 1 :
					world.setBlockState(pos, world.getBlockState(pos).with(INPUT1, InputSide.GetEnum(d)), 2);
					world.setBlockState(pos, world.getBlockState(pos).with(INPUT2, InputSide.NONE), 2);
					world.setBlockState(pos, world.getBlockState(pos).with(INPUT3, InputSide.NONE), 2);
					break;
				case 2 :
					world.setBlockState(pos, world.getBlockState(pos).with(INPUT2, InputSide.GetEnum(d)), 2);
					world.setBlockState(pos, world.getBlockState(pos).with(INPUT3, InputSide.NONE), 2);
					break;
				case 3 :
					world.setBlockState(pos, world.getBlockState(pos).with(INPUT3, InputSide.GetEnum(d)), 2);
					break;
			}
	}

	private void SetInput_OutputSide(BlockPos pos, World world, ArrayList<Integer> input, int output) {
		BlockState help = world.getBlockState(pos);
		if (RedstoneValue) {
			switch (directions.size()) {
				case 3 :
					help = help.with(INPUT3_VALUE, RedstonePower.GetEnum(input.get(2)));
				case 2 :
					help = help.with(INPUT2_VALUE, RedstonePower.GetEnum(input.get(1)));
				case 1 :
					help = help.with(INPUT1_VALUE, RedstonePower.GetEnum(input.get(0)));
			}
		}
		world.setBlockState(pos, help.with(POWER, output), 2);
	}

	protected void getPowerOnSides(World worldIn, BlockPos pos, BlockState state) {
		if (ab)
			return;
		ab = true;
		ArrayList<Integer> inputs = new ArrayList();
		for (Direction direct : directions) {
			inputs.add(this.getPowerOnSide(worldIn, pos.offset(direct), direct));
		}
		ab = false;
		if (inputs.size() <= 0)
			SetInput_OutputSide(pos, worldIn, inputs, 0);
		else
			SetInput_OutputSide(pos, worldIn, inputs, logic(inputs));
	}

	protected int getPowerOnSide(World worldIn, BlockPos pos, Direction side) {
		return (worldIn.getBlockState(pos).canProvidePower() || worldIn.getBlockState(pos).isSolid()) ? worldIn.getRedstonePower(pos, side) : 0;
	}

	protected abstract int logic(List<Integer> inputs);
}
