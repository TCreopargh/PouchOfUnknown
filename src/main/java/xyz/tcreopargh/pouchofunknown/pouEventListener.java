package xyz.tcreopargh.pouchofunknown;


import crafttweaker.api.item.IItemStack;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.GameStages;
import net.darkhax.itemstages.ItemStages;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class pouEventListener {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        InventoryPlayer inventory = event.player.inventory;
        boolean hasPouch = false;
        int pouchPos = -1;
        ItemStack pouch = ItemStack.EMPTY;
        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (stack.getItem().getRegistryName().toString().equals("pouchofunknown:pouch")) {
                hasPouch = true;
                pouchPos = i;
                pouch = stack;
            }
        }

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            String stage = ItemStages.getStage(stack);
            String displayString = "§6" + stack.getDisplayName() + " §e*§b " + stack.getCount() + "§r";
            if(stage != null) {
                if (!GameStageHelper.hasStage(event.player, stage)) {
                    if (hasPouch) {
                        NBTTagCompound nbt = stack.serializeNBT();
                        NBTTagList list = pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
                        list.appendTag(nbt);
                        NBTTagCompound newTag = pouch.getTagCompound();
                        NBTTagCompound inventoryTag = new NBTTagCompound();
                        inventoryTag.setTag("Inventory", list);
                        newTag.merge(inventoryTag);
                        pouch.setTagCompound(newTag);
                        event.player.sendMessage(new TextComponentString(I18n.format("pouchofunknown.pickup_message", displayString)));
                        inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                    } else {
                        event.player.dropItem(stack, true);
                        inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                        event.player.sendMessage(new TextComponentString(I18n.format("pouchofunknown.drop_message", displayString)));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPouchRightclick(PlayerInteractEvent.RightClickItem event) {
        if (Objects.equals(event.getItemStack().getItem().getRegistryName(), ItemPouchOfUnknown.itemPouchOfUnknown.getRegistryName())) {
            EntityPlayer player = event.getEntityPlayer();
            ItemStack pouch = event.getItemStack();
            int dropCount = 0;
            NBTTagList inventoryNBT = pouch.getTagCompound() != null ? pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND) : null;
            if(inventoryNBT == null) {
                inventoryNBT = new NBTTagList();
            }
            for (int i = 0; i < inventoryNBT.tagCount(); i++) {
                NBTTagCompound itemNBT = inventoryNBT.getCompoundTagAt(i);
                ItemStack newStack = new ItemStack(itemNBT);
                String stage = ItemStages.getStage(newStack);
                if(stage != null) {
                    if (GameStageHelper.hasStage(player, stage)) {
                        player.dropItem(newStack, true);
                        inventoryNBT.removeTag(i);
                        i--;
                        dropCount++;
                    }
                } else {
                    player.dropItem(newStack, true);
                    inventoryNBT.removeTag(i);
                    i--;
                    dropCount++;
                }
            }
            NBTTagCompound newTag = pouch.getTagCompound();
            NBTTagCompound inventoryTag = new NBTTagCompound();
            inventoryTag.setTag("Inventory", inventoryNBT);
            newTag.merge(inventoryTag);
            pouch.setTagCompound(newTag);
            if(dropCount == 0) {
                player.sendMessage(new TextComponentString("§c" + I18n.format("pouchofunknown.open_message_empty")));
            } else {
                player.sendMessage(new TextComponentString("§a" + I18n.format("pouchofunknown.open_message", String.valueOf(dropCount))));
            }
        }
    }

    @SubscribeEvent
    public void onPouchTooltip(ItemTooltipEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if(player != null && Objects.equals(event.getItemStack().getItem().getRegistryName(), ItemPouchOfUnknown.itemPouchOfUnknown.getRegistryName())) {
            ItemStack pouch = event.getItemStack();
            int canPickupItemCount = 0;
            int totalItemCount = 0;
            NBTTagList inventoryNBT = pouch.getTagCompound() != null ? pouch.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND) : new NBTTagList();
            for (int i = 0; i < inventoryNBT.tagCount(); i++) {
                NBTTagCompound itemNBT = inventoryNBT.getCompoundTagAt(i);
                ItemStack newStack = new ItemStack(itemNBT);
                String stage = ItemStages.getStage(newStack);
                if(stage != null) {
                    if (GameStageHelper.hasStage(player, stage)) {
                        player.dropItem(newStack, true);
                        inventoryNBT.removeTag(i);
                        i--;
                        canPickupItemCount++;
                    }
                } else {
                    player.dropItem(newStack, true);
                    inventoryNBT.removeTag(i);
                    i--;
                    canPickupItemCount++;
                }
            }
            totalItemCount = inventoryNBT.tagCount();
            String pickupItemDisplay = "§a" + I18n.format("pouchofunknown.tooltip.pickupable", String.valueOf(canPickupItemCount));
            String totalItemDisplay = "§e" + I18n.format("pouchofunknown.tooltip.total", String.valueOf(totalItemCount));
            event.getToolTip().add(pickupItemDisplay);
            event.getToolTip().add(totalItemDisplay);
        }
    }
}

