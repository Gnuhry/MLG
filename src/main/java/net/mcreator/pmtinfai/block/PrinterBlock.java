
package net.mcreator.pmtinfai.block;

import io.netty.buffer.Unpooled;
import net.mcreator.pmtinfai.MKLGBlock;
import net.mcreator.pmtinfai.MKLGItems;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.enums.LogicKinds;
import net.mcreator.pmtinfai.gui.PrinterGui;
import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@PMTINFAIElements.ModElement.Tag
public class PrinterBlock extends PMTINFAIElements.ModElement {
    @ObjectHolder("pmtinfai:printer")
    public static final Block block = null;
    @ObjectHolder("pmtinfai:printer")
    public static final TileEntityType<CustomTileEntity> tileEntityType = null;

    public PrinterBlock(PMTINFAIElements instance) {
        super(instance, 19);
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
        event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("printer"));
    }

    public static class CustomBlock extends Block {

        protected static final VoxelShape SWL_CORNER = Block.makeCuboidShape(0.2D, 0.0D, 0.2D, 2.2D, 6.0D, 2.2D);
        protected static final VoxelShape NWL_CORNER = Block.makeCuboidShape(0.2D, 0.0D, 15.8D, 2.0D, 6.0D, 13.8D);
        protected static final VoxelShape NEL_CORNER = Block.makeCuboidShape(15.8D, 0.0D, 15.8D, 13.8D, 6.0D, 13.8D);
        protected static final VoxelShape SEL_CORNER = Block.makeCuboidShape(15.8D, 0.0D, 0.2D, 13.8D, 6.0D, 2.2D);
        protected static final VoxelShape PLATE = Block.makeCuboidShape(0.2D, 6.0D, 0.2D, 15.8D, 8.0D, 15.8D);
        protected static final VoxelShape PRESS = Block.makeCuboidShape(4.6D, 8.0D, 4.6D, 11.8D, 15.4D, 12.0D);
        protected static final VoxelShape COMPLETE = VoxelShapes.or(SWL_CORNER, NWL_CORNER, NEL_CORNER, SEL_CORNER, PLATE, PRESS);
        public CustomTileEntity ct = null;

        public CustomBlock() {
            super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1f, 10f).lightValue(0));
            setRegistryName("printer");
            MKLGBlock.Printer=this;
        }


        /**
         * Gibt den VoxelShape(Aussehen) des Blockes zur�ck
         *
         * @param state   Blockstate des Blockes
         * @param worldIn Teil der Welt des Blockes
         * @param pos     Position des Blockes
         * @param context Kontext
         * @return VoxelShape des Blockes
         */

        public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
            return COMPLETE;
        }

        @Override
        public int tickRate(IWorldReader worldIn) {
            return 10;
        }

        @Override
        public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
            super.tick(state, worldIn, pos, random);
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));


            CustomTileEntity tileEntity = (CustomTileEntity) worldIn.getTileEntity(pos);
            ct = tileEntity;
            if (tileEntity.getStackInSlot(0).isEmpty() && tileEntity.getCook() > 0) {
                tileEntity.reset();
            } else if (tileEntity.getCook() == 0) {
                if (!(tileEntity.getStackInSlot(0).isEmpty() || tileEntity.getStackInSlot(1).isEmpty())) {
                    if ((tileEntity.getStackInSlot(0).getItem().equals(MKLGItems.StandardcardItem)) || tileEntity.getStackInSlot(1).getItem().equals(MKLGItems.CustomCardItem)) {
                        if (!tileEntity.getStackInSlot(2).isEmpty()) {
                            if (tileEntity.getStackInSlot(2).getTag().equals(tileEntity.getStackInSlot(0).getTag())) {
                                if (tileEntity.getStackInSlot(2).getCount() < tileEntity.getStackInSlot(2).getMaxStackSize()) {
                                    if (tileEntity.getStackInSlot(1).hasTag())
                                        tileEntity.setBlockType(tileEntity.getStackInSlot(1).getTag());
                                    tileEntity.startCook();
                                }
                            }
                        } else {
                            if (tileEntity.getStackInSlot(1).hasTag())
                                tileEntity.setBlockType(tileEntity.getStackInSlot(1).getTag());
                            tileEntity.startCook();
                        }
                    }
                }
            } else if (tileEntity.getCook() > 1) {
                tileEntity.cooking();
            } else if (tileEntity.getCook() == 1) {
                if (tileEntity.getStackInSlot(2).isEmpty()) {
                    ItemStack i = ItemStack.EMPTY;
                    if (tileEntity.getCustom().equals("")) {
                        i = new ItemStack(MKLGItems.StandardcardItem, 1);
                        i.setTag(tileEntity.getStackInSlot(0).getTag().copy());
                    } else {
                        i = new ItemStack(MKLGItems.CustomCardItem, 1);
                        if (tileEntity.getBlockType() == -1) {
                            i.setTag(tileEntity.getStackInSlot(0).getTag().copy());
                        } else {
                            i.setTag(LogicKinds.Get(tileEntity.getBlockType()).GetNBT());
                        }
                    }
                    tileEntity.setInventorySlotContents(2, i);
                } else {
                    tileEntity.getStackInSlot(2).grow(1);
                }
                tileEntity.cooking();
            }
        }

        @Override
        public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
            List<ItemStack> dropsOriginal = super.getDrops(state, builder);
            if (!dropsOriginal.isEmpty())
                return dropsOriginal;
            return Collections.singletonList(new ItemStack(this, 1));
        }

        public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
        }

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
                        return new StringTextComponent("Printer");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
                        return new PrinterGui.GuiContainerMod(id, inventory,
                                new PacketBuffer(Unpooled.buffer()).writeBlockPos(new BlockPos(x, y, z)));
                    }
                }, new BlockPos(x, y, z));
            }
            return true;
        }

        @Override
        public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
        }

        @Override
        public boolean hasTileEntity(BlockState state) {
            return true;
        }

        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return new CustomTileEntity();
        }

        @Override
        public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
            super.eventReceived(state, world, pos, eventID, eventParam);
            TileEntity tileentity = world.getTileEntity(pos);
            return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
        }

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
    }

    public static class CustomTileEntity extends LockableLootTileEntity {
        private NonNullList<ItemStack> stacks = NonNullList.withSize(3, ItemStack.EMPTY);
        private int cook = 0;
        private final int cook_duration = 20;
        private int type = 0;
        private String custom = "";

        protected CustomTileEntity() {
            super(Objects.requireNonNull(tileEntityType));
        }

        @Override
        public void read(CompoundNBT compound) {
            super.read(compound);
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
            this.cook = compound.getInt("cook");
            this.type = compound.getInt("type");
            this.custom = compound.getString("custom");
            ItemStackHelper.loadAllItems(compound, this.stacks);
        }

        @Override
        public CompoundNBT write(CompoundNBT compound) {
            super.write(compound);
            ItemStackHelper.saveAllItems(compound, this.stacks);
            compound.putInt("cook", cook);
            compound.putInt("type", type);
            compound.putString("custom", custom);
            return compound;
        }

        public int getCook() {
            return cook;
        }

        public void startCook() {
            cook = cook_duration;
            stacks.get(1).shrink(1);
        }

        public void cooking() {
            cook -= 1;
        }

        public int getProgressionScaled() {
            return cook != 0 ? (24 - cook * 24 / cook_duration) : 0;
        }

        public void reset() {
            cook = 0;
            if (stacks.get(1).isEmpty()) {
                ItemStack i = null;
                CompoundNBT nbt = new CompoundNBT();
                if (type == -1) {
                    nbt.putString("logic", custom);
                    System.out.println(custom);
                    nbt.putBoolean("logic_", true);
                    i = new ItemStack(MKLGItems.CustomCardItem, 1);
                } else {
                    nbt = LogicKinds.Get(type).GetNBT();
                    i = new ItemStack(MKLGItems.StandardcardItem, 1);
                }
                i.setTag(nbt);
                stacks.set(1, i);
            } else {
                stacks.get(1).grow(1);
            }
        }

        public void setBlockType(CompoundNBT nbt) {
            for (LogicKinds lk : LogicKinds.AndGate.getDeclaringClass().getEnumConstants()) {
                if (lk.GetNBT().equals(nbt)) {
                    type = lk.Get();
                    custom = "";
                    return;
                }
            }
            type = -1;
            custom = nbt.getString("logic");
        }

        public int getBlockType() {
            return type;
        }

        public String getCustom() {
            return custom;
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
            return 3;
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
            return new StringTextComponent("printer");
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public Container createMenu(int id, PlayerInventory player) {
            return new PrinterGui.GuiContainerMod(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
        }

        @Override
        public ITextComponent getDisplayName() {
            return new StringTextComponent("Printer");
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
