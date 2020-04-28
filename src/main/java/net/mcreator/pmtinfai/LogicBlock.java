
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import java.util.Random;
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
	// weitere Variablen
	private static boolean aa = false;
	// boolean Variablen zum Abfangen von Multithreading
	// Konstrukter
	public LogicBlock() {
		super(Block.Properties.create(Material.MISCELLANEOUS).sound(SoundType.STEM).hardnessAndResistance(1f, 10f).lightValue(0));
		// Laden der Default Properties der Blöcke
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
		return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite())
				.with(INPUT1, InputSide.GetEnum(context.getPlacementHorizontalFacing()))
				.with(INPUT2, InputSide.GetEnum(context.getPlacementHorizontalFacing().rotateY()))
				.with(INPUT3, InputSide.GetEnum(context.getPlacementHorizontalFacing().rotateYCCW()));
	}

	/**
	 * Gibt die Tickrate des Blockes zur�ck
	 * 
	 * @param worldIn
	 * 
	 *            Teil der Welt des Blockes
	 * @return Die aktuelle Tickrate des Blockes
	 */
	public int tickRate(IWorldReader worldIn) {
		return 1;
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
		return side == null ? false : (state.get(FACING) == side || existInputDirections(state, side));
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
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!state.isValidPosition(worldIn, pos)) {
			TileEntity tileentity = state.hasTileEntity() ? worldIn.getTileEntity(pos) : null;
			spawnDrops(state, worldIn, pos, tileentity);
			worldIn.removeBlock(pos, false);
			for (Direction d : Direction.values())
				worldIn.notifyNeighborsOfStateChange(pos.offset(d), this);
			return;
		}
		if (state.get(POWER) != this.getPowerOnSides(worldIn, pos, state) && !worldIn.getPendingBlockTicks().isTickPending(pos, this)) {
			update(state, worldIn, pos, null, this.getPowerOnSides(worldIn, pos, state));
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
		aa = false;
	}

	/**
	 * EventListener wenn Block durch Entity gesetzt wurde
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param world
	 *            Welt in der der Block steht
	 * @param pos
	 *            Position des Blockes
	 * @param placer
	 *            Entity die den Block gesetzt hat
	 * @param stack
	 *            Stack des Items
	 */
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (this.getPowerOnSides(worldIn, pos, state) > 0) {
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
		}
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
		BlockState blockstate = world.getBlockState(pos);
		if (blockstate.has(INPUT1) && blockstate.get(INPUT1).equals(InputSide.NONE)) {
			world.setBlockState(pos, blockstate.with(INPUT1, InputSide.GetEnum(d)), 2);
		} else if (blockstate.has(INPUT2) && blockstate.get(INPUT2).equals(InputSide.NONE)) {
			world.setBlockState(pos, blockstate.with(INPUT2, InputSide.GetEnum(d)), 2);
		} else if (blockstate.has(INPUT3) && blockstate.get(INPUT3).equals(InputSide.NONE)) {
			world.setBlockState(pos, blockstate.with(INPUT3, InputSide.GetEnum(d)), 2);
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
		world.setBlockState(pos, world.getBlockState(pos).with(INPUT1, InputSide.NONE), 2);
		world.setBlockState(pos, world.getBlockState(pos).with(INPUT2, InputSide.NONE), 2);
		world.setBlockState(pos, world.getBlockState(pos).with(INPUT3, InputSide.NONE), 2);
	}

	private boolean existInputDirections(BlockState blockstate, Direction d) {
		if (blockstate.has(INPUT1) && d == ((InputSide) blockstate.get(INPUT1)).GetDirection())
			return true;
		if (blockstate.has(INPUT2) && d == ((InputSide) blockstate.get(INPUT2)).GetDirection())
			return true;
		if (blockstate.has(INPUT3) && d == ((InputSide) blockstate.get(INPUT3)).GetDirection())
			return true;
		return false;
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
	protected int getPowerOnSides(World world, BlockPos pos, BlockState blockstate) {
		ArrayList<Integer> inputs = new ArrayList();
		int out;
		if (blockstate.has(INPUT1))
			inputs.add(this.getPowerOnSide(world, pos, ((InputSide) blockstate.get(INPUT1)).GetDirection()));
		if (blockstate.has(INPUT2))
			inputs.add(this.getPowerOnSide(world, pos, ((InputSide) blockstate.get(INPUT2)).GetDirection()));
		if (blockstate.has(INPUT3))
			inputs.add(this.getPowerOnSide(world, pos, ((InputSide) blockstate.get(INPUT3)).GetDirection()));
		if (inputs.size() <= 0) {
			out = 0;
			// world.setBlockState(pos, blockstate.with(POWER, 0), 2);
		} else {
			out = logic(inputs);
			// world.setBlockState(pos, blockstate.with(POWER, out), 2);
		}
		return out;
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
		Direction direction = side.getOpposite();
		BlockPos blockpos = pos.offset(direction);
		int i = world.getRedstonePower(blockpos, direction);
		if (i >= 15) {
			return i;
		} else {
			BlockState blockstate = world.getBlockState(blockpos);
			return Math.max(i, blockstate.getBlock() == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : 0);
		}
	}

	/**
	 * Wechselt den blockstate, fals
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param worldIn
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes, der den Tick ausf�hren soll
	 * @param random
	 *            Ein Java Random Element f�r Zuf�llige Ticks
	 */
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		update(state, worldIn, pos, random, this.getPowerOnSides(worldIn, pos, state));
	}

	/**
	 * Wechselt den blockstate, fals
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param worldIn
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes, der geupdated werden soll
	 * @param random
	 *            Ein Java Random Element f�r Zuf�llige Ticks
	 * @param clalculatedOutput
	 *            Auf diesen Wert soll der Output des blockes gesetzt werden
	 */
	public void update(BlockState state, World worldIn, BlockPos pos, Random random, int calculatedOutput) {
		if (state.get(POWER) > 0 && calculatedOutput == 0) {
			worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf("0")), 3);
		} else if (calculatedOutput > 0 && state.get(POWER) != calculatedOutput) {
			worldIn.setBlockState(pos, state.with(POWER, calculatedOutput), 3);
		}
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
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
