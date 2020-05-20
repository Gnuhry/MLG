
package net.mcreator.pmtinfai.gui;

import net.mcreator.pmtinfai.PMTINFAI;
import net.mcreator.pmtinfai.PMTINFAIElements;
import net.mcreator.pmtinfai.block.CodebenchBlock;
import net.mcreator.pmtinfai.block.WorkbenchBlock;
import net.mcreator.pmtinfai.slots.CodeBenchSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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

import java.util.*;
import java.util.function.Supplier;

@PMTINFAIElements.ModElement.Tag
public class CodebenchGui extends PMTINFAIElements.ModElement {
    public static HashMap guistate = new HashMap();
    private static ContainerType<GuiContainerMod> containerType = null;

    public CodebenchGui(PMTINFAIElements instance) {
        super(instance, 17);
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
        event.getRegistry().register(containerType.setRegistryName("table"));
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
            this.internal = new Inventory(1);
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
            this.customSlots.put(0, this.addSlot(new CodeBenchSlot(internal, 0, 116, 50) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    if (!((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).getText().equals("")) {
                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putString("logic", ((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).getText());
                        nbt.putBoolean("logic_", true);
                        customSlots.get(0).getStack().setTag(nbt);
                        ((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).setText("");
                    } else if (customSlots.get(0).getStack().hasTag() && customSlots.get(0).getStack().getTag().contains("logic_")) {
                        ((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).setText(customSlots.get(0).getStack().getTag().getString("logic"));
                        TextFieldWidget tw = (TextFieldWidget) guistate.getOrDefault("text:Logiccode", null);
                        if (tw != null) {
                            tw.setText(customSlots.get(0).getStack().getTag().getString("logic"));
                        }
                    }else if(customSlots.get(0).getStack()==ItemStack.EMPTY){
                        TextFieldWidget tw = (TextFieldWidget) guistate.getOrDefault("text:Logiccode", null);
                        if (tw != null) {
                            tw.setText("");
                        }
                    }
                }
            }));
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
                if (index < 1) {
                    if (!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(itemstack1, itemstack);
                } else if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                    if (index < 1 + 27) {
                        if (!this.mergeItemStack(itemstack1, 1 + 27, this.inventorySlots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, 1, 1 + 27, false)) {
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
        private int x, y, z;
        private PlayerEntity entity;
        TextFieldWidget Logiccode;

        public GuiWindow(GuiContainerMod container, PlayerInventory inventory, ITextComponent text) {
            super(container, inventory, text);
            this.x = container.x;
            this.y = container.y;
            this.z = container.z;
            this.entity = container.entity;
            this.xSize = 176;
            this.ySize = 166;
        }

        private static final ResourceLocation texture = new ResourceLocation("pmtinfai:textures/gui/codetablegui.png");

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            this.renderBackground();
            super.render(mouseX, mouseY, partialTicks);
            this.renderHoveredToolTip(mouseX, mouseY);
            Logiccode.render(mouseX, mouseY, partialTicks);
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
            Logiccode.tick();
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
            this.font.drawString(I18n.format("gui.pmtinfai.codebench"), 80, 5, -16777216);
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
            Logiccode = new TextFieldWidget(this.font, this.guiLeft + 20, this.guiTop + 20, 120, 20, "Logic Code");
            guistate.put("text:Logiccode", Logiccode);
            Logiccode.setMaxStringLength(32767);
            this.children.add(this.Logiccode);
            this.addButton(new Button(this.guiLeft + 25, this.guiTop + 47, 50, 20, "check", e -> {
                PMTINFAI.PACKET_HANDLER.sendToServer(new ButtonPressedMessage(0, x, y, z));
                handleButtonAction(entity, x, y, z);
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
                int x = message.x;
                int y = message.y;
                int z = message.z;
                assert entity != null;
                handleButtonAction(entity, x, y, z);
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

    private static void handleButtonAction(PlayerEntity entity, int x, int y, int z) {
        World world = entity.world;
        // security measure to prevent arbitrary chunk generation
        if (!world.isBlockLoaded(new BlockPos(x, y, z)))
            return;
        String text = ((TextFieldWidget) guistate.get("text:Logiccode")).getText();
        String[] exp = text.split(",");
        boolean b = exp.length == 3;
        if (b) {
            for (int f = 0; f < exp.length; f++) {
                b = b && CheckExpression(exp[f], 2 - f);
            }
        }
        MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
        if (b) {
            CodebenchBlock.CustomTileEntity ct = ((CodebenchBlock.CustomTileEntity) world.getTileEntity(new BlockPos(x, y, z)));
            if (ct.getStackInSlot(0).isEmpty()) {
                if (mcserv != null)
                    mcserv.getPlayerList().sendMessage(new StringTextComponent(I18n.format("gui.pmtinfai.save")));
                ((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).setText(text);
            } else {
                if (mcserv != null)
                    mcserv.getPlayerList().sendMessage(new StringTextComponent(I18n.format("gui.pmtinfai.saved")));
                ((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).setText(text);
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("logic", ((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).getText());
                nbt.putBoolean("logic_", true);
                ct.getStackInSlot(0).setTag(nbt);
                ((CodebenchBlock.CustomTileEntity) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z)))).setText("");
                TextFieldWidget tw = (TextFieldWidget) guistate.getOrDefault("text:Logiccode", null);
                if (tw != null) {
                    tw.setText("");
                }
            }
        } else {
            if (mcserv != null)
                mcserv.getPlayerList().sendMessage(new StringTextComponent(I18n.format("gui.pmtinfai.wrong")));
        }
    }

    private static boolean CheckExpression(String exp, int i) {
        List<Character> allowed = new ArrayList<>();
        allowed.add('(');
        allowed.add(')');
        allowed.add('!');
        List<Character> literal = new ArrayList<>();
        literal.add('T');
        literal.add('F');
        literal.add('A');
        if (i > 0)
            literal.add('B');
        if (i > 1)
            literal.add('C');
        List<Character> literal2 = new ArrayList<>();
        literal2.add('&');
        literal2.add('|');
        // Checking Expression
        char[] help = exp.toCharArray();
        List<Character> x = new ArrayList<>();
        for (char c : help) {
            if (!literal.contains(c) && !literal2.contains(c) && !allowed.contains(c))
                return false;
            x.add(c);
        }
        for (int f = 0; f < x.size(); f++) {
            if (x.get(f) == ')') {
                for (int g = f - 1; g >= 0; g--) {
                    if (x.get(g) == '(') {
                        if (f - g == 4) {
                            if (!literal.contains(x.get(g + 1)))
                                return false;
                            if (!literal.contains(x.get(g + 3)))
                                return false;
                            if (!literal2.contains(x.get(g + 2)))
                                return false;
                            x.set(f, 'T');
                            x.subList(g, f).clear();
                            f = 0;
                            g = -1;
                        } else if (f - g == 3) {
                            if (!literal.contains(x.get(g + 2)))
                                return false;
                            if (x.get(g + 1) != '!')
                                return false;
                            x.set(f, 'T');
                            x.subList(g, f).clear();
                            f = 0;
                            g = -1;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return x.size() == 1 && (literal.contains(x.get(0)));
    }
}
