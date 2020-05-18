package net.mcreator.pmtinfai.block;

import net.mcreator.pmtinfai.enums.InputSide;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class MKLGBlocks extends Block {
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

    public MKLGBlocks(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(POWER, 0));
    }
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
            if (tileentity instanceof LogicBlock.CustomTileEntity) {
                InventoryHelper.dropInventoryItems(world, pos, (LogicBlock.CustomTileEntity) tileentity);
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
     * ** protected*** Direction anhand der SlotId
     *
     * @param slot ID des Slot
     * @return Direction des SlotIDs
     */
    protected Direction SlotIDtoDirection(int slot) {
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
     * ** protected*** Direction anhand der SlotId
     *
     * @param d Direction des Slot
     * @return SlotID des SlotIDs
     */
    protected int DirectiontoSlotID(Direction d) {
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
     * ** private*** Ließt den Redstone Input an einer Seite an
     *
     * @param world Welt des Blockes
     * @param pos   Position des Blockes, der den Redstonewert bekommt
     * @param side  Seite an der der Redstonewert eingegeben wird
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


    public abstract void update(BlockState state, World worldIn, BlockPos pos, int calculatedOutput);
     protected abstract int getPowerOnSides(World world, BlockPos pos, BlockState blockstate);

    }
