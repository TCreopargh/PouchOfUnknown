package xyz.tcreopargh.pouchofunknown;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.itemstages.ItemStages;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.server.command.TextComponentHelper;

import java.lang.reflect.Method;
import java.util.Arrays;
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

    public static void detect(EntityPlayer player) {
        boolean hasPouch = false;
        ItemStack pouch = ItemStack.EMPTY;
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (isValidPouch(stack)) {
                hasPouch = true;
                pouch = stack;
                break;
            }
        }
        if (!hasPouch) {
            for (int i = 0; i < MAX_SLOT_NUMBER; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (isValidPouch(stack)) {
                    hasPouch = true;
                    pouch = stack;
                    break;
                }
            }
        }
        ItemPouchOfUnknown.load(pouch);

        for (int slot = 0; slot <= MAX_SLOT_NUMBER; slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot).copy();
            ItemStack remnant = stack;
            if (!isQualified(player, stack, true)) {
                if (hasPouch && isQualified(player, pouch, true)) {
                    if (Arrays.asList(PouchConfig.disabledStagesList).contains(ItemStages.getStage(stack))) {
                        if (PouchConfig.showMessage) {
                            player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.disabled_item_message").setStyle(new Style().setColor(TextFormatting.RED)));
                        }
                    } else {
                        remnant = ItemPouchOfUnknown.insertItem(pouch, remnant);
                        if (PouchConfig.showMessage) {
                            String displayString = getDisplayName(stack, player);
                            player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.pickup_message", displayString).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                        }
                    }
                    if (remnant != null && !remnant.isEmpty()) {
                        String displayString = getDisplayName(stack, player);
                        if (!PouchConfig.destroyItemWithoutPouch) {
                            player.dropItem(remnant, true);
                            player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.full_message", displayString, "\n").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                        } else {
                            player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.full_destroy_message", displayString, "\n").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                        }

                    }
                } else {
                    if (!PouchConfig.destroyItemWithoutPouch) {
                        player.dropItem(remnant, true);
                    }
                    if (PouchConfig.showMessage && !stack.isEmpty()) {
                        String displayString = getDisplayName(stack, player);
                        if (!remnant.isEmpty()) {
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
                ItemPouchOfUnknown.save(pouch);
                player.inventoryContainer.detectAndSendChanges();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.player instanceof EntityPlayerMP) {
            detect(event.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.player instanceof EntityPlayerMP && !event.crafting.isEmpty()) {
            detect(event.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.player instanceof EntityPlayerMP && !event.smelting.isEmpty()) {
            detect(event.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemPickedUp(PlayerEvent.ItemPickupEvent event) {
        if (event.player instanceof EntityPlayerMP && !event.getStack().isEmpty()) {
            detect(event.player);
        }
    }

    public static boolean isValidPouch(ItemStack pouch) {
        return Objects.equals(Objects.requireNonNull(pouch.getItem().getRegistryName()).toString(), ItemPouchOfUnknown.registryName);
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

