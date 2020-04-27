
package net.mcreator.pmtinfai;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.EnumProperty;
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
import java.util.ArrayList;

public abstract class LogicBlock extends Block {

	// Properties des Blocks
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	public static final EnumProperty<InputSide> INPUT1 = EnumProperty.create("input1_side", InputSide.class);
	public static final EnumProperty<InputSide> INPUT2 = EnumProperty.create("input2_side", InputSide.class);
	public static final EnumProperty<InputSide> INPUT3 = EnumProperty.create("input3_side", InputSide.class);
	public static final EnumProperty<RedstonePower> INPUT1_VALUE = EnumProperty.create("input1_value", RedstonePower.class);
	public static final EnumProperty<RedstonePower> INPUT2_VALUE = EnumProperty.create("input2_value", RedstonePower.class);
	public static final EnumProperty<RedstonePower> INPUT3_VALUE = EnumProperty.create("input3_value", RedstonePower.class);
	
	// weitere Variablen
	private static final boolean RedstoneValue = true; // Angabe ob RedstoneValue angezeigt werden soll oder RedstoneSide
	private static boolean aa = false, ab = false, ac = false, ad = false; // boolean Variablen zum Abfangen von Multithreading
	private static ArrayList<Direction> input_directions = new ArrayList<>(); // Speichern der Input Directions
	
