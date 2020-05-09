
package net.mcreator.pmtinfai.block;

import io.netty.buffer.Unpooled;
import net.mcreator.pmtinfai.MKLGItems;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.enums.FFSpecies;
import net.mcreator.pmtinfai.enums.InputSide;
import net.mcreator.pmtinfai.gui.FlipFlopGui;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import java.util.*;

@PMTINFAIElements.ModElement.Tag
public class FlipFlopBlock extends PMTINFAIElements.ModElement {
    @ObjectHolder("pmtinfai:flipflopblock")
    public static final Block block = null;
    @ObjectHolder("pmtinfai:flipflopblock")
    public static final TileEntityType<CustomTileEntity> tileEntityType = null;

    public FlipFlopBlock(PMTINFAIElements instance) {
        super(instance, 20);
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
        event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("flipflopblock"));
    }

    public static class CustomBlock extends Block {
        // Properties des Blocks
        public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
        public static final EnumProperty<InputSide> INPUT1 = EnumProperty.create("set_side", InputSide.class);
        public static final EnumProperty<InputSide> INPUT2 = EnumProperty.create("reset_side", InputSide.class);
        public static final EnumProperty<InputSide> INPUT3 = EnumProperty.create("clock_side", InputSide.class);
        public static final EnumProperty<InputSide> OUTPUT = EnumProperty.create("output", InputSide.class);
        public static final EnumProperty<FFSpecies> LOGIC = EnumProperty.create("logic", FFSpecies.class);

        // boolean Variablen zum Abfangen von Multithreading
        public CustomBlock() {
            super(Block.Properties.create(Material.MISCELLANEOUS).sound(SoundType.STEM).hardnessAndResistance(0f, 0f).lightValue(0));
            setRegistryName("flipflopblock");
            // Laden der Default Properties der Blöcke
            this.setDefaultState(this.stateContainer.getBaseState().with(POWER, 0).with(INPUT1, InputSide.NONE)
                    .with(INPUT2, InputSide.NONE).with(INPUT3, InputSide.NONE).with(OUTPUT, InputSide.NONE).with(LOGIC, FFSpecies.NONE));
        }

        // --------------------------------------------Getter------------

        /**
         * Gibt den Block als Item zurück
         *
         * @param state   BlockState des Blockes
         * @param builder Builder des LootContexes
         * @return Block als Item oder Items
         */
        @Override
        public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
            List<ItemStack> dropsOriginal = super.getDrops(state, builder);
            return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
        }

        @Override
        public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
        }

        /**
         * Gibt die Tickrate des Blockes zurück
         *
         * @param worldIn Teil der Welt des Blockes
         * @return Die aktuelle Tickrate des Blockes
         */
        public int tickRate(IWorldReader worldIn) {
            return 1;
        }

        /**
         * Abfgage wie stark die WeakPower(direkte Redstoneansteuerung) ist
         *
         * @param blockState  BlockState des Blockes
         * @param blockAccess Angabe welche Art der Block ist
         * @param pos         Position des Blockes
         * @param side        Seite an der die Power abgefragt wird
         * @return Gibt die RedstonePower(0-15) zurück
         */
        @Override
        public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
            return blockState.get(OUTPUT).GetDirection() == side ? blockState.get(POWER) : 0;
        }

        /**
         * Abfgage wie stark die StrongPower(indirekte Redstoneansteuerung) ist
         *
         * @param blockState  BlockState des Blockes
         * @param blockAccess Angabe welche Art der Block ist
         * @param pos         Position des Blockes
         * @param side        Seite an der die Power abgefragt wird
         * @return Gibt die RedstonePower(0-15) zurück
         */
        @Override
        public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
            return this.getWeakPower(blockState, blockAccess, pos, side);
        }

        /**
         * Gibt den VoxelShape(Aussehen) des Blockes zurück
         *
         * @param state   Blockstate des Blockes
         * @param worldIn Teil der Welt des Blockes
         * @param pos     Position des Blockes
         * @param context Kontext
         * @return VoxelShape des Blockes
         */
        public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
            return Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
        }

        /**
         * Erstellt die neue Warheitstabelle
         *
         * @param exp   Expresion der neuen Logik
         * @param world Welt des Blockes
         * @param pos   Position des Blockes
         */
        public void GetAllStates(String exp, World world, BlockPos pos) {
            BlockState bs = world.getBlockState(pos);
            FFSpecies species = FFSpecies.GetEnum(exp);
            if (species == bs.get(LOGIC))
                return;
            world.setBlockState(pos, bs.with(LOGIC, species));
            getTE(world, pos).SetHIGH(0);
            getTE(world, pos).SetLOW(0);
            getTE(world, pos).SetMS(0);
            update(world.getBlockState(pos), world, pos, getPowerOnSides(world, pos, world.getBlockState(pos)));
        }

        // -------------------------------------Eventlistener----------------------

        /**
         * EventListener wenn ein Rechtsklick auf den Block durchgeführt wird
         *
         * @param state  BlockState des Blockes
         * @param world  Welt des Blockes
         * @param pos    Position des Blockes
         * @param entity Player der den Rechtsklick ausführt
         * @param hand   Hand die das ausführt
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
                        return new StringTextComponent("FlipFlop Block");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
                        return new FlipFlopGui.GuiContainerMod(id, inventory,
                                new PacketBuffer(Unpooled.buffer()).writeBlockPos(new BlockPos(x, y, z)));
                    }
                }, new BlockPos(x, y, z));
            }
            return true;
        }

        /**
         * EventListener wenn der Block ersetzt wird
         *
         * @param state    BlockState des Blockes
         * @param world    Welt des Blockes
         * @param pos      Position des Blockes
         * @param newState neuer BlockState des Blockes
         * @param isMoving Gibt an ob der Block sich bewegt
         */
        @Override
        public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
            if (state.getBlock() != newState.getBlock()) {
                TileEntity tileentity = world.getTileEntity(pos);
                if (tileentity instanceof CustomTileEntity) {
                    InventoryHelper.dropInventoryItems(world, pos, (CustomTileEntity) tileentity);
                    world.updateComparatorOutputLevel(pos, this);
                }
                super.onReplaced(state, world, pos, newState, isMoving);
            }
        }

        /**
         * EventListener wenn Nachbar des Blockes sich ändert - Aktualisiert weitere
         * Blöcke
         *
         * @param state    Blockstate des Blockes
         * @param worldIn  Welt in der der Block steht
         * @param pos      Position des Blockes
         * @param blockIn  Nachbarblock der sich ändert
         * @param fromPos  Position von dem sich änderndem Block
         * @param isMoving Gibt an ob der Block sich bewegt
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
                update(state, worldIn, pos, this.getPowerOnSides(worldIn, pos, state));
            }
        }

        /**
         * EventListener wenn Block durch Entity gesetzt wurde
         *
         * @param state   Blockstate des Blockes
         * @param worldIn Welt in der der Block steht
         * @param pos     Position des Blockes
         * @param placer  Entity die den Block gesetzt hat
         * @param stack   Stack des Items
         */
        public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
            if (this.getPowerOnSides(worldIn, pos, state) > 0) {
                worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
            }
        }

        // ----------------------------------Abfrage----------------------------

        /**
         * Abfrage ob Block TileEntitys hat
         *
         * @param state BlockState des Blockes
         * @return Gibt an ob Block TileEntitys hat
         */
        @Override
        public boolean hasTileEntity(BlockState state) {
            return true;
        }

        /**
         * Abfrage ob Block ein Event bekommen hat
         *
         * @param state      BlockState des Blockes
         * @param world      Welt des Blockes
         * @param pos        Position des Blockes
         * @param eventID    ID des Events
         * @param eventParam Parametes des Event
         * @return Gibt an ob Block Event bekommen hat
         */
        @Override
        public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
            super.eventReceived(state, world, pos, eventID, eventParam);
            TileEntity tileentity = world.getTileEntity(pos);
            return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
        }

        /**
         * Abfrage ob Redstone sich an der Seite verbinden kann
         *
         * @param state Blockstate des Blockes
         * @param world Angabe welche Art der Block ist
         * @param pos   Position des Blockes
         * @param side  Seite die Abgefragt wird
         * @return Gibt zurück ob der Redstone sich verbinden kann
         */
        @Override
        public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
            return side != null && (state.get(OUTPUT).GetDirection() == side || existInputDirections(state, side));
        }

        /**
         * Abfrage ob der Block RedstonePower ausgeben kann
         *
         * @param state Blockstate des Blockes
         * @return Gibt an ob der Block RedstonePower ausgeben kann
         */
        @Override
        public boolean canProvidePower(BlockState state) {
            return true;
        }

        /**
         * Abfrage ob es eine valide Position für den Block ist
         *
         * @param state   Blockstate des Blockes
         * @param worldIn Teil der Welt des Blockes
         * @param pos     Position des Blockes
         * @return Gibt an ob der Platz des Blockes valide ist
         */
        public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
            return func_220064_c(worldIn, pos.down());
        }

        /**
         * Abfrage ob Block fest ist
         *
         * @param state BlockState des Blockes
         * @return Gibt an ob Block solid ist
         */
        public boolean isSolid(BlockState state) {
            return true;
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
         * Wechselt den Blockstate, fals
         *
         * @param state   Blockstate des Blockes
         * @param worldIn Welt des Blockes
         * @param pos     Position des Blockes, der den Tick ausführen soll
         * @param random  Ein Java Random Element für Zufällige Ticks
         */
        public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
            update(state, worldIn, pos, this.getPowerOnSides(worldIn, pos, state));
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
                worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf("0")), 3);
            } else if (calculatedOutput > 0 && state.get(POWER) != calculatedOutput) {
                worldIn.setBlockState(pos, state.with(POWER, calculatedOutput), 3);
            }
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
        }

        /**
         * Input und Output Ändern
         *
         * @param slot  SlotID des ändernden Slot
         * @param world Welt des Blockes
         * @param pos   Position des Blockes
         * @param item  Item im GUI
         */
        public boolean[] changeInput(int slot, BlockPos pos, World world, Item item) {
            BlockState blockstate = world.getBlockState(pos);
            Direction d = SlotIDtoDirection(slot).getOpposite();
            if (item == MKLGItems.SetItem) {
                world.setBlockState(pos, blockstate.with(INPUT1, InputSide.GetEnum(d)));
                clearSlot(pos, world, d, 0);
            } else if (item == MKLGItems.ResetItem) {
                world.setBlockState(pos, blockstate.with(INPUT2, InputSide.GetEnum(d)));
                clearSlot(pos, world, d, 1);
            } else if (item == MKLGItems.ClockItem) {
                world.setBlockState(pos, blockstate.with(INPUT3, InputSide.GetEnum(d)));
                clearSlot(pos, world, d, 2);
            } else if (item == MKLGItems.OutputItem) {
                world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.GetEnum(d)));
                clearSlot(pos, world, d, 3);
            } else {
                clearSlot(pos, world, d, -1);
            }
            return new boolean[]{!(blockstate.get(INPUT1).isActive() || item == MKLGItems.SetItem),
                    !(blockstate.get(INPUT2).isActive() || item == MKLGItems.ResetItem),
                    !(blockstate.get(INPUT3).isActive() || item == MKLGItems.ClockItem),
                    !(blockstate.get(OUTPUT).isActive() || item == MKLGItems.OutputItem)};
        }

        // ----private----------------

        /**
         * ** private*** Löscht alle Directions d aus den Propertys außer einer
         *
         * @param pos    Position des Blockes
         * @param world  Welt des Blockes
         * @param d      Direction die gelöscht wird aus allem
         * @param except Die Exception die nicht ersetzt wird 0-2 INPUT1-3, 3-Output
         */
        private void clearSlot(BlockPos pos, World world, Direction d, int except) {
            BlockState blockstate = world.getBlockState(pos);
            if (except != 0) {
                if (blockstate.get(INPUT1).GetDirection() == d) {
                    blockstate = blockstate.with(INPUT1, InputSide.NONE);
                }
            }
            if (except != 1) {
                if (blockstate.get(INPUT2).GetDirection() == d) {
                    blockstate = blockstate.with(INPUT2, InputSide.NONE);
                }
            }
            if (except != 2) {
                if (blockstate.get(INPUT3).GetDirection() == d) {
                    blockstate = blockstate.with(INPUT3, InputSide.NONE);
                }
            }
            if (except != 3) {
                if (blockstate.get(OUTPUT).GetDirection() == d) {
                    blockstate = blockstate.with(OUTPUT, InputSide.NONE);
                }
            }
            world.setBlockState(pos, blockstate);
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
         * ** private*** Ließt alle Redstone Inputs ein und gibt den neuen Output aus
         *
         * @param world      Welt des Blockes
         * @param pos        Position des Blockes
         * @param blockstate Blockstate des Blockes
         */
        private int getPowerOnSides(World world, BlockPos pos, BlockState blockstate) {
            List<Integer> inputs = new ArrayList();
            if (blockstate.has(INPUT1) && blockstate.get(INPUT1) != InputSide.NONE)
                inputs.add(this.getPowerOnSide(world, pos, Objects.requireNonNull(blockstate.get(INPUT1).GetDirection())));
            if (blockstate.has(INPUT2) && blockstate.get(INPUT2) != InputSide.NONE)
                inputs.add(this.getPowerOnSide(world, pos, Objects.requireNonNull(blockstate.get(INPUT2).GetDirection())));
            if (blockstate.has(INPUT3) && blockstate.get(INPUT3) != InputSide.NONE)
                inputs.add(this.getPowerOnSide(world, pos, Objects.requireNonNull(blockstate.get(INPUT3).GetDirection())));
            if (inputs.size() <= 0)
                return 0;
            else
                return logic(inputs, world, pos);
        }

        /**
         * ** private*** Ließt den Redstone Input an einer Seite an
         *
         * @param world Welt des Blockes
         * @param pos   Position des Blockes, der den Redstonewert bekommt
         * @param side  Seite an der der Redstonewert eingegeben wird
         */
        private int getPowerOnSide(World world, BlockPos pos, Direction side) {
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
         * ** private*** Direction anhand der SlotId
         *
         * @param slot ID des Slot
         * @return Direction des SlotIDs
         */
        private Direction SlotIDtoDirection(int slot) {
            switch (slot) {
                case 0:
                    return Direction.WEST;
                case 1:
                    return Direction.NORTH;
                case 2:
                    return Direction.EAST;
                case 3:
                    return Direction.SOUTH;
                default:
                    return null;
            }
        }

        /**
         * ** private*** Gibt die Logik des Blockes an
         *
         * @param inputs Alle Redstone Input Values
         * @return Neuer Output
         */
        private int logic(List<Integer> inputs, World world, BlockPos pos) {
            // set,reset,clock
            BlockState bs = world.getBlockState(pos);
            if (bs.get(LOGIC) == FFSpecies.NONE || !bs.get(INPUT1).isActive() || !bs.get(INPUT2).isActive()
                    || !bs.get(OUTPUT).isActive())
                return 0;
            char[] table = bs.get(LOGIC).GetTable();
            int index1 = inputs.get(0) > 0 ? 1 : 0, index2 = inputs.get(1) > 0 ? 1 : 0;
            char help = table[(2 * index1) + index2];
            switch (bs.get(LOGIC).GetClockMode()) {
                case 0:
                    if (bs.get(INPUT3).isActive())
                        inputs.remove(2);
                    if (help == 'T')
                        return Collections.max(inputs);
                    if (help == 'F')
                        return 0;
                    if (help == 'D') {
                        if (bs.get(POWER) == 0)
                            return Collections.max(inputs);
                        else {
                            return 0;
                        }
                    }
                    if (help == 'Q')
                        return bs.get(POWER);
                    // No Clock
                case 1:
                    if (!bs.get(INPUT3).isActive())
                        return 0;
                    if (inputs.remove(2) <= 0)
                        return bs.get(POWER);
                    if (help == 'T')
                        return Collections.max(inputs);
                    if (help == 'F')
                        return 0;
                    if (help == 'D') {
                        if (bs.get(POWER) == 0)
                            return Collections.max(inputs);
                        else {
                            return 0;
                        }
                    }
                    if (help == 'Q')
                        return bs.get(POWER);
                    // pegel Clock
                case 2:
                    if (!bs.get(INPUT3).isActive())
                        return 0;
                    if (inputs.remove(2) <= 0) {
                        getTE(world, pos).SetHIGH(0);
                        return bs.get(POWER);
                    }
                    if (getTE(world, pos).GetHIGH() == 2)
                        return bs.get(POWER);
                    getTE(world, pos).SetHIGH(getTE(world, pos).GetHIGH() + 1);
                    if (help == 'T')
                        return Collections.max(inputs);
                    if (help == 'F')
                        return 0;
                    if (help == 'D') {
                        if (bs.get(POWER) == 0)
                            return Collections.max(inputs);
                        else {
                            return 0;
                        }
                    }
                    if (help == 'Q')
                        return bs.get(POWER);
                    // HF Clock
                case 3:
                    if (!bs.get(INPUT3).isActive())
                        return 0;
                    if (inputs.remove(2) > 0) {
                        getTE(world, pos).SetLOW(0);
                        return bs.get(POWER);
                    }
                    if (getTE(world, pos).GetLOW() == 2)
                        return bs.get(POWER);
                    getTE(world, pos).SetLOW(getTE(world, pos).GetLOW() + 1);
                    if (help == 'T')
                        return Collections.max(inputs);
                    if (help == 'F')
                        return 0;
                    if (help == 'D') {
                        if (bs.get(POWER) == 0)
                            return Collections.max(inputs);
                        else {
                            return 0;
                        }
                    }
                    if (help == 'Q')
                        return bs.get(POWER);
                    // LF Clock
                case 4:
                    if (!bs.get(INPUT3).isActive())
                        return 0;
                    if (inputs.remove(2) > 0) {
                        getTE(world, pos).SetLOW(0);
                        if (getTE(world, pos).GetHIGH() == 2) {
                            return bs.get(POWER);
                        } else {
                            getTE(world, pos).SetHIGH(getTE(world, pos).GetHIGH() + 1);
                            if (help == 'T') {
                                getTE(world, pos).SetMS(Collections.max(inputs));
                                return bs.get(POWER);
                            }
                            if (help == 'F') {
                                getTE(world, pos).SetMS(0);
                                return bs.get(POWER);
                            }
                            if (help == 'D') {
                                if (bs.get(POWER) == 0) {
                                    getTE(world, pos).SetMS(Collections.max(inputs));
                                } else {
                                    getTE(world, pos).SetMS(0);
                                }
                                return bs.get(POWER);
                            }
                        }
                        if (help == 'Q') {
                            getTE(world, pos).SetMS(bs.get(POWER));
                            return bs.get(POWER);
                        }
                    } else {
                        getTE(world, pos).SetHIGH(0);
                        if (getTE(world, pos).GetLOW() == 2) {
                            return bs.get(POWER);
                        } else {
                            // Ausgabe
                            getTE(world, pos).SetLOW(getTE(world, pos).GetLOW() + 1);
                            return getTE(world, pos).GetMS();
                        }
                    } // MS Clock
                default:
                    return 0;
            }
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
        private int HIGH = 0;
        private int LOW = 0;
        private int MS = 0;

        protected CustomTileEntity() {
            super(Objects.requireNonNull(tileEntityType));
        }

        @Override
        public void read(CompoundNBT compound) {
            super.read(compound);
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
            HIGH = compound.getInt("HIGH");
            LOW = compound.getInt("LOW");
            MS = compound.getInt("MS");
            ItemStackHelper.loadAllItems(compound, this.stacks);
        }

        @Override
        public CompoundNBT write(CompoundNBT compound) {
            super.write(compound);
            ItemStackHelper.saveAllItems(compound, this.stacks);
            compound.putInt("HIGH", HIGH);
            compound.putInt("LOW", LOW);
            compound.putInt("MS", MS);
            return compound;
        }

        public int GetHIGH() {
            return HIGH;
        }

        public void SetHIGH(int set) {
            HIGH = set;
        }

        public int GetLOW() {
            return LOW;
        }

        public void SetLOW(int set) {
            LOW = set;
        }

        public int GetMS() {
            return MS;
        }

        public void SetMS(int set) {
            MS = set;
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
            return new StringTextComponent("flipflopblock");
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
            return new StringTextComponent("FlipFlop Block");
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
