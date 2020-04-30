
package net.mcreator.pmtinfai;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import java.util.Random;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

public abstract class LogicBlock extends Block {
	private final Item InputItem = Items.REDSTONE;
	private final Item OutputItem = Items.REDSTONE_TORCH;
	// Properties des Blocks
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	public static final EnumProperty<InputSide> INPUT1 = EnumProperty.create("input1_side", InputSide.class);
	public static final EnumProperty<InputSide> INPUT2 = EnumProperty.create("input2_side", InputSide.class);
	public static final EnumProperty<InputSide> INPUT3 = EnumProperty.create("input3_side", InputSide.class);
	public static final EnumProperty<InputSide> OUTPUT = EnumProperty.create("output", InputSide.class);
	// weitere Variablen
	private static boolean aa = false;
	private HashMap<String, Boolean> test = null;
	private String test2 = null;
	// boolean Variablen zum Abfangen von Multithreading
	// Konstrukter
	public LogicBlock() {
		super(Block.Properties.create(Material.MISCELLANEOUS).sound(SoundType.STEM).hardnessAndResistance(0f, 0f).lightValue(0));
		// Laden der Default Properties der BlÃ¶cke
		this.setDefaultState(this.stateContainer.getBaseState().with(POWER, Integer.valueOf(0)).with(INPUT1, InputSide.NONE)
				.with(INPUT2, InputSide.NONE).with(INPUT3, InputSide.NONE).with(OUTPUT, InputSide.NONE));
		// GetAllStates("(A&(B&C))");
	}

