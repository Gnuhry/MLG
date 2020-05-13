
package net.mcreator.pmtinfai.gui;

import net.mcreator.pmtinfai.PMTINFAI;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.block.TimerBlock;
import net.mcreator.pmtinfai.slots.LogicSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
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
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
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
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@PMTINFAIElements.ModElement.Tag
public class TimerGui extends PMTINFAIElements.ModElement {
    public static HashMap guistate = new HashMap();
    private static ContainerType<GuiContainerMod> containerType = null;

    public TimerGui(PMTINFAIElements instance) {
        super(instance, 38);
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
        event.getRegistry().register(containerType.setRegistryName("timer"));
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
        private TimerBlock.CustomBlock lb;

        public GuiContainerMod(int id, PlayerInventory inv, PacketBuffer extraData) {
            super(containerType, id);
            this.entity = inv.player;
            this.world = inv.player.world;
            this.internal = new Inventory(4);
            if (extraData != null) {
                BlockPos pos = extraData.readBlockPos();
                this.x = pos.getX();
                this.y = pos.getY();
                this.z = pos.getZ();
                lb = (TimerBlock.CustomBlock) world.getBlockState(pos).getBlock();
                TileEntity ent = inv.player != null ? inv.player.world.getTileEntity(pos) : null;
                if (ent instanceof IInventory)
                    this.internal = (IInventory) ent;
            }
            internal.openInventory(inv.player);
            this.customSlots.put(0, this.addSlot(new LogicSlot(internal, 0, 35, 30) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(0).getItem();
                    boolean[] io_boolean = lb.changeInput(0, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        ((LogicSlot) customSlots.get(f)).input = io_boolean[0];
                        ((LogicSlot) customSlots.get(f)).output = io_boolean[1];
                    }
                }
            }));
            this.customSlots.put(1, this.addSlot(new LogicSlot(internal, 1, 53, 12) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(1).getItem();
                    boolean[] io_boolean = lb.changeInput(1, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        ((LogicSlot) customSlots.get(f)).input = io_boolean[0];
                        ((LogicSlot) customSlots.get(f)).output = io_boolean[1];
                    }
                }
            }));
            this.customSlots.put(2, this.addSlot(new LogicSlot(internal, 2, 71, 30) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(2).getItem();
                    boolean[] io_boolean = lb.changeInput(2, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        ((LogicSlot) customSlots.get(f)).input = io_boolean[0];
                        ((LogicSlot) customSlots.get(f)).output = io_boolean[1];
                    }
                }
            }));
            this.customSlots.put(3, this.addSlot(new LogicSlot(internal, 3, 53, 48) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    Item item = internal.getStackInSlot(3).getItem();
                    boolean[] io_boolean = lb.changeInput(3, new BlockPos(x, y, z), world, item);
                    for (int f = 0; f < 4; f++) {
                        ((LogicSlot) customSlots.get(f)).input = io_boolean[0];
                        ((LogicSlot) customSlots.get(f)).output = io_boolean[1];
                    }
                }
            }));
            int si;
            int sj;
            for (si = 0; si < 3; ++si)
                for (sj = 0; sj < 9; ++sj)
                    this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
            for (si = 0; si < 9; ++si)
                this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142));
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
            Slot slot = (Slot) this.inventorySlots.get(index);
            if (slot != null && slot.getHasStack()) {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();
                if (index < 4) {
                    if (!this.mergeItemStack(itemstack1, 4, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(itemstack1, itemstack);
                } else if (!this.mergeItemStack(itemstack1, 0, 4, false)) {
                    if (index < 4 + 27) {
                        if (!this.mergeItemStack(itemstack1, 4 + 27, this.inventorySlots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, 4, 4 + 27, false)) {
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
        /**
         * Merges provided ItemStack with the first avaliable one in the
         * container/player inventor between minIndex (included) and maxIndex
         * (excluded). Args : stack, minIndex, maxIndex, negativDirection. /!\ the
         * Container implementation do not check if the item is valid for the slot
         */
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

        private void slotChanged(int slotid, int ctype, int meta) {
            if (this.world != null && this.world.isRemote) {
                PMTINFAI.PACKET_HANDLER.sendToServer(new GUISlotChangedMessage(slotid, x, y, z, ctype, meta));
                handleSlotAction(entity, slotid, ctype, meta, x, y, z);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class GuiWindow extends ContainerScreen<GuiContainerMod> {
        private World world;
        private int x, y, z;
        private PlayerEntity entity;
        TextFieldWidget ticks;

        public GuiWindow(GuiContainerMod container, PlayerInventory inventory, ITextComponent text) {
            super(container, inventory, text);
            this.world = container.world;
            this.x = container.x;
            this.y = container.y;
            this.z = container.z;
            this.entity = container.entity;
            this.xSize = 176;
            this.ySize = 166;
        }

        private static final ResourceLocation texture = new ResourceLocation("pmtinfai:textures/gui/timergui.png");

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            this.renderBackground();
            super.render(mouseX, mouseY, partialTicks);
            this.renderHoveredToolTip(mouseX, mouseY);
            ticks.render(mouseX, mouseY, partialTicks);
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
            ticks.tick();
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
            this.font.drawString("N", 58, 1, -16777216);
            this.font.drawString("E", 27, 33, -16777216);
            this.font.drawString("S", 57, 65, -16777216);
            this.font.drawString("W", 90, 33, -16777216);
            this.font.drawString("I/O", 17, 6, -16777216);
            this.font.drawString("Ticks", 134, 6, -16777216);
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
            ticks = new TextFieldWidget(this.font, 240, 64, 50, 20, "Ticks");
            guistate.put("text:ticks", ticks);
            int i=((TimerBlock.CustomTileEntity)world.getTileEntity(new BlockPos(x,y,z))).getTimer();
            if(i!=-1)
                ticks.setText(i+"");
            ticks.setMaxStringLength(32767);
            this.children.add(this.ticks);
            this.addButton(new Button(this.guiLeft + 133, this.guiTop + 53, 30, 20, "Set", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(0, x, y, z));
                handleButtonAction(entity, 0, x, y, z);
            }));
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

        public ButtonPressedMessage(int buttonID, int x, int y, int z) {
            this.buttonID = buttonID;
            this.x = x;
            this.y = y;
            this.z = z;
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
                PlayerEntity entity = context.getSender();
                int buttonID = message.buttonID;
                int x = message.x;
                int y = message.y;
                int z = message.z;
                handleButtonAction(entity, buttonID, x, y, z);
            });
            context.setPacketHandled(true);
        }
    }

    public static class GUISlotChangedMessage {
        int slotID, x, y, z, changeType, meta;

        public GUISlotChangedMessage(int slotID, int x, int y, int z, int changeType, int meta) {
            this.slotID = slotID;
            this.x = x;
            this.y = y;
            this.z = z;
            this.changeType = changeType;
            this.meta = meta;
        }

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
                PlayerEntity entity = context.getSender();
                int slotID = message.slotID;
                int changeType = message.changeType;
                int meta = message.meta;
                int x = message.x;
                int y = message.y;
                int z = message.z;
                handleSlotAction(entity, slotID, changeType, meta, x, y, z);
            });
            context.setPacketHandled(true);
        }
    }

    private static void handleButtonAction(PlayerEntity entity, int buttonID, int x, int y, int z) {
        World world = entity.world;
        // security measure to prevent arbitrary chunk generation
        if (!world.isBlockLoaded(new BlockPos(x, y, z)))
            return;
        String s = ((TextFieldWidget) guistate.get("text:ticks")).getText();
        try {
            int i = Integer.parseInt(s);
            if(i<0)
                throw new NumberFormatException();
            ((TimerBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).setTimer(i);
            return;
        } catch (NumberFormatException e) {
        } catch (NullPointerException e) {
        }
        ((TextFieldWidget) guistate.get("text:ticks")).setText("");
        ((TimerBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).setTimer(0);

    }

    private static void handleSlotAction(PlayerEntity entity, int slotID, int changeType, int meta, int x, int y, int z) {
        World world = entity.world;
        // security measure to prevent arbitrary chunk generation
        if (!world.isBlockLoaded(new BlockPos(x, y, z)))
            return;
    }
}
