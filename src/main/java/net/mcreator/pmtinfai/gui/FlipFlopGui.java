
package net.mcreator.pmtinfai.gui;

import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.block.FlipFlopBlock;
import net.mcreator.pmtinfai.slots.KernelSlot;
import net.mcreator.pmtinfai.slots.FlipFlopSlot;
import net.mcreator.pmtinfai.slots.LogicSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
import java.util.Objects;
import java.util.function.Supplier;

@PMTINFAIElements.ModElement.Tag
public class FlipFlopGui extends PMTINFAIElements.ModElement {
    private static ContainerType<GuiContainerMod> containerType = null;

    public FlipFlopGui(PMTINFAIElements instance) {
        super(instance, 25);
        elements.addNetworkMessage(ButtonPressedMessage.class, ButtonPressedMessage::buffer, ButtonPressedMessage::new,
                ButtonPressedMessage::handler);
        containerType = new ContainerType<>(new GuiContainerModFactory());
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @OnlyIn(Dist.CLIENT)
    public void initElements() {
        ScreenManager.registerFactory(containerType, GuiWindow::new);
    }

    @SubscribeEvent
    public void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(containerType.setRegistryName("flipflopgui"));
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
        private FlipFlopBlock.CustomBlock lb;

        public GuiContainerMod(int id, PlayerInventory inv, PacketBuffer extraData) {
            super(containerType, id);
            this.entity = inv.player;
            this.world = inv.player.world;
            this.internal = new Inventory(5);
            if (extraData != null) {
                BlockPos pos = extraData.readBlockPos();
                this.x = pos.getX();
                this.y = pos.getY();
                this.z = pos.getZ();
                lb = (FlipFlopBlock.CustomBlock) world.getBlockState(pos).getBlock();
                TileEntity ent = inv.player.world.getTileEntity(pos);
                if (ent instanceof IInventory)
                    this.internal = (IInventory) ent;
            }
            internal.openInventory(inv.player);
            this.customSlots.put(0, this.addSlot(new FlipFlopSlot(internal, 0, 35, 30) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(0).getItem();
                    boolean[] io_boolean = lb.changeInput(0, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        FlipFlopSlot slot = ((FlipFlopSlot) customSlots.get(f));
                        slot.set = io_boolean[0];
                        slot.reset = io_boolean[1];
                        slot.clock = io_boolean[2];
                        slot.output = io_boolean[3];
                    }
                }
            }));
            this.customSlots.put(1, this.addSlot(new FlipFlopSlot(internal, 1, 53, 12) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(1).getItem();
                    boolean[] io_boolean = lb.changeInput(1, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        FlipFlopSlot slot = ((FlipFlopSlot) customSlots.get(f));
                        slot.set = io_boolean[0];
                        slot.reset = io_boolean[1];
                        slot.clock = io_boolean[2];
                        slot.output = io_boolean[3];
                    }
                }
            }));
            this.customSlots.put(2, this.addSlot(new FlipFlopSlot(internal, 2, 71, 30) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(2).getItem();
                    boolean[] io_boolean = lb.changeInput(2, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        FlipFlopSlot slot = ((FlipFlopSlot) customSlots.get(f));
                        slot.set = io_boolean[0];
                        slot.reset = io_boolean[1];
                        slot.clock = io_boolean[2];
                        slot.output = io_boolean[3];
                    }
                }
            }));
            this.customSlots.put(3, this.addSlot(new FlipFlopSlot(internal, 3, 53, 48) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(3).getItem();
                    boolean[] io_boolean = lb.changeInput(3, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        FlipFlopSlot slot = ((FlipFlopSlot) customSlots.get(f));
                        slot.set = io_boolean[0];
                        slot.reset = io_boolean[1];
                        slot.clock = io_boolean[2];
                        slot.output = io_boolean[3];
                    }
                }
            }));
            this.customSlots.put(4, this.addSlot(new KernelSlot(internal, 4, 138, 31) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    try {
                        lb.GetAllStates(Objects.requireNonNull(internal.getStackInSlot(4).getTag()).getString("logic"), world, new BlockPos(x, y, z));
                    } catch (Exception ed) {
                        lb.GetAllStates("none", world, new BlockPos(x, y, z));
                    }
                    boolean[] io_boolean = lb.IO_State(world.getBlockState(new BlockPos(x,y,z)),null);
                    for (int f = 0; f < 4; f++) {
                        FlipFlopSlot slot = ((FlipFlopSlot) customSlots.get(f));
                        slot.set = io_boolean[0];
                        slot.reset = io_boolean[1];
                        slot.clock = io_boolean[2];
                        slot.output = io_boolean[3];
                    }
                }
            }));
            boolean[] io_boolean = lb.IO_State(world.getBlockState(new BlockPos(x,y,z)),null);
            for (int f = 0; f < 4; f++) {
                FlipFlopSlot slot = ((FlipFlopSlot) customSlots.get(f));
                slot.set = io_boolean[0];
                slot.reset = io_boolean[1];
                slot.clock = io_boolean[2];
                slot.output = io_boolean[3];
            }
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
                if (index < 5) {
                    if (!this.mergeItemStack(itemstack1, 5, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(itemstack1, itemstack);
                } else if (!this.mergeItemStack(itemstack1, 0, 5, false)) {
                    if (index < 5 + 27) {
                        if (!this.mergeItemStack(itemstack1, 5 + 27, this.inventorySlots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, 5, 5 + 27, false)) {
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

        private static final ResourceLocation texture = new ResourceLocation("pmtinfai:textures/gui/flipflopgui.png");

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
            this.font.drawString(I18n.format("gui.pmtinfai.n"), 58, 3, -16777216);
            this.font.drawString(I18n.format("gui.pmtinfai.w"), 27, 33, -16777216);
            this.font.drawString(I18n.format("gui.pmtinfai.s"), 57, 65, -16777216);
            this.font.drawString(I18n.format("gui.pmtinfai.e"), 90, 33, -16777216);
            this.font.drawString(I18n.format("gui.pmtinfai.io"), 17, 6, -16777216);
            this.font.drawString(I18n.format("gui.pmtinfai.logic"), 134, 6, -16777216);
            this.font.drawString(I18n.format("gui.pmtinfai.flipflop"), 80, 5, -16777216);

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

}
