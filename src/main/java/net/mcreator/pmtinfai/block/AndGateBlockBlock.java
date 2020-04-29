
package net.mcreator.pmtinfai.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.pmtinfai.itemgroup.LogicBlocksItemGroup;
import net.mcreator.pmtinfai.gui.LogicBlockGUIGui;
import net.mcreator.pmtinfai.PMTINFAIElements;

import java.util.List;
import java.util.Collections;

import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import net.minecraft.inventory.container.Slot;
import java.util.Map;
import net.minecraft.item.Items;
import net.mcreator.pmtinfai.LogicBlock;
import net.mcreator.pmtinfai.InputSide;

@PMTINFAIElements.ModElement.Tag
public class AndGateBlockBlock extends PMTINFAIElements.ModElement {
	@ObjectHolder("pmtinfai:andgateblock")
	public static final Block block = null;
	@ObjectHolder("pmtinfai:test")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public AndGateBlockBlock(PMTINFAIElements instance) {
		super(instance, 7);
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
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("test"));
	}
	public static class CustomBlock extends LogicBlock {
		public CustomBlock() {
			super();
			setRegistryName("andgateblock");
		}

		protected int logic(List<Integer> inputs) {
			return inputs.contains(0) ? 0 : Collections.max(inputs);
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
						return new StringTextComponent("Erwrwe");
					}

					@Override
					public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
						return new LogicBlockGUIGui.GuiContainerMod(id, inventory,
								new PacketBuffer(Unpooled.buffer()).writeBlockPos(new BlockPos(x, y, z)));
					}
				}, new BlockPos(x, y, z));
			/*	Container _current = ((PlayerEntity) entity).openContainer;
			if (_current instanceof Supplier) {
				Object invobj = ((Supplier) _current).get();
				if (invobj instanceof Map) {
					ItemStack _setstack = new ItemStack(Items.REDSTONE, (int) (1));
					_setstack.setCount(1);
					ItemStack _setstack2 = new ItemStack(Items.REDSTONE_TORCH, (int) (1));
					_setstack2.setCount(1);
					ItemStack _setstack3 = new ItemStack(Items.REDSTONE, (int) (1));
					_setstack3.setCount(1);
					ItemStack _setstack4 = new ItemStack(Items.REDSTONE, (int) (1));
					_setstack4.setCount(1);
					((Slot) ((Map) invobj).get((int) (this.DirectiontoSlotID(state.get(FACING))))).putStack(_setstack2);
					if(state.has(LogicBlock.INPUT1)&&state.get(LogicBlock.INPUT1)!=InputSide.NONE)
						((Slot) ((Map) invobj).get((int) (this.DirectiontoSlotID(((InputSide)state.get(LogicBlock.INPUT1)).GetDirection())))).putStack(_setstack);
					if(state.has(LogicBlock.INPUT2)&&state.get(LogicBlock.INPUT2)!=InputSide.NONE)
						((Slot) ((Map) invobj).get((int) (this.DirectiontoSlotID(((InputSide)state.get(LogicBlock.INPUT2)).GetDirection())))).putStack(_setstack3);
					if(state.has(LogicBlock.INPUT3)&&state.get(LogicBlock.INPUT3)!=InputSide.NONE)
						((Slot) ((Map) invobj).get((int) (this.DirectiontoSlotID(((InputSide)state.get(LogicBlock.INPUT3)).GetDirection())))).putStack(_setstack4);
					_current.detectAndSendChanges();
				}
			}*/
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
			return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
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
		private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(compound, this.stacks);
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			ItemStackHelper.saveAllItems(compound, this.stacks);
			return compound;
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
			return new StringTextComponent("erwrwe");
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public Container createMenu(int id, PlayerInventory player) {
			return new LogicBlockGUIGui.GuiContainerMod(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
		}

		@Override
		public ITextComponent getDisplayName() {
			return new StringTextComponent("Erwrwe");
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