	/**
	 * Initalisiert die Parameter
	 * 
	 * @param builder
	 *            Builder des Blockes
	 */
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(POWER).add(INPUT1).add(INPUT2).add(INPUT3).add(OUTPUT);
	}

	/**
	 * Gibt die Tickrate des Blockes zurück
	 * 
	 * @param worldIn
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
		return ((InputSide) blockState.get(OUTPUT)).GetDirection() == side ? blockState.get(POWER) : 0;
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
	 * Wechselt den Blockstate, fals
	 * 
	 * @param state
	 *            Blockstate des Blockes
	 * @param worldIn
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes, der den Tick ausführen soll
	 * @param random
	 *            Ein Java Random Element für Zufällige Ticks
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
	 *            Ein Java Random Element für Zufällige Ticks
	 * @param clalculatedOutput
	 *            Auf diesen Wert soll der Output des blockes gesetzt werden
	 * 
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
		return side == null ? false : (((InputSide) state.get(OUTPUT)).GetDirection() == side || existInputDirections(state, side));
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
	 * Setzt neune In/Outputs
	 * 
	 * @param output
	 *            Neue Seite des Outputs
	 * @param inputs
	 *            Neue Seiten des Inputs
	 * @param world
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes
	 */
	public void SetInput_OutputDirection(Direction output, List<Direction> inputs, World world, BlockPos pos) {
		BlockState blockstate = world.getBlockState(pos);
		if (inputs.contains(output)) {
			System.err.println("Output = Input!");
			return;
		}
		blockstate = blockstate.with(OUTPUT, InputSide.GetEnum(output));
		this.clearInput(pos, world);
		for (Direction input : inputs)
			this.addInput(input, pos, world);
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
		Direction direction = ((InputSide) state.get(OUTPUT)).GetDirection();
		if(direction==null)
			return;
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
	 *** private*** Hinzufügen eines neuen Inputes
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
		if (this.existInputDirections(blockstate, d))
			return;
		if (blockstate.has(INPUT1) && blockstate.get(INPUT1).equals(InputSide.NONE)) {
			world.setBlockState(pos, blockstate.with(INPUT1, InputSide.GetEnum(d)), 2);
		} else if (blockstate.has(INPUT2) && blockstate.get(INPUT2).equals(InputSide.NONE)) {
			world.setBlockState(pos, blockstate.with(INPUT2, InputSide.GetEnum(d)), 2);
		} else if (blockstate.has(INPUT3) && blockstate.get(INPUT3).equals(InputSide.NONE)) {
			world.setBlockState(pos, blockstate.with(INPUT3, InputSide.GetEnum(d)), 2);
		}
		refreshInput(pos, world);
	}

	/**
	 *** private*** Entfernen eines Inputes
	 * 
	 * @param d
	 *            Seite an der der zu löschende Input ist
	 * @param pos
	 *            Position des Blockes, an dem der Input entfernt wird
	 * @param world
	 *            Welt des Blockes
	 */
	private void removeInput(Direction d, BlockPos pos, World world) {
		BlockState blockstate = world.getBlockState(pos);
		if (!this.existInputDirections(blockstate, d))
			return;
		if (blockstate.has(INPUT1) && blockstate.get(INPUT1).equals(InputSide.GetEnum(d))) {
			world.setBlockState(pos, blockstate.with(INPUT1, InputSide.NONE), 2);
		} else if (blockstate.has(INPUT2) && blockstate.get(INPUT2).equals(InputSide.GetEnum(d))) {
			world.setBlockState(pos, blockstate.with(INPUT2, InputSide.NONE), 2);
		} else if (blockstate.has(INPUT3) && blockstate.get(INPUT3).equals(InputSide.GetEnum(d))) {
			world.setBlockState(pos, blockstate.with(INPUT3, InputSide.NONE), 2);
		}
		refreshInput(pos, world);
	}

	/**
	 * Input und Output Ändern
	 * 
	 * @param slot
	 *            SlotID des ändernden Slot
	 * @param world
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes
	 * @param item
	 *            Item im GUI
	 */
	public void changeInput(int slot, BlockPos pos, World world, Item item) {
		BlockState blockstate = world.getBlockState(pos);
		Direction d = SlotIDtoDirection(slot).getOpposite();
		if (item == InputItem) {
			if (existInputDirections(blockstate, d)) {
				return;
			}
			System.out.println("Change Input in slot \'" + slot + "\'");
			if (d == ((InputSide) blockstate.get(OUTPUT)).GetDirection()) {
				world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.NONE));
			}
			addInput(d, pos, world);
		} else if (item == OutputItem) {
			if (d == ((InputSide) blockstate.get(OUTPUT)).GetDirection()) {
				return;
			}
			System.out.println("Change Output in slot \'" + slot + "\'");
			removeInput(d, pos, world);
			world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.GetEnum(d)));
			removeInput(d, pos, world);
		} else {
			System.out.println("Change in slot \'" + slot + "\'");
			if (d == ((InputSide) blockstate.get(OUTPUT)).GetDirection()) {
				world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.NONE));
			}
			removeInput(d, pos, world);
		}
	}

	/**
	 * Set In-&Outputs
	 * 
	 * @param inputs
	 *            Seiten des Inputes
	 * @param output
	 *            Seite des Outputs
	 * @param pos
	 *            Position des Blockes, an dem der Input entfernt wird
	 * @param world
	 *            Welt des Blockes
	 */
	@Deprecated
	public void setPort(Direction[] inputs, Direction output, World world, BlockPos pos) {
		clearInput(pos, world);
		world.setBlockState(pos, world.getBlockState(pos).with(OUTPUT, InputSide.GetEnum(output)));
		for (Direction input : inputs) {
			addInput(input, pos, world);
		}
		BlockState state = world.getBlockState(pos);
		update(state, world, pos, null, this.getPowerOnSides(world, pos, state));
	}

	/**
	 *** private*** Löschen aller Inputs
	 * 
	 * @param pos
	 *            Position des Blockes, an dem die Inputs gelöscht werden
	 * @param world
	 *            Welt des Blockes
	 */
	private void clearInput(BlockPos pos, World world) {
		world.setBlockState(pos, world.getBlockState(pos).with(INPUT1, InputSide.NONE), 2);
		world.setBlockState(pos, world.getBlockState(pos).with(INPUT2, InputSide.NONE), 2);
		world.setBlockState(pos, world.getBlockState(pos).with(INPUT3, InputSide.NONE), 2);
		BlockState state = world.getBlockState(pos);
		update(state, world, pos, null, this.getPowerOnSides(world, pos, state));
	}

	/**
	 *** private*** Refreshen der Inputs
	 * 
	 * @param pos
	 *            Position des Blockes
	 * @param world
	 *            Welt des Blockes
	 */
	private void refreshInput(BlockPos pos, World world) {
		BlockState blockstate = world.getBlockState(pos);
		if (blockstate.has(INPUT1) && blockstate.get(INPUT1).equals(InputSide.NONE)) {
			if (blockstate.has(INPUT3) && !blockstate.get(INPUT3).equals(InputSide.NONE))
				world.setBlockState(pos, blockstate.with(INPUT1, blockstate.get(INPUT3)).with(INPUT3, InputSide.NONE));
			else if (blockstate.has(INPUT2) && !blockstate.get(INPUT2).equals(InputSide.NONE))
				world.setBlockState(pos, blockstate.with(INPUT1, blockstate.get(INPUT2)).with(INPUT2, InputSide.NONE));
		}
		if (blockstate.has(INPUT2) && blockstate.get(INPUT2).equals(InputSide.NONE)) {
			if (blockstate.has(INPUT3) && !blockstate.get(INPUT3).equals(InputSide.NONE))
				world.setBlockState(pos, blockstate.with(INPUT2, blockstate.get(INPUT3)).with(INPUT3, InputSide.NONE));
		}
	}

	/**
	 *** private*** Abfrage ob Input existiert
	 * 
	 * @param blockstate
	 *            Blockstate des Blockes
	 * @param d
	 *            Direction des Blockes
	 */
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
		if (blockstate.has(INPUT1) && blockstate.get(INPUT1) != InputSide.NONE)
			inputs.add(this.getPowerOnSide(world, pos, ((InputSide) blockstate.get(INPUT1)).GetDirection()));
		if (blockstate.has(INPUT2) && blockstate.get(INPUT2) != InputSide.NONE)
			inputs.add(this.getPowerOnSide(world, pos, ((InputSide) blockstate.get(INPUT2)).GetDirection()));
		if (blockstate.has(INPUT3) && blockstate.get(INPUT3) != InputSide.NONE)
			inputs.add(this.getPowerOnSide(world, pos, ((InputSide) blockstate.get(INPUT3)).GetDirection()));
		if (inputs.size() <= 0)
			return 0;
		else
			return logic(inputs);
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
	 *** protected*** SlotId anhand der Direction
	 * 
	 * @param d
	 *            Seite des SlotIds
	 * @return SlotID
	 */
	protected int DirectiontoSlotID(Direction d) {
		if (d == Direction.WEST)
			return 0;
		if (d == Direction.NORTH)
			return 1;
		if (d == Direction.EAST)
			return 2;
		if (d == Direction.SOUTH)
			return 3;
		return -1;
	}

	/**
	 *** protected*** Direction anhand der SlotId
	 * 
	 * @param slot
	 *            ID des Slot
	 * @return Direction des SlotIDs
	 */
	protected Direction SlotIDtoDirection(int slot) {
		switch (slot) {
			case 0 :
				return Direction.WEST;
			case 1 :
				return Direction.NORTH;
			case 2 :
				return Direction.EAST;
			case 3 :
				return Direction.SOUTH;
			default :
				return null;
		}
	}

	/**
	 *** protected*** Gibt die Logik des Blockes an
	 * 
	 * @param inputs
	 *            Alle Redstone Input Values
	 * @return Neuer Output
	 */
	protected int logic(List<Integer> inputs) {
		if (test == null)
			return 0;
		String erg = "";
		for (int f : inputs)
			erg += f > 0 ? 'T' : 'F';
		while (erg.length() < 3) {
			erg += 'N';
		}
		return test != null && test.get(erg) ? Collections.max(inputs) : 0;
	}

	/**
	 * Erstellt die neue Warheitstabelle
	 * 
	 * @param exp
	 *            Expresion der neuen Logik
	 * @param world
	 *            Welt des Blockes
	 * @param pos
	 *            Position des Blockes
	 */
	public void GetAllStates(String exp, World world, BlockPos pos) {
		if (exp == null || test2 == exp)
			return;
		test2 = exp;
		System.out.println("Change output to: " + exp);
		if (exp == "none") {
			test = null;
			return;
		}
		String[] exp_help = exp.split(",");
		String[] cases3 = new String[]{"TFF", "TFT", "TTF", "TTT", "FFF", "FFT", "FTF", "FTT"};
		String[] cases2 = new String[]{"TF", "TT", "FF", "FT"};
		String[] cases1 = new String[]{"T", "F"};
		List<String[]> cases = new ArrayList<>();
		cases.add(cases3);
		cases.add(cases2);
		cases.add(cases1);
		char[] replace = new char[]{'A', 'B', 'C'};
		HashMap<String, Boolean> erg = new HashMap<>();
		List<Integer> inputs = new ArrayList<>();
		for (int e = 0; e < cases.size(); e++) {
			for (int f = 0; f < cases.get(e).length; f++) {
				String exp2 = exp_help[e];
				inputs.clear();
				char[] fchar = cases.get(e)[f].toCharArray();
				for (int g = 0; g < fchar.length; g++) {
					exp2 = exp2.replace(replace[g], fchar[g]);
				}
				erg.put(cases.get(e)[f], calculate(exp2));
				System.out.println(cases.get(e)[f] + "-" + erg.get(cases.get(e)[f]));
			}
		}
		test = erg;
	}

	/**
	 ***private*** Parsed eine String Expression in eine Bool Expression
	 * @param exp Expresion die geparsed werden soll
	 * @return Boolisches Ergebnis der Expression
	 */

	private static boolean calculate(String exp) {
		List<Character> allowed = new ArrayList<>();
		allowed.add('T');
		allowed.add('F');
		allowed.add('(');
		allowed.add(')');
		allowed.add('&');
		allowed.add('|');
		allowed.add('!');
		// Checking Expression
		char[] help = exp.toCharArray();
		List<Character> x = new ArrayList<>();
		for (int f = 0; f < help.length; f++) {
			if (!allowed.contains(help[f]))
				return false;
			x.add(help[f]);
		}
		// Calculate
		for (int f = 0; f < x.size(); f++) {
			if (x.get(f) == ')') {
				for (int g = f - 1; g >= 0; g--) {
					if (x.get(g) == '(') {
						if (f - g == 4) {
							if (x.get(g + 2) == '&') {
								if (x.get(g + 1) == 'T' && x.get(g + 3) == 'T') {
									x.set(f, 'T');
								} else {
									x.set(f, 'F');
								}
								for (int h = f - 1; h > g - 1; h--) {
									x.remove(h);
								}
								f = 0;
								g = -1;
							} else if (x.get(g + 2) == '|') {
								if (x.get(g + 1) == 'F' && x.get(g + 3) == 'F') {
									x.set(f, 'F');
								} else {
									x.set(f, 'T');
								}
								for (int h = f - 1; h > g - 1; h--) {
									x.remove(h);
								}
								f = 0;
								g = -1;
							}
						} else {
							if (x.get(g + 1) == '!') {
								if (x.get(f - 1) == 'F')
									x.set(f, 'T');
								else {
									x.set(f, 'F');
								}
								for (int h = f - 1; h > g - 1; h--) {
									x.remove(h);
								}
								f = 0;
								g = -1;
							}
						}
					}
				}
			}
		}
		return x.get(0) == 'T' ? true : false;
	}
}
