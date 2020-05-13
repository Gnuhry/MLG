
package net.mcreator.pmtinfai.block;

import io.netty.buffer.Unpooled;
import net.mcreator.pmtinfai.MKLGItems;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.enums.InputSide;
import net.mcreator.pmtinfai.gui.LogicBlockGui;
import net.mcreator.pmtinfai.gui.TimerGui;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@PMTINFAIElements.ModElement.Tag
public class TimerBlock extends PMTINFAIElements.ModElement {
    @ObjectHolder("pmtinfai:timerb")
    public static final Block block = null;
    @ObjectHolder("pmtinfai:timerb")
    public static final TileEntityType<CustomTileEntity> tileEntityType = null;

    public TimerBlock(PMTINFAIElements instance) {
        super(instance, 39);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @Override
    public void initElements() {
        elements.blocks.add(() -> new CustomBlock());
        elements.items
                .add(() -> new BlockItem(block, new Item.Properties().group(LogicBlocksItemGroup.tab)).setRegistryName(block.getRegistryName()));
    }

    @SubscribeEvent
    public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("timerb"));
    }

    public static class CustomBlock extends Block {
        // Properties des Blocks
        public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
        public static final EnumProperty<InputSide> INPUT1 = EnumProperty.create("input1_side", InputSide.class);
        public static final EnumProperty<InputSide> OUTPUT = EnumProperty.create("output", InputSide.class);

        public CustomBlock() {
            super(Block.Properties.create(Material.ROCK).sound(SoundType.GROUND).hardnessAndResistance(1f, 10f).lightValue(0));
            setRegistryName("timerb");
            this.setDefaultState(this.stateContainer.getBaseState().with(POWER, 0).with(INPUT1, InputSide.NONE).with(OUTPUT, InputSide.NONE));
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
        @Override
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
                        return new StringTextComponent("Timer Block");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
                        return new TimerGui.GuiContainerMod(id, inventory,
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
                update(state, worldIn, pos);
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
            return side != null && (state.get(OUTPUT).GetDirection() == side || state.get(INPUT1).GetDirection() == side);
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
            builder.add(POWER).add(INPUT1).add(OUTPUT);
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
            update(state, worldIn, pos);
        }

        /**
         * Wechselt den blockstate, fals
         *
         * @param state   Blockstate des Blockes
         * @param worldIn Welt des Blockes
         * @param pos     Position des Blockes, der geupdated werden soll
         */
        public void update(BlockState state, World worldIn, BlockPos pos) {
            int calculatedOutput = this.getPowerOnSides(worldIn, pos, state);
            worldIn.setBlockState(pos, state.with(POWER, 0), 3);
            if (getTE(worldIn, pos).isTimer()) {
                if (state.get(POWER) > 0 && calculatedOutput == 0) {
                    worldIn.setBlockState(pos, state.with(POWER, 0), 3);
                } else if (calculatedOutput > 0 && state.get(POWER) != calculatedOutput) {
                    worldIn.setBlockState(pos, state.with(POWER, calculatedOutput), 3);
                }
                int k = DirectiontoSlotID(state.get(OUTPUT).GetDirection());
                if (k >= 0)
                    getTE(worldIn, pos).SetActiveInput(k, calculatedOutput > 0);
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
         * Input und Output Ändern
         *
         * @param slot  SlotID des ändernden Slot
         * @param world Welt des Blockes
         * @param pos   Position des Blockes
         * @param item  Item im GUI
         */
        public boolean[] changeInput(int slot, BlockPos pos, World world, Item item) {
            int[] help = new int[]{0, 0};
            BlockState blockstate = world.getBlockState(pos);
            Direction d = SlotIDtoDirection(slot).getOpposite();
            if (item == MKLGItems.InputItem) {
                if (d == blockstate.get(INPUT1).GetDirection()) {
                    return IO_State(blockstate, 0, 0, world, pos);
                }
                if (d == blockstate.get(OUTPUT).GetDirection()) {
                    world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.NONE));
                }
                world.setBlockState(pos, blockstate.with(INPUT1, InputSide.GetEnum(d)));
                help[0] = 1;
            } else if (item == MKLGItems.OutputItem) {
                if (d == blockstate.get(OUTPUT).GetDirection()) {
                    return IO_State(blockstate, 0, 0, world, pos);
                }
                if (d == blockstate.get(INPUT1).GetDirection()) {
                    world.setBlockState(pos, blockstate.with(INPUT1, InputSide.NONE));
                }
                world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.GetEnum(d)));
                help[1] = 1;
            } else {
                if (d == blockstate.get(OUTPUT).GetDirection()) {
                    world.setBlockState(pos, blockstate.with(OUTPUT, InputSide.NONE));
                }
                if (d == blockstate.get(INPUT1).GetDirection()) {
                    world.setBlockState(pos, blockstate.with(INPUT1, InputSide.NONE));
                }
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
            return new boolean[]{bs.get(INPUT1) == InputSide.NONE && input == 0, bs.get(OUTPUT) == InputSide.NONE && output == 0};
        }

        // ----private----------------

        /**
         * ** private*** Ließt alle Redstone Inputs ein und gibt den neuen Output aus
         *
         * @param world      Welt des Blockes
         * @param pos        Position des Blockes
         * @param blockstate Blockstate des Blockes
         */
        private int getPowerOnSides(World world, BlockPos pos, BlockState blockstate) {
            int erg = 0;
            if (blockstate.has(INPUT1) && blockstate.get(INPUT1) != InputSide.NONE) {
                erg = this.getPowerOnSide(world, pos, Objects.requireNonNull(blockstate.get(INPUT1).GetDirection()));
                int k = DirectiontoSlotID(blockstate.get(INPUT1).GetDirection());
                if (k >= 0)
                    getTE(world, pos).SetActiveInput(k, erg > 0);
            }
            return erg;
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
         * ** private*** Direction anhand der SlotId
         *
         * @param d Direction des Slot
         * @return SlotID des SlotIDs
         */
        private int DirectiontoSlotID(Direction d) {
            if (d == Direction.WEST) {
                return 0;
            }
            if (d == Direction.NORTH) {
                return 1;
            }
            if (d == Direction.EAST) {
                return 2;
            }
            if (d == Direction.SOUTH) {
                return 3;
            }
            return -1;
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
        private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
        private int timer = -1;
        private int time = 0;
        private boolean[] activeInput = new boolean[]{false, false, false, false};
        private boolean SetActive = false;

        protected CustomTileEntity() {
            super(tileEntityType);
        }

        @Override
        public void read(CompoundNBT compound) {
            super.read(compound);
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
            timer = compound.getInt("timer");
            for (int f = 0; f < activeInput.length; f++) {
                activeInput[f] = compound.getBoolean("activeInput" + f);
            }
            SetActive = compound.getBoolean("set");
            ItemStackHelper.loadAllItems(compound, this.stacks);
        }

        @Override
        public CompoundNBT write(CompoundNBT compound) {
            super.write(compound);
            compound.putInt("timer", timer);
            ItemStackHelper.saveAllItems(compound, this.stacks);
            for (int f = 0; f < activeInput.length; f++) {
                compound.putBoolean("activeInput" + f, activeInput[f]);
            }
            compound.putBoolean("set", SetActive);
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

        public boolean isSetActive() {
            return SetActive;
        }

        public void setSetActive(boolean setActive) {
            SetActive = setActive;
        }

        public int getTimer() {
            return timer;
        }

        public void setTimer(int timer) {
            time=0;
            this.timer = timer;
        }

        public boolean isTimer() {
            if (timer < 0) return false;
            if (++time == timer) {
                time = 0;
                return true;
            }
            return false;
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
            return 4;
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
            return new StringTextComponent("timerb");
        }

        @Override
        public int getInventoryStackLimit() {
            return 1;
        }

        @Override
        public Container createMenu(int id, PlayerInventory player) {
            return new TimerGui.GuiContainerMod(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
        }

        @Override
        public ITextComponent getDisplayName() {
            return new StringTextComponent("Timer B");
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
