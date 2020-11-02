package xyz.tcreopargh.pouchofunknown;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.itemstages.ItemStages;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class PouchOfUnknownEvents {

    public static final int SLOT_COUNT = 40;

    private static final AtomicReference<Method> method = new AtomicReference<>();

    static {
        try {
            method.set(ItemStages.class.getDeclaredMethod("getUnfamiliarName", ItemStack.class));
            method.get().setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player.getEntityWorld().isRemote || event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        InventoryPlayer inventory = player.inventory;
        boolean hasPouch = false;
        boolean isFullFlag = false;
        ItemStack pouch = ItemStack.EMPTY;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (isValidPouch(stack)) {
                hasPouch = true;
                pouch = stack;
                isFullFlag = false;
                break;
            } else if (stack.getItem().getRegistryName() != null && stack.getItem().getRegistryName().toString().equals(ItemPouchOfUnknown.registryName)) {
                isFullFlag = true;
            }
        }
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != ItemStack.EMPTY && ItemStages.getStage(stack) != null) {
                if (!isQualified(player, stack, true)) {
                    if (hasPouch && isQualified(player, pouch, true)) {
                        NBTTagCompound nbt = stack.serializeNBT();
                        NBTTagList list = pouch.getTagCompound() != null ? pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND) : new NBTTagList();
                        list.appendTag(nbt);
                        NBTTagCompound newTag = pouch.getTagCompound() != null ? pouch.getTagCompound() : new NBTTagCompound();
                        newTag.setTag("Inventory", list);
                        pouch.setTagCompound(newTag);
                        if (PouchConfig.showMessage) {
                            String displayString = getDisplayName(stack);
                            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + I18n.format("pouchofunknown.pickup_message", displayString)));
                        }
                    } else {
                        player.dropItem(stack, true);
                        if (PouchConfig.showMessage) {
                            String displayString = getDisplayName(stack);
                            if (isFullFlag) {
                                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + I18n.format("pouchofunknown.full_message", displayString, "\n")));
                            } else {
                                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + I18n.format("pouchofunknown.drop_message", displayString, "\n")));
                            }
                        }
                    }
                    inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onPouchRightClick(PlayerInteractEvent.RightClickItem event) {

        if (!event.getWorld().isRemote && Objects.equals(Objects.requireNonNull(event.getItemStack().getItem().getRegistryName()).toString(), ItemPouchOfUnknown.registryName)) {
            EntityPlayer player = event.getEntityPlayer();
            ItemStack pouch = event.getItemStack();
            int dropCount = 0;
            NBTTagList inventoryNBT = pouch.getTagCompound() != null ? pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND) : null;
            if (inventoryNBT == null) {
                inventoryNBT = new NBTTagList();
            }
            for (int i = 0; i < inventoryNBT.tagCount(); i++) {
                NBTTagCompound itemNBT = inventoryNBT.getCompoundTagAt(i);
                ItemStack newStack = new ItemStack(itemNBT);
                if (isQualified(player, newStack, false)) {
                    player.dropItem(newStack, true);
                    inventoryNBT.removeTag(i);
                    i--;
                    dropCount++;
                }
            }
            NBTTagCompound newTag = pouch.getTagCompound() != null ? pouch.getTagCompound() : new NBTTagCompound();
            newTag.setTag("Inventory", inventoryNBT);
            pouch.setTagCompound(newTag);
            if (dropCount == 0) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + I18n.format("pouchofunknown.open_message_empty")));
            } else {
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + I18n.format("pouchofunknown.open_message", String.valueOf(dropCount))));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onPouchTooltip(ItemTooltipEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player != null && Objects.equals(Objects.requireNonNull(event.getItemStack().getItem().getRegistryName()).toString(), ItemPouchOfUnknown.registryName)) {
            ItemStack pouch = event.getItemStack();
            int canPickupItemCount = 0;
            int totalItemCount;
            NBTTagList inventoryNBT = pouch.getTagCompound() != null ? pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND) : new NBTTagList();
            for (int i = 0; i < inventoryNBT.tagCount(); i++) {
                NBTTagCompound itemNBT = inventoryNBT.getCompoundTagAt(i);
                ItemStack newStack = new ItemStack(itemNBT);
                if (isQualified(player, newStack, false)) {
                    canPickupItemCount++;
                }
            }
            totalItemCount = inventoryNBT.tagCount();
            String pickupItemDisplay = TextFormatting.GREEN + I18n.format("pouchofunknown.tooltip.pickupable", String.valueOf(canPickupItemCount));
            String totalItemDisplay = TextFormatting.YELLOW + I18n.format("pouchofunknown.tooltip.total", String.valueOf(totalItemCount));
            event.getToolTip().add(pickupItemDisplay);
            event.getToolTip().add(totalItemDisplay);
            event.getToolTip().add(TextFormatting.GRAY + I18n.format("pouchofunknown.tooltip.maxcapacity", String.valueOf(PouchConfig.pouchCapacity)));
        }
    }

    public static boolean isValidPouch(ItemStack pouch) {
        if (Objects.equals(Objects.requireNonNull(pouch.getItem().getRegistryName()).toString(), ItemPouchOfUnknown.registryName)) {
            if (pouch.getTagCompound() == null) {
                return true;
            } else {
                return pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND).tagCount() < PouchConfig.pouchCapacity;
            }
        }
        return false;
    }

    public static boolean isQualified(EntityPlayer player, ItemStack stack, boolean ignoreCreative) {
        if (ItemStages.getStage(stack) == null || stack.getItem().getRegistryName() == null) {
            return true;
        }
        if (player.isCreative() && ignoreCreative) {
            return true;
        }
        if (PouchConfig.ignoreNBT) {
            ItemStack baseStack = new ItemStack(new Item().setRegistryName(stack.getItem().getRegistryName()), stack.getCount(), stack.getItemDamage());
            return GameStageHelper.hasStage(player, ItemStages.getStage(baseStack));
        } else {
            return GameStageHelper.hasStage(player, ItemStages.getStage(stack));
        }
    }

    public static String getDisplayName(ItemStack stack) {
        String unfamiliarName;
        if (PouchConfig.showItemName) {
            unfamiliarName = stack.getDisplayName();
        } else {
            try {
                unfamiliarName = (String) method.get().invoke(null, stack);
            } catch (Exception e) {
                unfamiliarName = I18n.format("pouchofunknown.unfamiliar.default.name");
            }
        }
        return TextFormatting.GOLD + unfamiliarName + " " + TextFormatting.YELLOW + "*" + " " + TextFormatting.AQUA + stack.getCount() + TextFormatting.YELLOW;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (eventArgs.getModID().equals(PouchOfUnknownMod.MODID)) {
            System.out.println("Pouch Of Unknown config changed!");
            ConfigManager.sync(PouchOfUnknownMod.MODID, Config.Type.INSTANCE);
        }
    }
}

