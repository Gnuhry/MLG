
package net.mcreator.pmtinfai.gui;

import net.mcreator.pmtinfai.MKLGItems;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.block.PrinterBlock;
import net.mcreator.pmtinfai.slots.PrinterSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.network.NetworkEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@PMTINFAIElements.ModElement.Tag
public class PrinterGui extends PMTINFAIElements.ModElement {
    private static ContainerType<GuiContainerMod> containerType = null;

    public PrinterGui(PMTINFAIElements instance) {
        super(instance, 20);
        elements.addNetworkMessage(ButtonPressedMessage.class, ButtonPressedMessage::buffer, ButtonPressedMessage::new,
                ButtonPressedMessage::handler);
        elements.addNetworkMessage(GUISlotChangedMessage.class, GUISlotChangedMessage::buffer, GUISlotChangedMessage::new,
                GUISlotChangedMessage::handler);
        containerType = new ContainerType<>(new GuiContainerModFactory());
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @OnlyIn(Dist.CLIENT)
    public void initElements() {
        ScreenManager.registerFactory(containerType, GuiWindow::new);
    }

    @SubscribeEvent
    public void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(containerType.setRegistryName("printergui"));
    }

    public static class GuiContainerModFactory implements IContainerFactory {
        public GuiContainerMod create(int id, PlayerInventory inv, PacketBuffer extraData) {
            return new GuiContainerMod(id, inv, extraData);
        }
    }


    public static class GuiContainerMod extends Container implements Supplier<Map<Integer, Slot>> {
        private World world;
        private PlayerEntity entity;
        private int x, y, z;
        private IInventory internal;
        private Map<Integer, Slot> customSlots = new HashMap<>();

