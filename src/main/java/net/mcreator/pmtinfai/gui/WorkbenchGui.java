
package net.mcreator.pmtinfai.gui;

import net.mcreator.pmtinfai.MKLGItems;
import net.mcreator.pmtinfai.PMTINFAI;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.Recipe_MKLG;
import net.mcreator.pmtinfai.block.WorkbenchBlock;
import net.mcreator.pmtinfai.enums.LogicKinds;
import net.mcreator.pmtinfai.slots.DisplaySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
public class WorkbenchGui extends PMTINFAIElements.ModElement {
    private static ContainerType<GuiContainerMod> containerType = null;

    public WorkbenchGui(PMTINFAIElements instance) {
        super(instance, 18);
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
        event.getRegistry().register(containerType.setRegistryName("tischgui2"));
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
            this.internal = new Inventory(11);
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
            this.customSlots.put(0, this.addSlot(new Slot(internal, 0, 8, 57) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return (((WorkbenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).getKind() < 27) && MKLGItems.Cable == stack.getItem();
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    int amount_ = ((WorkbenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).getKind();
                    int[] amount = Recipe_MKLG.CheckRecipe(LogicKinds.Get(amount_));
                    if (amount_ > 26) return;
                    while (true) {
                        for (int f = 0; f < amount.length; f++) {
                            if (amount[f] > customSlots.get(f).getStack().getCount())
                                return;
                        }
                        if (customSlots.get(8).getStack().getCount() > 0) {
                            Slot set = customSlots.get(9);
                            for (int f = 0; f < amount.length; f++) {
                                customSlots.get(f).getStack().shrink(amount[f]);
                            }
                            customSlots.get(8).getStack().shrink(1);
                            if (set.getHasStack() && set.getStack().getTag() == LogicKinds.Get(amount_).GetNBT()) {
                                set.getStack().grow(1);
                            } else {
                                ItemStack is = new ItemStack(MKLGItems.StandardcardItem, 1);
                                is.setTag(LogicKinds.Get(amount_).GetNBT());
                                set.putStack(is);
                            }
                        } else {
                            return;
                        }
                    }

                }
            }));
            this.customSlots.put(1, this.addSlot(new Slot(internal, 1, 26, 57) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return (((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind() < 27) && MKLGItems.Pin == stack.getItem();
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    int amount_ = ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind();
                    int[] amount = Recipe_MKLG.CheckRecipe(LogicKinds.Get(amount_));
                    if (amount_ > 26) return;
                    while (true) {
                        for (int f = 0; f < amount.length; f++) {
                            if (amount[f] > customSlots.get(f).getStack().getCount())
                                return;
                        }
                        if (customSlots.get(8).getStack().getCount() > 0) {
                            Slot set = customSlots.get(9);
                            for (int f = 0; f < amount.length; f++) {
                                customSlots.get(f).getStack().shrink(amount[f]);
                            }
                            customSlots.get(8).getStack().shrink(1);
                            if (set.getHasStack() && set.getStack().getTag() == LogicKinds.Get(amount_).GetNBT()) {
                                set.getStack().grow(1);
                            } else {
                                ItemStack is = new ItemStack(MKLGItems.StandardcardItem, 1);
                                is.setTag(LogicKinds.Get(amount_).GetNBT());
                                set.putStack(is);
                            }
                        } else {
                            return;
                        }
                    }

                }
            }));
            this.customSlots.put(2, this.addSlot(new Slot(internal, 2, 44, 57) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return (((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind() < 27) && MKLGItems.Memory == stack.getItem();
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    int amount_ = ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind();
                    int[] amount = Recipe_MKLG.CheckRecipe(LogicKinds.Get(amount_));
                    if (amount_ > 26) return;
                    while (true) {
                        for (int f = 0; f < amount.length; f++) {
                            if (amount[f] > customSlots.get(f).getStack().getCount())
                                return;
                        }
                        if (customSlots.get(8).getStack().getCount() > 0) {
                            Slot set = customSlots.get(9);
                            for (int f = 0; f < amount.length; f++) {
                                customSlots.get(f).getStack().shrink(amount[f]);
                            }
                            customSlots.get(8).getStack().shrink(1);
                            if (set.getHasStack() && set.getStack().getTag() == LogicKinds.Get(amount_).GetNBT()) {
                                set.getStack().grow(1);
                            } else {
                                ItemStack is = new ItemStack(MKLGItems.StandardcardItem, 1);
                                is.setTag(LogicKinds.Get(amount_).GetNBT());
                                set.putStack(is);
                            }
                        } else {
                            return;
                        }
                    }

                }
            }));
            this.customSlots.put(3, this.addSlot(new Slot(internal, 3, 62, 57) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return (((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind() < 27) &&MKLGItems.Resistor == stack.getItem();
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    int amount_ = ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind();
                    int[] amount = Recipe_MKLG.CheckRecipe(LogicKinds.Get(amount_));
                    if (amount_ > 26) return;
                    while (true) {
                        for (int f = 0; f < amount.length; f++) {
                            if (amount[f] > customSlots.get(f).getStack().getCount())
                                return;
                        }
                        if (customSlots.get(8).getStack().getCount() > 0) {
                            Slot set = customSlots.get(9);
                            for (int f = 0; f < amount.length; f++) {
                                customSlots.get(f).getStack().shrink(amount[f]);
                            }
                            customSlots.get(8).getStack().shrink(1);
                            if (set.getHasStack() && set.getStack().getTag() == LogicKinds.Get(amount_).GetNBT()) {
                                set.getStack().grow(1);
                            } else {
                                ItemStack is = new ItemStack(MKLGItems.StandardcardItem, 1);
                                is.setTag(LogicKinds.Get(amount_).GetNBT());
                                set.putStack(is);
                            }
                        } else {
                            return;
                        }
                    }

                }
            }));
            this.customSlots.put(4, this.addSlot(new DisplaySlot(internal, 4, 8, 35) {

            }));
            this.customSlots.put(5, this.addSlot(new DisplaySlot(internal, 5, 26, 35) {

            }));
            this.customSlots.put(6, this.addSlot(new DisplaySlot(internal, 6, 44, 35) {

            }));
            this.customSlots.put(7, this.addSlot(new DisplaySlot(internal, 7, 62, 35) {

            }));
            this.customSlots.put(8, this.addSlot(new Slot(internal, 8, 134, 57) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return MKLGItems.StandardcardItem == stack.getItem();
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    int amount_ = ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind();
                    int[] amount = Recipe_MKLG.CheckRecipe(LogicKinds.Get(amount_));
                    if (amount_ > 26) return;
                    while (true) {
                        for (int f = 0; f < amount.length; f++) {
                            if (amount[f] > customSlots.get(f).getStack().getCount())
                                return;
                        }
                        if (customSlots.get(8).getStack().getCount() > 0) {
                            Slot set = customSlots.get(9);
                            for (int f = 0; f < amount.length; f++) {
                                customSlots.get(f).getStack().shrink(amount[f]);
                            }
                            customSlots.get(8).getStack().shrink(1);
                            if (set.getHasStack() && set.getStack().getTag() == LogicKinds.Get(amount_).GetNBT()) {
                                set.getStack().grow(1);
                            } else {
                                ItemStack is = new ItemStack(MKLGItems.StandardcardItem, 1);
                                is.setTag(LogicKinds.Get(amount_).GetNBT());
                                set.putStack(is);
                            }
                        } else {
                            return;
                        }
                    }

                }
            }));
            this.customSlots.put(9, this.addSlot(new Slot(internal, 9, 134, 21) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    int amount_ = ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).getKind();
                    int[] amount = Recipe_MKLG.CheckRecipe(LogicKinds.Get(amount_));
                    if (amount_ > 26) return;
                    while (true) {
                        for (int f = 0; f < amount.length; f++) {
                            if (amount[f] > customSlots.get(f).getStack().getCount())
                                return;
                        }
                        if (customSlots.get(8).getStack().getCount() > 0) {
                            Slot set = customSlots.get(9);
                            for (int f = 0; f < amount.length; f++) {
                                customSlots.get(f).getStack().shrink(amount[f]);
                            }
                            customSlots.get(8).getStack().shrink(1);
                            if (set.getHasStack() && set.getStack().getTag() == LogicKinds.Get(amount_).GetNBT()) {
                                set.getStack().grow(1);
                            } else {
                                ItemStack is = new ItemStack(MKLGItems.StandardcardItem, 1);
                                is.setTag(LogicKinds.Get(amount_).GetNBT());
                                set.putStack(is);
                            }
                        } else {
                            return;
                        }
                    }

                }
            }));
            this.customSlots.put(10, this.addSlot(new DisplaySlot(internal, 10, 134, 35) {

            }));
            WorkbenchBlock.CustomTileEntity wb = ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z)));
            int[] set = Recipe_MKLG.CheckRecipe(LogicKinds.Get(wb.getKind()));
            this.customSlots.get(4).putStack(new ItemStack(MKLGItems.Cable, set[0]));
            this.customSlots.get(5).putStack(new ItemStack(MKLGItems.Pin, set[1]));
            this.customSlots.get(6).putStack(new ItemStack(MKLGItems.Memory, set[2]));
            this.customSlots.get(7).putStack(new ItemStack(MKLGItems.Resistor, set[3]));
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
                if (index < 11) {
                    if (!this.mergeItemStack(itemstack1, 11, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(itemstack1, itemstack);
                } else if (!this.mergeItemStack(itemstack1, 0, 11, false)) {
                    if (index < 11 + 27) {
                        if (!this.mergeItemStack(itemstack1, 11 + 27, this.inventorySlots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, 11, 11 + 27, false)) {
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
        private World world;
        private int x, y, z;
        private PlayerEntity entity;

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

        private static final ResourceLocation texture = new ResourceLocation("pmtinfai:textures/gui/worktablegui.png");

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
            this.font.drawString("R-S-FF", 178, 11, -60392);
            this.font.drawString("D-FF", 178, 47, -60392);
            this.font.drawString("T-FF", 178, 83, -60392);
            this.font.drawString("J-K-FF", 178, 119, -60392);
            this.font.drawString("Name", 80, 5, -16777216);
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
            this.addButton(new Button(this.guiLeft + -29, this.guiTop + 11, 27, 20, "AND", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(0, x, y, z));
                handleButtonAction(entity, 0, x, y, z);
            }) {
            });
            this.addButton(new Button(this.guiLeft + -29, this.guiTop + 29, 27, 20, "OR", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(1, x, y, z));
                handleButtonAction(entity, 1, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + -29, this.guiTop + 47, 27, 20, "NOT", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(2, x, y, z));
                handleButtonAction(entity, 2, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + -29, this.guiTop + 65, 27, 20, "NAND", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(3, x, y, z));
                handleButtonAction(entity, 3, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + -29, this.guiTop + 83, 27, 20, "NOR", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(4, x, y, z));
                handleButtonAction(entity, 4, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + -29, this.guiTop + 101, 27, 20, "XOR", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(5, x, y, z));
                handleButtonAction(entity, 5, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + -29, this.guiTop + 119, 27, 20, "XNOR", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(6, x, y, z));
                handleButtonAction(entity, 6, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 178, this.guiTop + 20, 10, 20, "0", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(7, x, y, z));
                handleButtonAction(entity, 7, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 205, this.guiTop + 20, 10, 20, "P", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(8, x, y, z));
                handleButtonAction(entity, 8, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 178, this.guiTop + 56, 10, 20, "0", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(9, x, y, z));
                handleButtonAction(entity, 9, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 196, this.guiTop + 20, 10, 20, "-", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(10, x, y, z));
                handleButtonAction(entity, 10, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 196, this.guiTop + 56, 10, 20, "-", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(11, x, y, z));
                handleButtonAction(entity, 11, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 205, this.guiTop + 56, 10, 20, "P", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(12, x, y, z));
                handleButtonAction(entity, 12, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 178, this.guiTop + 92, 10, 20, "0", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(13, x, y, z));
                handleButtonAction(entity, 13, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 187, this.guiTop + 20, 10, 20, "+", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(14, x, y, z));
                handleButtonAction(entity, 14, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 187, this.guiTop + 56, 10, 20, "+", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(15, x, y, z));
                handleButtonAction(entity, 15, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 187, this.guiTop + 92, 10, 20, "+", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(16, x, y, z));
                handleButtonAction(entity, 16, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 196, this.guiTop + 92, 10, 20, "-", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(17, x, y, z));
                handleButtonAction(entity, 17, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 205, this.guiTop + 92, 10, 20, "P", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(18, x, y, z));
                handleButtonAction(entity, 18, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 214, this.guiTop + 20, 10, 20, "M", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(19, x, y, z));
                handleButtonAction(entity, 19, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 214, this.guiTop + 56, 10, 20, "M", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(20, x, y, z));
                handleButtonAction(entity, 20, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 214, this.guiTop + 92, 10, 20, "M", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(21, x, y, z));
                handleButtonAction(entity, 21, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 178, this.guiTop + 128, 10, 20, "0", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(22, x, y, z));
                handleButtonAction(entity, 22, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 187, this.guiTop + 128, 10, 20, "+", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(23, x, y, z));
                handleButtonAction(entity, 23, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 196, this.guiTop + 128, 10, 20, "-", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(24, x, y, z));
                handleButtonAction(entity, 24, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 205, this.guiTop + 128, 10, 20, "P", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(25, x, y, z));
                handleButtonAction(entity, 25, x, y, z);
            }));
            this.addButton(new Button(this.guiLeft + 214, this.guiTop + 128, 10, 20, "M", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(26, x, y, z));
                handleButtonAction(entity, 26, x, y, z);
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
                assert entity != null;
                handleButtonAction(entity, buttonID, x, y, z);
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

    private static void handleButtonAction(PlayerEntity entity, int buttonID, int x, int y, int z) {
        World world = entity.world;
        // security measure to prevent arbitrary chunk generation
        if (!world.isBlockLoaded(new BlockPos(x, y, z)))
            return;
        LogicKinds lk = LogicKinds.Get(buttonID);
        int[] help = Recipe_MKLG.CheckRecipe(lk);
        ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z))).setKind(lk.Get());
        WorkbenchBlock.CustomTileEntity ct = ((WorkbenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z)));
        ct.setInventorySlotContents(4, new ItemStack(MKLGItems.Cable, help[0]));
        ct.setInventorySlotContents(5, new ItemStack(MKLGItems.Pin, help[1]));
        ct.setInventorySlotContents(6, new ItemStack(MKLGItems.Memory, help[2]));
        ct.setInventorySlotContents(7, new ItemStack(MKLGItems.Resistor, help[3]));
        ItemStack erg = new ItemStack(MKLGItems.StandardcardItem, 1);
        erg.setTag(lk.GetNBT());
        ct.setInventorySlotContents(10, erg);

    }
}