	// Konstrukter
	public LogicBlock() {
		super(Block.Properties.create(Material.REDSTONE_LIGHT).sound(SoundType.STEM).hardnessAndResistance(1f, 10f).lightValue(0));
		// Laden der Default Properties der Blöcke
		if (RedstoneValue)
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWER, Integer.valueOf(0))
					.with(INPUT1_VALUE, RedstonePower.ZERO).with(INPUT2_VALUE, RedstonePower.ZERO).with(INPUT3_VALUE, RedstonePower.ZERO));
		else
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWER, Integer.valueOf(0))
					.with(INPUT1, InputSide.NONE).with(INPUT2, InputSide.NONE).with(INPUT3, InputSide.NONE));
	}



	/**
	 * Initalisiert die Parameter
	 * 
	 * @param builder
	 *            Builder des Blockes
	 */
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		if (RedstoneValue)
			builder.add(FACING).add(POWER).add(INPUT1_VALUE).add(INPUT2_VALUE).add(INPUT3_VALUE);
		else
			builder.add(FACING).add(POWER).add(INPUT1).add(INPUT2).add(INPUT3);
	}

	/**
	 * Gibt den Default State beim plazieren zurück
	 * 
	 * @param context
	 *            Context des Blockes beim Plazieren
	 * @return Gibt den default Blockstate zurück
	 */
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	/**
	 * Gibt den Block als Item zurück
	 * 
	 * @param state
	 *            BlockState des Blockes
	 * @param builder
	 *            Builder des LootContexes
	 * @return Block als Item oder Items
	 */
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
	}
	
	/**
	 * Abfgage wie stark die WeakPower(direkte Redstoneansteuerung) ist
	 * 
	 * @param blockState
	 *            BlockState des Blockes
	 * @param blockAccess
	 *            Angabe welche Art der Block ist
	 * @param pos
	 *            Position des Blockes
	 * @param side
	 *            Seite an der die Power abgefragt wird
	 * @return Gibt die RedstonePower(0-15) zurück
	 */
	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(FACING) == side ? blockState.get(POWER) : 0;
	}

	/**
	 * Abfgage wie stark die StrongPower(indirekte Redstoneansteuerung) ist
	 * 
	 * @param blockState
	 *            BlockState des Blockes
	 * @param blockAccess
	 *            Angabe welche Art der Block ist
	 * @param pos
	 *            Position des Blockes
	 * @param side
	 *            Seite an der die Power abgefragt wird
	 * @return Gibt die RedstonePower(0-15) zurück
	 */
	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return this.getWeakPower(blockState, blockAccess, pos, side);
	}

	/**
	 * Gibt den VoxelShape(Aussehen) des Blockes zurück
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param worldIn
	 *            Teil der Welt des Blockes
	 * @param pos
	 *            Position des Blockes
	 * @param context
	 *            Kontext
	 * @return VoxelShape des Blockes
	 */
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	}

	/**
	 * Rotiert den Block und ändert den Facing Parameter
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param Rotation
	 *            die der Block durchführen soll
	 * @return Gibt den neuen Blockstate zurück
	 */
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	/**
	 * Spiegelt den Block und ändert den Facing Parameter
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param Rotation
	 *            die der Block durchführen soll
	 * @return Gibt den neuen Blockstate zurück
	 */
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	
	
	/**
	 * Abfrage ob Redstone sich an der Seite verbinden kann
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param world
	 *            Angabe welche Art der Block ist
	 * @param pos
	 *            Position des Blockes
	 * @param side
	 *            Seite die Abgefragt wird
	 * @return Gibt zurück ob der Redstone sich verbinden kann
	 */
	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side == null ? false : (state.get(FACING) == side || input_directions.contains(side.getOpposite()));
	}

	/**
	 * Abfrage ob der Block RedstonePower ausgeben kann
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @return Gibt an ob der Block RedstonePower ausgeben kann
	 */
	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	/**
	 * Abfrage ob es eine valide Position für den Block ist
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param worldIn
	 *            Teil der Welt des Blockes
	 * @param pos
	 *            Position des Blockes
	 * @return Gibt an ob der Platz des Blockes valide ist
	 */
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return func_220064_c(worldIn, pos.down());
	}

	/**
	 * Abfrage ob Block fest ist
	 * 
	 * @param state
	 *            BlockState des Blockes
	 * @return Gibt an ob Block solid ist
	 */
	public boolean isSolid(BlockState state) {
		return true;
	}



	/**
	 * EventListener wenn Block ersetzt wird
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param world
	 *            Welt in der der Block steht
	 * @param pos
	 *            Position des Blockes
	 * @param newState
	 *            neuer Blockstate nach dem Ersetzen
	 * @param isMoving
	 *            Gibt an ob der Block sich bewegt
	 */
	@Deprecated
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onReplaced(state, world, pos, newState, isMoving);
		Direction direction = state.get(FACING);
		BlockPos blockpos = pos.offset(direction.getOpposite());
		BlockState n = world.getBlockState(blockpos);
		if ((!n.getBlock().canProvidePower(n)) && n.isSolid() && (!(n.getBlock() instanceof LogicBlock))) {
			world.notifyNeighborsOfStateExcept(blockpos, this, direction);
		}
	}

	/**
	 * EventListener wenn Nachbar des Blockes sich ändert - Aktualisiert weitere
	 * Blöcke
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param world
	 *            Welt in der der Block steht
	 * @param pos
	 *            Position des Blockes
	 * @param neighborBlock
	 *            Nachbarblock der sich ändert
	 * @param fromPos
	 *            Position von dem sich änderndem Block
	 * @param isMoving
	 *            Gibt an ob der Block sich bewegt
	 */
	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
		System.out.println(".");
		super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
		Direction direction = null;
		if (!state.isValidPosition(world, pos)) {
			TileEntity tileentity = state.hasTileEntity() ? world.getTileEntity(pos) : null;
			spawnDrops(state, world, pos, tileentity);
			world.removeBlock(pos, false);
			for (Direction d : Direction.values())
				world.notifyNeighborsOfStateChange(pos.offset(d), this);
			return;
		}
		if (state.has(HorizontalBlock.HORIZONTAL_FACING)) {
			direction = state.get(HorizontalBlock.HORIZONTAL_FACING);
			/*if (net.minecraftforge.event.ForgeEventFactory
					.onNeighborNotify(world, pos, world.getBlockState(pos), java.util.EnumSet.of(direction.getOpposite()), false).isCanceled()) {
				ac = false;
				return;
			}*/
		}
		if (pos != null && fromPos != null)
			System.out.println(pos.toString() + ", " + fromPos.toString());
		/*
		 * if(fromPos!=null&&pos.up().equals(fromPos)){ ac = false;
		 * System.out.println("UP"); return;}
		 * if(fromPos!=null&&pos.down().equals(fromPos)){ ac = false;
		 * System.out.println("down"); return;}
		 */
		getPowerOnSides(world, pos, state);
		if (fromPos != null && world.getBlockState(fromPos).getBlock() instanceof LogicBlock) {
			if (world.getBlockState(fromPos).has(FACING) && world.getBlockState(fromPos).get(FACING) == state.get(FACING).getOpposite()) {
				System.out.println("Connected");
				// world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
				// world.notifyNeighborsOfStateExcept(fromPos, neighborBlock,
				// world.getBlockState(fromPos).get(FACING).getOpposite());
				// world.notifyNeighborsOfStateChange(pos.up(),
				// world.getBlockState(pos.up()).getBlock());
				if (neighborBlock instanceof LogicBlock)
					getPowerOnSides(world, pos, state);
			} else if (world.getBlockState(fromPos).has(FACING) && world.getBlockState(fromPos).get(FACING) == state.get(FACING)) {
				System.out.println("OtherSide");
			} else {
				System.out.println("INPUT");
				// if (ac)
				// return;
				// ac = true;
				// this.neighborChanged(state, world, pos, null, null, false);
				BlockPos blockpos = pos.offset(direction.getOpposite());
				world.neighborChanged(blockpos, this, pos);
				// if(ad)
				// world.notifyNeighborsOfStateExcept(fromPos, neighborBlock, direction);
				// world.notifyNeighborsOfStateExcept(blockpos, this, direction);
				// ad=!ad;
				// this.notifyNeighborExceptFacingLogicBlock(world, blockpos, this);
				// BlockPos blockpos = pos.offset(direction.getOpposite());
				// this.notifyNeighborsOfStateExceptLogicBlock(world, blockpos, this);
				// ac = false;
			}
		} else {
			if (direction != null) {
				System.out.println("--");
				BlockPos blockpos = pos.offset(direction.getOpposite());
				world.neighborChanged(blockpos, this, pos);
			}
		}
	}

	/**
	 * EventListener wenn Block gesetzt wird
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param world
	 *            Welt in der der Block steht
	 * @param pos
	 *            Position des Blockes
	 * @param oldstate
	 *            alter Blockstate vor dem Ersetzen
	 * @param isMoving
	 *            Gibt an ob der Block sich bewegt
	 */
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (aa)
			return;
		aa = true;
		this.neighborChanged(state, worldIn, pos, null, null, false);
		input_directions.clear();
		this.addInput(state.get(FACING).rotateY(), pos, worldIn);
		this.addInput(state.get(FACING).rotateYCCW(), pos, worldIn);
		this.addInput(state.get(FACING), pos, worldIn);
		aa = false;
	}



	/**
	 *** privat*** Hinzufügen eines neuen Inputes
	 * 
	 * @param d
	 *            Seite an der der neue Input ist
	 * @param pos
	 *            Position des Blockes, an dem der Input hinzugefügt wird
	 * @param world
	 *            Welt des Blockes
	 */
	private void addInput(Direction d, BlockPos pos, World world) {
		input_directions.add(d);
		if (!RedstoneValue)
			switch (input_directions.size()) {
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

	/**
	 *** privat*** Löschen aller Inputs
	 * 
	 * @param pos
	 *            Position des Blockes, an dem die Inputs gelöscht werden
	 * @param world
	 *            Welt des Blockes
	 */
	private void claerInput(BlockPos pos, World world) {
		input_directions.clear();
		if (!RedstoneValue) {
			world.setBlockState(pos, world.getBlockState(pos).with(INPUT1, InputSide.NONE), 2);
			world.setBlockState(pos, world.getBlockState(pos).with(INPUT2, InputSide.NONE), 2);
			world.setBlockState(pos, world.getBlockState(pos).with(INPUT3, InputSide.NONE), 2);
		}
	}

	/**
	 *** privat*** Aktualisieren der Parameter
	 * 
	 * @param pos
	 *            Position des Blockes
	 * @param world
	 *            Welt des Blockes
	 * @param input
	 *            Redstone Werte der Inputs
	 * @param output
	 *            Derzeitiger REdstoneWert des Outputs
	 */
	private void SetInput_OutputSide(BlockPos pos, World world, ArrayList<Integer> input, int output) {
		BlockState help = world.getBlockState(pos);
		if (RedstoneValue && help.has(INPUT3_VALUE) && help.has(INPUT2_VALUE) && help.has(INPUT1_VALUE)) {
			switch (input_directions.size()) {
				case 3 :
					help = help.with(INPUT3_VALUE, RedstonePower.GetEnum(input.get(2)));
				case 2 :
					help = help.with(INPUT2_VALUE, RedstonePower.GetEnum(input.get(1)));
				case 1 :
					help = help.with(INPUT1_VALUE, RedstonePower.GetEnum(input.get(0)));
			}
		}
		if (help.has(POWER)) {
			world.setBlockState(pos, help.with(POWER, output), 2);
		}
	}

	/**
	 *** protected*** Ließt alle Redstone Inputs ein und gibt den neuen Output aus
	 * 
	 * @param world
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes
	 * @param state
	 *            Blockstate des Blockes
	 */
	protected void getPowerOnSides(World world, BlockPos pos, BlockState state) {
		if (ab)
			return;
		ab = true;
		ArrayList<Integer> inputs = new ArrayList();
		for (Direction direct : input_directions) {
			inputs.add(this.getPowerOnSide2(world, pos, direct));
		}
		ab = false;
		if (inputs.size() <= 0)
			SetInput_OutputSide(pos, world, inputs, 0);
		else
			SetInput_OutputSide(pos, world, inputs, logic(inputs));
	}

	/**
	 *** protected*** Ließt den Redstone Input an einer Seite an
	 * 
	 * @param world
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes, der den Redstonewert bekommt
	 * @param side
	 *            Seite an der der Redstonewert eingegeben wird
	 */
	protected int getPowerOnSide(World world, BlockPos pos, Direction side) {
		BlockPos redstoneBlockPos = pos.offset(side);
		BlockState redstoneBlockState = world.getBlockState(redstoneBlockPos);
		Block redstoneBlock = redstoneBlockState.getBlock();

		if(redstoneBlock==Blocks.REDSTONE_BLOCK||(redstoneBlockState.has(BlockStateProperties.POWERED)&&redstoneBlockState.get(BlockStateProperties.POWERED))){
			return 15;
		} else if(redstoneBlock==Blocks.REDSTONE_WIRE){
			return redstoneBlockState.get(RedstoneWireBlock.POWER);
		} else if(redstoneBlockState.canProvidePower()){
			return Math.max(world.getRedstonePower(pos, side.getOpposite()),world.getStrongPower(redstoneBlockPos));
		} else if(redstoneBlockState.isSolid()){
			return world.isBlockPowered(redstoneBlockPos) ? world.getRedstonePower(pos, side) : 0;
		} else{
			return 0;
		}
		//if (worldIn.getBlockState(pos).canProvidePower() || worldIn.getBlockState(pos).isSolid())
		//return (worldIn.getBlockState(pos).canProvidePower() || worldIn.getBlockState(pos).isSolid()) ? worldIn.getRedstonePower(pos, side) : 0;
	}

	protected int getPowerOnSide2(World worldIn, BlockPos pos, Direction side) {
        Direction direction = side;
          BlockPos blockpos = pos.offset(direction);
          int i = worldIn.getRedstonePower(blockpos, direction);
          if (i >= 15) {
             return i;
          } else {
             BlockState blockstate = worldIn.getBlockState(blockpos);
             return Math.max(i, blockstate.getBlock() == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : 0);
          }
    }

	/**
	 * Abstrakte Methode Gibt die Logik des Blockes an
	 * 
	 * @param inputs
	 *            Alle Redstone Input Values
	 * @return Neuer Output
	 */
	protected abstract int logic(List<Integer> inputs);
}
