package xyz.tcreopargh.pouchofunknown;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.itemstages.ItemStages;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.server.command.TextComponentHelper;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class PouchOfUnknownEvents {

    public static final int MAX_SLOT_NUMBER = 40;

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
    public static void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote || event.getEntityLiving() == null || !(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        boolean hasPouch = false;
        boolean isFullFlag = false;
        ItemStack pouch = ItemStack.EMPTY;
        for (int i = 0; i < MAX_SLOT_NUMBER; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (isValidPouch(stack)) {
                hasPouch = true;
                pouch = stack;
                isFullFlag = false;
                break;
            } else if (stack.getItem().getRegistryName() != null && stack.getItem().getRegistryName().toString().equals(ItemPouchOfUnknown.registryName)) {
                isFullFlag = true;
            }
        }
        for (int slot = 0; slot <= MAX_SLOT_NUMBER; slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot).copy();
            if (!isQualified(player, stack, true)) {
                if (hasPouch && isQualified(player, pouch, true)) {
                    NBTTagCompound nbt = stack.serializeNBT();
                    NBTTagList list = pouch.getTagCompound() != null ? pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND) : new NBTTagList();
                    list.appendTag(nbt);
                    NBTTagCompound newTag = pouch.getTagCompound() != null ? pouch.getTagCompound() : new NBTTagCompound();
                    newTag.setTag("Inventory", list);
                    pouch.setTagCompound(newTag);
                    if (PouchConfig.showMessage) {
                        String displayString = getDisplayName(stack, player);
                        player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.pickup_message", displayString).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    }
                } else {
                    if (!PouchConfig.destroyItemWithoutPouch) {
                        player.dropItem(stack, true);
                    }
                    if (PouchConfig.showMessage && !stack.isEmpty()) {
                        String displayString = getDisplayName(stack, player);
                        if (isFullFlag) {
                            if (!PouchConfig.destroyItemWithoutPouch) {
                                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.full_message", displayString, "\n").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                            } else {
                                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.full_destroy_message", displayString, "\n").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                            }
                        } else {
                            if (!PouchConfig.destroyItemWithoutPouch) {
                                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.drop_message", displayString, "\n").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                            } else {
                                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.destroy_message", displayString, "\n").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                            }
                        }
                    }
                }
                player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                if (player instanceof EntityPlayerMP) {
                    EntityPlayerMP playerMP = (EntityPlayerMP) player;
                    playerMP.sendContainerToPlayer(player.inventoryContainer);
                }
                player.inventoryContainer.detectAndSendChanges();
            }
        }
    }


    @SubscribeEvent
    public static void onPouchRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP && event.getEntityPlayer() != null && !event.getWorld().isRemote && Objects.equals(Objects.requireNonNull(event.getItemStack().getItem().getRegistryName()).toString(), ItemPouchOfUnknown.registryName)) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
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
                    dropCount++;
                    ItemHandlerHelper.giveItemToPlayer(player, newStack);
                    inventoryNBT.removeTag(i);
                    i--;
                }
            }
            NBTTagCompound newTag = pouch.getTagCompound() != null ? pouch.getTagCompound() : new NBTTagCompound();
            newTag.setTag("Inventory", inventoryNBT);
            pouch.setTagCompound(newTag);
            if (dropCount == 0) {
                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.open_message_empty").setStyle(new Style().setColor(TextFormatting.RED)));
            } else {
                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.open_message", String.valueOf(dropCount)).setStyle(new Style().setColor(TextFormatting.GREEN)));
            }
            player.sendContainerToPlayer(player.inventoryContainer);
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
        if (ItemStages.getStage(stack) == null || stack.isEmpty()) {
            return true;
        }
        if (player.isCreative() && ignoreCreative) {
            return true;
        }
        if (PouchConfig.ignoreNBT) {
            ItemStack baseStack = new ItemStack(new Item().setRegistryName(Objects.requireNonNull(stack.getItem().getRegistryName())), stack.getCount(), stack.getItemDamage());
            return GameStageHelper.hasStage(player, ItemStages.getStage(baseStack));
        } else {
            return GameStageHelper.hasStage(player, ItemStages.getStage(stack));
        }
    }

    public static String getDisplayName(ItemStack stack, ICommandSender sender) {
        String unfamiliarName;
        if (PouchConfig.showItemName) {
            unfamiliarName = stack.getDisplayName();
        } else {
            try {
                unfamiliarName = (String) method.get().invoke(null, stack);
            } catch (Exception e) {
                unfamiliarName = TextComponentHelper.createComponentTranslation(sender, "pouchofunknown.unfamiliar.default.name").getFormattedText();
            }
        }
        return TextFormatting.GOLD + unfamiliarName + " " + TextFormatting.YELLOW + "*" + " " + TextFormatting.AQUA + stack.getCount() + TextFormatting.YELLOW;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (eventArgs.getModID().equals(PouchOfUnknownMod.MODID)) {
            PouchOfUnknownMod.logger.info("Pouch Of Unknown config changed!");
            ConfigManager.sync(PouchOfUnknownMod.MODID, Config.Type.INSTANCE);
        }
    }
}

