
package net.mcreator.pmtinfai.block;

import io.netty.buffer.Unpooled;
import net.mcreator.pmtinfai.MKLGItems;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.enums.InputSide;
import net.mcreator.pmtinfai.enums.LogicSpecies;
import net.mcreator.pmtinfai.gui.LogicBlockGui;
import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import java.util.*;
import java.util.stream.IntStream;

@PMTINFAIElements.ModElement.Tag
public class LogicBlock extends PMTINFAIElements.ModElement {
    @ObjectHolder("pmtinfai:logicblock")
    public static final Block block = null;
    @ObjectHolder("pmtinfai:logicblock")
    public static final TileEntityType<CustomTileEntity> tileEntityType = null;

    public LogicBlock(PMTINFAIElements instance) {
        super(instance, 12);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @Override
    public void initElements() {
        elements.blocks.add(CustomBlock::new);
        elements.items
                .add(() -> new BlockItem(block, new Item.Properties().group(LogicBlocksItemGroup.tab)).setRegistryName(block.getRegistryName()));
    }

    @SubscribeEvent
    public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("logicblock"));
    }

    public static class CustomBlock extends MKLGBlocks {
        // Properties des Blocks
        public static final EnumProperty<InputSide> OUTPUT = EnumProperty.create("output", InputSide.class);
        public static final EnumProperty<InputSide> INPUT1 = EnumProperty.create("input1_side", InputSide.class);
        public static final EnumProperty<InputSide> INPUT2 = EnumProperty.create("input2_side", InputSide.class);
        public static final EnumProperty<InputSide> INPUT3 = EnumProperty.create("input3_side", InputSide.class);
        public static final EnumProperty<LogicSpecies> LOGIC = EnumProperty.create("logic", LogicSpecies.class);

        // boolean Variablen zum Abfangen von Multithreading
        public CustomBlock() {
            super(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0f, 0f).lightValue(0));
            setRegistryName("logicblock");
            // Laden der Default Properties der Bl�cke
            this.setDefaultState(this.stateContainer.getBaseState().with(INPUT1, InputSide.NONE)
                    .with(INPUT2, InputSide.NONE).with(INPUT3, InputSide.NONE).with(LOGIC, LogicSpecies.NONE).with(OUTPUT,InputSide.NONE));
        }

        // --------------------------------------------Getter------------

        /**
         * Abfgage wie stark die WeakPower(direkte Redstoneansteuerung) ist
         *
         * @param blockState  BlockState des Blockes
         * @param blockAccess Angabe welche Art der Block ist
         * @param pos         Position des Blockes
         * @param side        Seite an der die Power abgefragt wird
         * @return Gibt die RedstonePower(0-15) zur�ck
         */
        @Override
        public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
            return blockState.get(OUTPUT).GetDirection() == side ? blockState.get(POWER) : 0;
        }
        /**
         * Erstellt die neue Warheitstabelle
         *
         * @param exp   Expresion der neuen Logik
         * @param world Welt des Blockes
         * @param pos   Position des Blockes
         */
        public void GetAllStates(String exp, World world, BlockPos pos) {
            if (exp == null || (world.getBlockState(pos).hasTileEntity() && getTE(world, pos).GetTest2() != null && getTE(world, pos).GetTest2().equals(exp)))
                return;
            getTE(world, pos).SetTest2(exp);
            world.setBlockState(pos, world.getBlockState(pos).with(LOGIC, LogicSpecies.GetEnum(exp)));
            if (exp.equals("none")) {
                getTE(world, pos).SetActive(false);
                return;
            }
            getTE(world, pos).SetActive(true);
            String[] exp_help = exp.split(",");
            String[] cases3 = new String[]{"TFF", "TFT", "TTF", "TTT", "FFF", "FFT", "FTF", "FTT"};
            String[] cases2 = new String[]{"TF", "TT", "FF", "FT"};
            String[] cases1 = new String[]{"T", "F"};
            List<String[]> cases = new ArrayList<>();
            boolean[] max = new boolean[]{true, true, true};
            cases.add(cases3);
            cases.add(cases2);
            cases.add(cases1);
            char[] replace = new char[]{'A', 'B', 'C'};
            List<Boolean> erg = new ArrayList<>();
            for (int e = 0; e < cases.size(); e++) {
                for (int f = 0; f < cases.get(e).length; f++) {
                    String exp2 = exp_help[e];
                    char[] fchar = cases.get(e)[f].toCharArray();
                    for (int g = 0; g < fchar.length; g++) {
                        exp2 = exp2.replace(replace[g], fchar[g]);
                    }
                    StringBuilder help = new StringBuilder(cases.get(e)[f]);
                    while (help.length() < 3)
                        help.append("N");
                    boolean erg_calculate = calculate(exp2);
                    erg.add(erg_calculate);
                    if (max[e] && erg_calculate) {
                        max[e] = false;
                    }
                }
            }
            getTE(world, pos).SetHashMap(erg);
            if (max[0]) {
                if (max[1]) {
                    if (max[2]) {
                        getTE(world, pos).SetMaxInput(0);
                    } else {
                        getTE(world, pos).SetMaxInput(1);
                    }
                } else {
                    getTE(world, pos).SetMaxInput(2);
                }
            } else {
                getTE(world, pos).SetMaxInput(3);
            }
            update(world.getBlockState(pos), world, pos, getPowerOnSides(world, pos, world.getBlockState(pos)));
        }

        // -------------------------------------Eventlistener----------------------

        /**
         * EventListener wenn ein Rechtsklick auf den Block durchgef�hrt wird
         *
         * @param state  BlockState des Blockes
         * @param world  Welt des Blockes
         * @param pos    Position des Blockes
         * @param entity Player der den Rechtsklick ausf�hrt
         * @param hand   Hand die das ausf�hrt
         * @param hit    Hit
         */
        @Override
        public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult hit) {
            super.onBlockActivated(state, world, pos, entity, hand, hit);
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            if (entity instanceof ServerPlayerEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) entity, new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new StringTextComponent("Logic Block");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
                        return new LogicBlockGui.GuiContainerMod(id, inventory,
                                new PacketBuffer(Unpooled.buffer()).writeBlockPos(new BlockPos(x, y, z)));
                    }
                }, new BlockPos(x, y, z));
            }
            return true;
        }

        /**
         * Abfrage ob Redstone sich an der Seite verbinden kann
         *
         * @param state Blockstate des Blockes
         * @param world Angabe welche Art der Block ist
         * @param pos   Position des Blockes
         * @param side  Seite die Abgefragt wird
         * @return Gibt zur�ck ob der Redstone sich verbinden kann
         */
        @Override
        public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
            return side != null && (state.get(OUTPUT).GetDirection() == side || existInputDirections(state, side));
        }

        // ------------------------------Others
        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return new CustomTileEntity();
        }

        /**
         * Initalisiert die Parameter
         *
         * @param builder Builder des Blockes
         */
        @Override
        protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
            builder.add(POWER).add(INPUT1).add(INPUT2).add(INPUT3).add(OUTPUT).add(LOGIC);
        }


        /**
         * Wechselt den blockstate, fals
         *
         * @param state            Blockstate des Blockes
         * @param worldIn          Welt des Blockes
         * @param pos              Position des Blockes, der geupdated werden soll
         * @param calculatedOutput Auf diesen Wert soll der Output des blockes gesetzt werden
         */
        public void update(BlockState state, World worldIn, BlockPos pos, int calculatedOutput) {
            if (state.get(POWER) > 0 && calculatedOutput == 0) {
                worldIn.setBlockState(pos, state.with(POWER, 0), 3);
            } else if (calculatedOutput > 0 && state.get(POWER) != calculatedOutput) {
                worldIn.setBlockState(pos, state.with(POWER, calculatedOutput), 3);
            }
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
            InputSide i = state.get(OUTPUT);
            if (calculatedOutput == state.get(POWER) && getTE(worldIn, pos).isSetActive()) {
                worldIn.setBlockState(pos, state.with(OUTPUT, InputSide.NONE), 3);
                worldIn.setBlockState(pos, state.with(OUTPUT, i), 3);
                getTE(worldIn, pos).setSetActive(false);
            }
        }

        /**
         * Input und Output �ndern
         *
         * @param slot  SlotID des �ndernden Slot
         * @param world Welt des Blockes
         * @param pos   Position des Blockes
         * @param item  Item im GUI
         */
        public boolean[] changeInput(int slot, BlockPos pos, World world, Item item) {
            int[] help = new int[]{0, 0};
            BlockState blockstate = world.getBlockState(pos);
            Direction d = SlotIDtoDirection(slot).getOpposite();
            if (item == MKLGItems.InputItem) {
                if (existInputDirections(blockstate, d)) {
                    return IO_State(blockstate, 0, 0, world, pos);
                }
                if (d == blockstate.get(OUTPUT).GetDirection()) {
                    world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.NONE));
                }
                addInput(d, pos, world);
                help[0] = 1;
            } else if (item == MKLGItems.OutputItem) {
                if (d == blockstate.get(OUTPUT).GetDirection()) {
                    return IO_State(blockstate, 0, 0, world, pos);
                }
                removeInput(d, pos, world);
                world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.GetEnum(d)));
                removeInput(d, pos, world);
                help[1] = 1;
            } else {
                if (d == blockstate.get(OUTPUT).GetDirection()) {
                    world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.NONE));
                }
                removeInput(d, pos, world);
            }
            return IO_State(blockstate, help[0], help[1], world, pos);
        }

        /**
         * Animiere die Partikel
         *
         * @param stateIn BlockState des Blockes
         * @param worldIn Welt des Blockes
         * @param pos     Position des Blockes
         * @param rand    RandomGenerator
         */
        @OnlyIn(Dist.CLIENT)
        @Override
        public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
            CustomTileEntity ct = ((CustomTileEntity) worldIn.getTileEntity(pos));
            //west,north,east,south
            double over = (double) pos.getY() + 0.3D;
            if (ct.GetActiveInput(3)) {
                double north_middle = (double) pos.getX() + 0.5D;
                double west_abit = (double) pos.getZ() + 0.2D;
                worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, north_middle, over, west_abit, 1.0D, 0.0D, 0.0D);
            }
            if (ct.GetActiveInput(0)) {
                double north_middle_abit = (double) pos.getX() + 0.8D;
                double west_middle = (double) pos.getZ() + 0.5D;
                worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, north_middle_abit, over, west_middle, 1.0D, 0.0D, 0.0D);
            }
            if (ct.GetActiveInput(1)) {
                double north_middle = (double) pos.getX() + 0.5D;
                double west_middle_abit = (double) pos.getZ() + 0.8D;
                worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, north_middle, over, west_middle_abit, 1.0D, 0.0D, 0.0D);
            }
            if (ct.GetActiveInput(2)) {
                double north_abit = (double) pos.getX() + 0.2D;
                double west_middle = (double) pos.getZ() + 0.5D;
                worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, north_abit, over, west_middle, 1.0D, 0.0D, 0.0D);

            }
        }


        /**
         * * private*** Findet Heraus ob weitere Inputs oder Outputs Slot frei sind
         *
         * @param bs     BlockState des Blockes
         * @param input  Anzahl der Inputs
         * @param output Anzahl der Outputs
         * @param world  Welt des Blockes
         * @param pos    Position des Blockes
         */
        public boolean[] IO_State(BlockState bs, int input, int output, World world, BlockPos pos) {
            if (bs.get(INPUT1) != InputSide.NONE)
                input++;
            if (bs.get(INPUT2) != InputSide.NONE)
                input++;
            if (bs.get(INPUT3) != InputSide.NONE)
                input++;
            return new boolean[]{input < getTE(world, pos).GetMaxInput(), bs.get(OUTPUT) == InputSide.NONE && output == 0};
        }

        // ----private----------------

        /**
         * ** private*** Hinzuf�gen eines neuen Inputes
         *
         * @param d     Seite an der der neue Input ist
         * @param pos   Position des Blockes, an dem der Input hinzugef�gt wird
         * @param world Welt des Blockes
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
         * ** private*** Entfernen eines Inputes
         *
         * @param d     Seite an der der zu l�schende Input ist
         * @param pos   Position des Blockes, an dem der Input entfernt wird
         * @param world Welt des Blockes
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
         * ** private*** Refreshen der Inputs
         *
         * @param pos   Position des Blockes
         * @param world Welt des Blockes
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
         * ** private*** Abfrage ob Input existiert
         *
         * @param blockstate Blockstate des Blockes
         * @param d          Direction des Blockes
         */
        private boolean existInputDirections(BlockState blockstate, Direction d) {
            if (blockstate.has(INPUT1) && d == blockstate.get(INPUT1).GetDirection())
                return true;
            if (blockstate.has(INPUT2) && d == blockstate.get(INPUT2).GetDirection())
                return true;
            return blockstate.has(INPUT3) && d == blockstate.get(INPUT3).GetDirection();
        }

        /**
         * ** private*** Lie�t alle Redstone Inputs ein und gibt den neuen Output aus
         *
         * @param world      Welt des Blockes
         * @param pos        Position des Blockes
         * @param blockstate Blockstate des Blockes
         */
        protected int getPowerOnSides(World world, BlockPos pos, BlockState blockstate) {
            List<Integer> inputs = new ArrayList();
            if (blockstate.has(INPUT1) && blockstate.get(INPUT1) != InputSide.NONE) {
                int i = this.getPowerOnSide(world, pos, Objects.requireNonNull(blockstate.get(INPUT1).GetDirection()));
                int k = DirectiontoSlotID(blockstate.get(INPUT1).GetDirection());
                if (k >= 0)
                    getTE(world, pos).SetActiveInput(k, i > 0);
                inputs.add(i);
            }
            if (blockstate.has(INPUT2) && blockstate.get(INPUT2) != InputSide.NONE) {
                int i = this.getPowerOnSide(world, pos, Objects.requireNonNull(blockstate.get(INPUT2).GetDirection()));
                int k = DirectiontoSlotID(blockstate.get(INPUT2).GetDirection());
                if (k >= 0)
                    getTE(world, pos).SetActiveInput(k, i > 0);
                inputs.add(i);
            }
            if (blockstate.has(INPUT3) && blockstate.get(INPUT3) != InputSide.NONE) {
                int i = this.getPowerOnSide(world, pos, Objects.requireNonNull(blockstate.get(INPUT3).GetDirection()));
                int k = DirectiontoSlotID(blockstate.get(INPUT3).GetDirection());
                if (k >= 0)
                    getTE(world, pos).SetActiveInput(k, i > 0);
                inputs.add(i);
            }
            if (inputs.size() <= 0)
                return 0;
            else {
                int i = logic(inputs, world, pos);
                int k = DirectiontoSlotID(blockstate.get(OUTPUT).GetDirection());
                if (k >= 0)
                    getTE(world, pos).SetActiveInput(k, i > 0);
                return i;
            }
        }


        /**
         * ** private*** Gibt die Logik des Blockes an
         *
         * @param inputs Alle Redstone Input Values
         * @return Neuer Output
         */
        private int logic(List<Integer> inputs, World world, BlockPos pos) {
            if (!getTE(world, pos).IsActive())
                return 0;
            StringBuilder erg = new StringBuilder();
            for (int f : inputs)
                erg.append(f > 0 ? 'T' : 'F');
            while (erg.length() < 3) {
                erg.append('N');
            }
            return getTE(world, pos).GetBooleanAt(GetIdWithState(erg.toString())) ? (Collections.max(inputs) == 0 ? 15 : Collections.max(inputs)) : 0;
        }

        private int GetIdWithState(String state) {
            String[] cases3 = new String[]{"TFF", "TFT", "TTF", "TTT", "FFF", "FFT", "FTF", "FTT", "TFN", "TTN", "FFN", "FTN", "TNN", "FNN", "NNN"};
            return IntStream.range(0, cases3.length).filter(f -> cases3[f].equals(state)).findFirst().orElse(14);
        }

        /**
         * ** private*** Parsed eine String Expression in eine Bool Expression
         *
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
            for (char c : help) {
                if (!allowed.contains(c))
                    return false;
                x.add(c);
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
                                    x.subList(g, f).clear();
                                    f = 0;
                                    g = -1;
                                } else if (x.get(g + 2) == '|') {
                                    if (x.get(g + 1) == 'F' && x.get(g + 3) == 'F') {
                                        x.set(f, 'F');
                                    } else {
                                        x.set(f, 'T');
                                    }
                                    x.subList(g, f).clear();
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
                                    if (f > g) {
                                        x.subList(g, f).clear();
                                    }
                                    f = 0;
                                    g = -1;
                                }
                            }
                        }
                    }
                }
            }
            return x.get(0) == 'T';
        }

        /**
         * ** private*** TileEntity des Blockes bekommen
         *
         * @param world Welt des Blockes
         * @param pos   Position des Blockes
         */
        private CustomTileEntity getTE(World world, BlockPos pos) {
            return (CustomTileEntity) world.getTileEntity(pos);
        }
    }

    public static class CustomTileEntity extends LockableLootTileEntity {
        private NonNullList<ItemStack> stacks = NonNullList.withSize(5, ItemStack.EMPTY);
        private boolean[] help = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
        private boolean isActive = true;
        private String test2 = "null";
        private int MaxInput = 3;
        private boolean[] activeInput = new boolean[]{false, false, false, false};
        private boolean SetActive = false;

        protected CustomTileEntity() {
            super(Objects.requireNonNull(tileEntityType));
        }

        @Override
        public void read(CompoundNBT compound) {
            super.read(compound);
            test2 = compound.getString("test2");
            MaxInput = compound.getInt("input");
            for (int f = 0; f < help.length; f++) {
                help[f] = compound.getBoolean("HashMap" + f);
            }
            for (int f = 0; f < activeInput.length; f++) {
                activeInput[f] = compound.getBoolean("activeInput" + f);
            }
            isActive = compound.getBoolean("active");
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
            SetActive = compound.getBoolean("set");
            ItemStackHelper.loadAllItems(compound, this.stacks);
        }

        @Override
        public CompoundNBT write(CompoundNBT compound) {
            super.write(compound);
            compound.putString("test2", test2);
            compound.putInt("input", MaxInput);
            for (int f = 0; f < help.length; f++) {
                compound.putBoolean("HashMap" + f, help[f]);
            }
            for (int f = 0; f < activeInput.length; f++) {
                compound.putBoolean("activeInput" + f, activeInput[f]);
            }
            compound.putBoolean("set", SetActive);
            compound.putBoolean("active", isActive);
            ItemStackHelper.saveAllItems(compound, this.stacks);
            return compound;
        }

        public void SetActiveInput(int slot, boolean active) {
            if (activeInput[slot] != active) {
                SetActive = true;
            }
            activeInput[slot] = active;
        }

        public boolean GetActiveInput(int slot) {
            return activeInput[slot];
        }

        public int GetMaxInput() {
            return MaxInput;
        }

        public void SetMaxInput(int set) {
            MaxInput = set;
        }

        public String GetTest2() {
            if (test2.equals("null"))
                return null;
            return test2;
        }

        public boolean isSetActive() {
            return SetActive;
        }

        public void setSetActive(boolean setActive) {
            SetActive = setActive;
        }


        public void SetTest2(String set) {
            test2 = set;
        }

        public void SetHashMap(List<Boolean> help_) {
            for (int f = 0; f < help_.size(); f++) {
                help[f] = help_.get(f);
            }
        }

        public boolean GetBooleanAt(int id) {
            if (id > 13)
                return false;
            return help[id];
        }

        public boolean IsActive() {
            return isActive;
        }

        public void SetActive(boolean set) {
            isActive = set;
        }

        @Override
        public SUpdateTileEntityPacket getUpdatePacket() {
            return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
        }

        @Override
        public CompoundNBT getUpdateTag() {
            return this.write(new CompoundNBT());
        }

        @Override
        public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
            this.read(pkt.getNbtCompound());
        }

        @Override
        public int getSizeInventory() {
            return 5;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack itemstack : this.stacks)
                if (!itemstack.isEmpty())
                    return false;
            return true;
        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return true;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stacks.get(slot);
        }

        @Override
        public ITextComponent getDefaultName() {
            return new StringTextComponent("logicblock");
        }

        @Override
        public int getInventoryStackLimit() {
            return 1;
        }

        @Override
        public Container createMenu(int id, PlayerInventory player) {
            return new LogicBlockGui.GuiContainerMod(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
        }

        @Override
        public ITextComponent getDisplayName() {
            return new StringTextComponent("Logic Block");
        }

        @Override
        protected NonNullList<ItemStack> getItems() {
            return this.stacks;
        }

        @Override
        protected void setItems(NonNullList<ItemStack> stacks) {
            this.stacks = stacks;
        }
    }
}
