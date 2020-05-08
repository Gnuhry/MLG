
package net.mcreator.pmtinfai.block;

import io.netty.buffer.Unpooled;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.gui.PrinterGUIGui;
import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
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
        public CustomBlock() {
            super(Block.Properties.create(Material.ROCK).sound(SoundType.GROUND).hardnessAndResistance(1f, 10f).lightValue(0));
            setRegistryName("printer");
        }

        @Override
        public int tickRate(IWorldReader worldIn) {
            return 10;
        }

        @Override
        public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
            super.tick(state, worldIn, pos, random);
            ((PrinterBlock.CustomTileEntity) Objects.requireNonNull(worldIn.getTileEntity(pos))).tick();
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
        }

        @Override
        public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
            List<ItemStack> dropsOriginal = super.getDrops(state, builder);
            if (!dropsOriginal.isEmpty())
                return dropsOriginal;
            return Collections.singletonList(new ItemStack(this, 1));
        }

        public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {

            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);

        }

        @Override
        public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult hit) {
            boolean retval = super.onBlockActivated(state, world, pos, entity, hand, hit);
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
                        return new PrinterGUIGui.GuiContainerMod(id, inventory,
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

        protected CustomTileEntity() {
            super(Objects.requireNonNull(tileEntityType));
        }

        private int cook = 0;

        @Override
        public void read(CompoundNBT compound) {
            super.read(compound);
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(compound, this.stacks);
            cook = compound.getInt("cook");
        }

        @Override
        public CompoundNBT write(CompoundNBT compound) {
            super.write(compound);
            ItemStackHelper.saveAllItems(compound, this.stacks);
            compound.putInt("cook", cook);
            return compound;
        }

        @Override
        public SUpdateTileEntityPacket getUpdatePacket() {
            return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
        }

        public void tick() {
            if (cook > 1) {
                cook--;
            } else if (cook == 1) {
                if (stacks.get(2).isEmpty()) {
                    ItemStack erg = getStackInSlot(1).copy();
                    CompoundNBT nbt = new CompoundNBT();
                    nbt.putString("logic", stacks.get(0).getTag().getString("logic"));
                    if (stacks.get(0).getTag().contains("logic_"))
                        nbt.putBoolean("logic_", stacks.get(0).getTag().getBoolean("logic_"));
                    erg.setTag(nbt);
                    erg.setCount(1);
                    stacks.set(2, erg);
                } else {
                    getStackInSlot(2).grow(1);
                }
                cook--;
            } else if (cook == 0 && getStackInSlot(1).getCount() > 0 && Objects.requireNonNull(getStackInSlot(0).getTag()).contains("logic")) {
                if (stacks.get(2).hasTag() && !stacks.get(2).getTag().getString("logic").equals(Objects.requireNonNull(getStackInSlot(0).getTag()).getString("logic")))
                    return;
                cook = 200;
                getStackInSlot(1).shrink(1);
            }
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
            return new PrinterGUIGui.GuiContainerMod(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
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
