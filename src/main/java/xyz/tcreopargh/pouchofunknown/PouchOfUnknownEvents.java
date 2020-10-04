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
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class PouchOfUnknownEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() != null && event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().getEntityWorld().isRemote) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();

            InventoryPlayer inventory = player.inventory;
            boolean hasPouch = false;
            boolean isFullFlag = false;
            ItemStack pouch = ItemStack.EMPTY;
            for (int i = 0; i < inventory.mainInventory.size(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (isValidPouch(stack) && !hasPouch) {
                    hasPouch = true;
                    pouch = stack;
                } else if (stack.getItem().getRegistryName() != null && stack.getItem().getRegistryName().toString().equals(ItemPouchOfUnknown.registryName)) {
                    isFullFlag = true;
                }
            }

            for (int i = 0; i < inventory.mainInventory.size(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack != ItemStack.EMPTY) {
                    String displayString = TextFormatting.GOLD + stack.getDisplayName() + " " + TextFormatting.YELLOW + "*" + " " + TextFormatting.AQUA + stack.getCount() + TextFormatting.YELLOW;

                    if (!isQualified(player, stack, true)) {
                        if (hasPouch && isQualified(player, pouch, true)) {
                            NBTTagCompound nbt = stack.serializeNBT();
                            NBTTagList list = pouch.getTagCompound() != null ? pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND) : new NBTTagList();
                            list.appendTag(nbt);
                            NBTTagCompound newTag = pouch.getTagCompound() != null ? pouch.getTagCompound() : new NBTTagCompound();
                            newTag.setTag("Inventory", list);
                            pouch.setTagCompound(newTag);
                            if(PouchConfig.showMessage) {
                                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + I18n.format("pouchofunknown.pickup_message", displayString)));
                            }
                        } else {
                            player.dropItem(stack, true);
                            if (PouchConfig.showMessage) {
                                if (isFullFlag) {
                                    player.sendMessage(new TextComponentString(TextFormatting.YELLOW + I18n.format("pouchofunknown.full_message", displayString)));
                                } else {
                                    player.sendMessage(new TextComponentString(TextFormatting.YELLOW + I18n.format("pouchofunknown.drop_message", displayString)));
                                }
                            }
                        }
                        inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                    }
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
            } else return pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND).tagCount() < PouchConfig.pouchCapacity;
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

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if(eventArgs.getModID().equals(PouchOfUnknownMod.MODID)){
            System.out.println("Pouch Of Unknown config changed!");
            ConfigManager.sync(PouchOfUnknownMod.MODID, Config.Type.INSTANCE);
        }
    }
}

