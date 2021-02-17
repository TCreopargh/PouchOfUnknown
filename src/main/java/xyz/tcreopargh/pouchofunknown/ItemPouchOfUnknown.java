package xyz.tcreopargh.pouchofunknown;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.server.command.TextComponentHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static xyz.tcreopargh.pouchofunknown.PouchOfUnknownEvents.isQualified;


@SuppressWarnings("NullableProblems")
@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class ItemPouchOfUnknown extends Item implements IBauble {

    public static final ItemPouchOfUnknown itemPouchOfUnknown = new ItemPouchOfUnknown();
    public static final String registryName = PouchOfUnknownMod.MODID + ":" + "pouch";

    public ItemPouchOfUnknown() {
        this.setTranslationKey("pouchofunknown.pouch");
        this.setRegistryName(registryName);
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);
    }

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(itemPouchOfUnknown);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelReg(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemPouchOfUnknown, 0, new ModelResourceLocation(itemPouchOfUnknown.getRegistryName(), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerRenders(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemPouchOfUnknown, 0, new ModelResourceLocation(itemPouchOfUnknown.getRegistryName(), "inventory"));
    }

    public static ItemStack insertItem(ItemStack pouch, ItemStack stack) {
        IItemHandler handler = getItemHandler(pouch);
        if (handler instanceof InventoryHandler) {
            InventoryHandler inventoryHandler = (InventoryHandler) handler;
            return inventoryHandler.insert(stack);
        }
        return null;
    }

    public static ItemStack extractItem(ItemStack pouch) {
        IItemHandler handler = getItemHandler(pouch);
        if (handler instanceof InventoryHandler) {
            InventoryHandler inventoryHandler = (InventoryHandler) handler;
            return inventoryHandler.extractLast();
        }
        return null;
    }

    public static IItemHandler getItemHandler(ItemStack stack) {
        return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    public static NonNullList<ItemStack> getItems(ItemStack pouch) {
        IItemHandler handler = getItemHandler(pouch);
        if (handler instanceof InventoryHandler) {
            InventoryHandler inventoryHandler = (InventoryHandler) handler;
            return inventoryHandler.getStacks();
        }
        return null;
    }

    public static void save(ItemStack stack) {
        if (getItemHandler(stack) instanceof InventoryHandler) {
            InventoryHandler handler = (InventoryHandler) getItemHandler(stack);
            handler.save();
        }
    }

    public static void load(ItemStack stack) {
        if (getItemHandler(stack) instanceof InventoryHandler) {
            InventoryHandler handler = (InventoryHandler) getItemHandler(stack);
            handler.load();
        }
    }

    public static void forceLoad(ItemStack stack) {
        if (getItemHandler(stack) instanceof InventoryHandler) {
            InventoryHandler handler = (InventoryHandler) getItemHandler(stack);
            handler.forceLoad();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            ItemStack pouch = stack;
            int canPickupItemCount = 0;
            int totalItemCount = 0;
            NonNullList<ItemStack> items = ItemPouchOfUnknown.getItems(pouch);
            if (items != null) {
                for (ItemStack newStack : items) {
                    if (!newStack.isEmpty() && isQualified(player, newStack, false)) {
                        canPickupItemCount++;
                    }
                    if (!newStack.isEmpty()) {
                        totalItemCount++;
                    }
                }
            }
            String pickupItemDisplay = TextFormatting.GREEN + I18n.format("pouchofunknown.tooltip.pickupable", String.valueOf(canPickupItemCount));
            String totalItemDisplay = TextFormatting.YELLOW + I18n.format("pouchofunknown.tooltip.total", String.valueOf(totalItemCount));
            tooltip.add(pickupItemDisplay);
            tooltip.add(totalItemDisplay);
            tooltip.add(TextFormatting.GRAY + I18n.format("pouchofunknown.tooltip.maxcapacity", String.valueOf(PouchConfig.pouchCapacity)));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn instanceof EntityPlayerMP && !worldIn.isRemote) {
            EntityPlayerMP player = (EntityPlayerMP) playerIn;
            ItemStack pouch = playerIn.getHeldItem(handIn);
            load(pouch);
            boolean pickUpAll = player.isSneaking();
            int dropCount = 0;
            NonNullList<ItemStack> items = getItems(pouch);
            int validCount = 0;
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    validCount++;
                }
            }
            for (int i = 0; i < validCount; i++) {
                IItemHandler handler = getItemHandler(pouch);
                if (handler instanceof InventoryHandler) {
                    InventoryHandler inventoryHandler = (InventoryHandler) handler;
                    for (int j = items.size() - 1; j >= 0; j--) {
                        if (isQualified(player, items.get(j), false)) {
                            if (!items.get(j).isEmpty() && (pickUpAll || player.inventory.getFirstEmptyStack() >= 0)) {
                                dropCount++;
                                ItemStack extract = inventoryHandler.extractItem(i, items.get(i).getCount(), false);
                                ItemHandlerHelper.giveItemToPlayer(player, extract);
                                inventoryHandler.save();
                            }
                        }
                    }
                }
            }
            if (dropCount == 0) {
                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.open_message_empty").setStyle(new Style().setColor(TextFormatting.RED)));
            } else {
                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.open_message", String.valueOf(dropCount)).setStyle(new Style().setColor(TextFormatting.GREEN)));
            }
            player.sendContainerToPlayer(player.inventoryContainer);
            ItemPouchOfUnknown.save(pouch);
            ItemPouchOfUnknown.save(pouch);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return new IRarity() {
            @Override
            public TextFormatting getColor() {
                return TextFormatting.YELLOW;
            }

            @Override
            public String getName() {
                return stack.getDisplayName();
            }
        };
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new PouchCapability(stack);
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            return NBTSerializer.serialize(getItemHandler(stack));
        } else {
            return stack.getTagCompound();
        }
    }

    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        IItemHandler handler = getItemHandler(stack);
        if (handler instanceof InventoryHandler) {
            InventoryHandler inventoryHandler = (InventoryHandler) handler;
            inventoryHandler.deserializeNBT(nbt);
        }
    }

    public static class NBTSerializer {
        public static NBTTagCompound serialize(List<ItemStack> stacks) {
            NBTTagList nbtTagList = new NBTTagList();
            for (int i = 0; i < stacks.size(); i++) {
                if (!stacks.get(i).isEmpty()) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    stacks.get(i).writeToNBT(itemTag);
                    nbtTagList.appendTag(itemTag);
                }
            }
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("Inventory", nbtTagList);
            return nbt;
        }

        public static NBTTagCompound serialize(IItemHandler handler) {
            if (!(handler instanceof InventoryHandler)) {
                return null;
            }
            return ((ItemStackHandler) handler).serializeNBT();
        }

        public static NonNullList<ItemStack> deserialize(NBTTagCompound nbt, int maxSize) {
            NonNullList<ItemStack> stacks = NonNullList.withSize(maxSize, ItemStack.EMPTY);
            NBTTagList tagList = nbt.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
                if (i < maxSize) {
                    stacks.set(i, new ItemStack(itemTags));
                }
            }
            return stacks;
        }
    }

    public static class InventoryHandler extends ItemStackHandler {
        boolean isLoaded = false;
        private boolean dirty = false;
        private ItemStack stack;

        public InventoryHandler(ItemStack stack) {
            this.setSize(PouchConfig.pouchCapacity);
            this.stack = stack;
        }

        public NonNullList<ItemStack> getStacks() {
            return stacks;
        }

        public int getSize() {
            return this.stacks.size();
        }

        public ItemStack insert(@Nonnull ItemStack stack) {
            ItemStack remnant = stack;
            for (int i = 0; i < getSize(); i++) {
                remnant = insertItem(i, remnant, false);
                if (remnant.isEmpty()) {
                    break;
                }
            }
            dirty = true;
            return remnant;
        }

        public ItemStack extractLast() {
            for (int i = getSize() - 1; i >= 0; i--) {
                if (!stacks.get(i).isEmpty()) {
                    return extractItem(i, stacks.get(i).getCount(), false);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return NBTSerializer.serialize(stacks);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            stacks = NBTSerializer.deserialize(nbt, getSize());
            onLoad();
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            dirty = true;
        }

        public void save() {
            if(stack == null) {
                return;
            }
            if (dirty) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                }
                nbt.removeTag("Inventory");
                nbt.merge(serializeNBT());
                stack.setTagCompound(nbt);
            }
        }

        public void load() {
            if (isLoaded) {
                return;
            }
            forceLoad();
        }

        public void forceLoad() {
            if(stack == null) {
                return;
            }
            if (!stack.hasTagCompound()) {
                load(new NBTTagCompound());
            }
            load(stack.getTagCompound());
        }

        public void load(NBTTagCompound nbt) {
            deserializeNBT(nbt);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            dirty = true;
            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            dirty = true;
            return super.extractItem(slot, amount, simulate);
        }

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }
    }

    public static class PouchCapability implements ICapabilitySerializable<NBTBase> {

        ItemStack stack;

        IItemHandler inventoryHandler = new InventoryHandler(stack);

        public PouchCapability(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventoryHandler);
            }
            return null;
        }

        @Override
        public NBTBase serializeNBT() {
            if (!(inventoryHandler instanceof InventoryHandler)) {
                return null;
            } else {
                return ((InventoryHandler) inventoryHandler).serializeNBT();
            }
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            if (!(inventoryHandler instanceof InventoryHandler)) {
                return;
            }
            ((InventoryHandler) inventoryHandler).deserializeNBT((NBTTagCompound) nbt);
        }
    }
}