        public GuiContainerMod(int id, PlayerInventory inv, PacketBuffer extraData) {
            super(containerType, id);
            this.entity = inv.player;
            this.world = inv.player.world;
            this.internal = new Inventory(3);
            if (extraData != null) {
                BlockPos pos = extraData.readBlockPos();
                this.x = pos.getX();
                this.y = pos.getY();
                this.z = pos.getZ();
                TileEntity ent = inv.player.world.getTileEntity(pos);
                if (ent instanceof IInventory)
                    this.internal = (IInventory) ent;
            }
            internal.openInventory(inv.player);
            this.customSlots.put(0, this.addSlot(new PrinterSlot(internal, 0, 8, 29) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    ((PrinterBlock.CustomTileEntity)world.getTileEntity(new BlockPos(x,y,z))).setInventorySlotContents(0,customSlots.get(0).getStack());
                }

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }
            }));
            this.customSlots.put(1, this.addSlot(new PrinterSlot(internal, 1, 43, 29) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    ((PrinterBlock.CustomTileEntity)world.getTileEntity(new BlockPos(x,y,z))).setInventorySlotContents(0,customSlots.get(0).getStack());
                }
            }));
            this.customSlots.put(2, this.addSlot(new PrinterSlot(internal, 2, 133, 29) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    ((PrinterBlock.CustomTileEntity)world.getTileEntity(new BlockPos(x,y,z))).setInventorySlotContents(0,customSlots.get(0).getStack());
                    internal.markDirty();
                }
            }));
            ((PrinterSlot) this.customSlots.get(0)).SetItem(MKLGItems.StandardcardItem);
            ((PrinterSlot) this.customSlots.get(0)).setTag(true);
            ((PrinterSlot) this.customSlots.get(1)).SetItem(MKLGItems.StandardcardItem);
            ((PrinterSlot) this.customSlots.get(2)).SetItem(null);
            int si;
            int sj;
            for (si = 0; si < 3; ++si)
                for (sj = 0; sj < 9; ++sj)
                    this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
            for (si = 0; si < 9; ++si)
                this.addSlot(new Slot(inv, si, 8 + si * 18, 142));
        }


        public Map<Integer, Slot> get() {
            return customSlots;
        }

        @Override
        public boolean canInteractWith(PlayerEntity player) {
            return internal.isUsableByPlayer(player);
        }

        @Override
        public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
            ItemStack itemstack = ItemStack.EMPTY;
            Slot slot = this.inventorySlots.get(index);
            if (slot != null && slot.getHasStack()) {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();
                if (index < 3) {
                    if (!this.mergeItemStack(itemstack1, 3, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(itemstack1, itemstack);
                } else if (!this.mergeItemStack(itemstack1, 0, 3, false)) {
                    if (index < 3 + 27) {
                        if (!this.mergeItemStack(itemstack1, 3 + 27, this.inventorySlots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, 3, 3 + 27, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                    return ItemStack.EMPTY;
                }
                if (itemstack1.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }
                if (itemstack1.getCount() == itemstack.getCount()) {
                    return ItemStack.EMPTY;
                }
                slot.onTake(playerIn, itemstack1);
            }
            return itemstack;
        }

        @Override
        protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
            boolean flag = false;
            int i = startIndex;
            if (reverseDirection) {
                i = endIndex - 1;
            }
            if (stack.isStackable()) {
                while (!stack.isEmpty()) {
                    if (reverseDirection) {
                        if (i < startIndex) {
                            break;
                        }
                    } else if (i >= endIndex) {
                        break;
                    }
                    Slot slot = this.inventorySlots.get(i);
                    ItemStack itemstack = slot.getStack();
                    if (slot.isItemValid(itemstack) && !itemstack.isEmpty() && areItemsAndTagsEqual(stack, itemstack)) {
                        int j = itemstack.getCount() + stack.getCount();
                        int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                        if (j <= maxSize) {
                            stack.setCount(0);
                            itemstack.setCount(j);
                            slot.putStack(itemstack);
                            flag = true;
                        } else if (itemstack.getCount() < maxSize) {
                            stack.shrink(maxSize - itemstack.getCount());
                            itemstack.setCount(maxSize);
                            slot.putStack(itemstack);
                            flag = true;
                        }
                    }
                    if (reverseDirection) {
                        --i;
                    } else {
                        ++i;
                    }
                }
            }
            if (!stack.isEmpty()) {
                if (reverseDirection) {
                    i = endIndex - 1;
                } else {
                    i = startIndex;
                }
                while (true) {
                    if (reverseDirection) {
                        if (i < startIndex) {
                            break;
                        }
                    } else if (i >= endIndex) {
                        break;
                    }
                    Slot slot1 = this.inventorySlots.get(i);
                    ItemStack itemstack1 = slot1.getStack();
                    if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
                        if (stack.getCount() > slot1.getSlotStackLimit()) {
                            slot1.putStack(stack.split(slot1.getSlotStackLimit()));
                        } else {
                            slot1.putStack(stack.split(stack.getCount()));
                        }
                        slot1.onSlotChanged();
                        flag = true;
                        break;
                    }
                    if (reverseDirection) {
                        --i;
                    } else {
                        ++i;
                    }
                }
            }
            return flag;
        }

        @Override
        public void onContainerClosed(PlayerEntity playerIn) {
            super.onContainerClosed(playerIn);
            internal.closeInventory(playerIn);
            if ((internal instanceof Inventory) && (playerIn instanceof ServerPlayerEntity)) {
                this.clearContainer(playerIn, playerIn.world, internal);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class GuiWindow extends ContainerScreen<GuiContainerMod> {

        public GuiWindow(GuiContainerMod container, PlayerInventory inventory, ITextComponent text) {
            super(container, inventory, text);
            this.xSize = 176;
            this.ySize = 166;
        }

        private static final ResourceLocation texture = new ResourceLocation("pmtinfai:textures/printergui.png");

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            this.renderBackground();
            super.render(mouseX, mouseY, partialTicks);
            this.renderHoveredToolTip(mouseX, mouseY);
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(texture);
            int k = (this.width - this.xSize) / 2;
            int l = (this.height - this.ySize) / 2;
            this.blit(k, l, 0, 0, this.xSize, this.ySize);
        }

        @Override
        public void tick() {
            super.tick();
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        }

        @Override
        public void removed() {
            super.removed();
            Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
        }

        @Override
        public void init(Minecraft minecraft, int width, int height) {
            super.init(minecraft, width, height);
            minecraft.keyboardListener.enableRepeatEvents(true);
        }
    }

    public static class ButtonPressedMessage {
        int buttonID, x, y, z;

        public ButtonPressedMessage(PacketBuffer buffer) {
            this.buttonID = buffer.readInt();
            this.x = buffer.readInt();
            this.y = buffer.readInt();
            this.z = buffer.readInt();
        }

        public static void buffer(ButtonPressedMessage message, PacketBuffer buffer) {
            buffer.writeInt(message.buttonID);
            buffer.writeInt(message.x);
            buffer.writeInt(message.y);
            buffer.writeInt(message.z);
        }

        public static void handler(ButtonPressedMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
            });
            context.setPacketHandled(true);
        }
    }

    public static class GUISlotChangedMessage {
        int slotID, x, y, z, changeType, meta;

        public GUISlotChangedMessage(PacketBuffer buffer) {
            this.slotID = buffer.readInt();
            this.x = buffer.readInt();
            this.y = buffer.readInt();
            this.z = buffer.readInt();
            this.changeType = buffer.readInt();
            this.meta = buffer.readInt();
        }

        public static void buffer(GUISlotChangedMessage message, PacketBuffer buffer) {
            buffer.writeInt(message.slotID);
            buffer.writeInt(message.x);
            buffer.writeInt(message.y);
            buffer.writeInt(message.z);
            buffer.writeInt(message.changeType);
            buffer.writeInt(message.meta);
        }

        public static void handler(GUISlotChangedMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
            });
            context.setPacketHandled(true);
        }
    }
}
