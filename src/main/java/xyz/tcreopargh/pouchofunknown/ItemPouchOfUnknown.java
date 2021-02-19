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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.server.command.TextComponentHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static xyz.tcreopargh.pouchofunknown.PouchOfUnknownEvents.isQualified;


@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class ItemPouchOfUnknown extends Item implements IBauble {

    public static final ItemPouchOfUnknown itemPouchOfUnknown = new ItemPouchOfUnknown();
    public static final String registryName = PouchOfUnknownMod.MODID + ":" + "pouch";

    public static final String INVENTORY_TAG_NAME = "Inventory";

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
        ModelLoader.setCustomModelResourceLocation(itemPouchOfUnknown, 0, new ModelResourceLocation(Objects.requireNonNull(itemPouchOfUnknown.getRegistryName()), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerRenders(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemPouchOfUnknown, 0, new ModelResourceLocation(Objects.requireNonNull(itemPouchOfUnknown.getRegistryName()), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            int canPickupItemCount = 0;
            int totalItemCount = 0;
            List<ItemStack> items = Util.getItems(stack);
            for (ItemStack newStack : items) {
                if (!newStack.isEmpty() && isQualified(player, newStack, false)) {
                    canPickupItemCount++;
                }
                if (!newStack.isEmpty()) {
                    totalItemCount++;
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
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.MISC;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn instanceof EntityPlayerMP && !worldIn.isRemote) {
            EntityPlayerMP player = (EntityPlayerMP) playerIn;
            ItemStack pouch = playerIn.getHeldItem(handIn);
            if (!(pouch.getItem() instanceof ItemPouchOfUnknown)) {
                return ActionResult.newResult(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
            }
            boolean pickUpAll = player.isSneaking();
            int dropCount = 0;
            List<ItemStack> items = Util.getItems(pouch);
            boolean isInventoryFull = false;
            for (int i = items.size() - 1; i >= 0; i--) {
                if (isQualified(player, items.get(i), false)) {
                    if (!items.get(i).isEmpty() && (pickUpAll || player.inventory.getFirstEmptyStack() >= 0)) {
                        dropCount++;
                        ItemStack extract = items.get(i);
                        items.set(i, ItemStack.EMPTY);
                        ItemHandlerHelper.giveItemToPlayer(player, extract);
                    }
                    if (!pickUpAll && player.inventory.getFirstEmptyStack() < 0) {
                        isInventoryFull = true;
                    }
                }
            }
            Util.setItems(pouch, items);
            if (dropCount == 0) {
                if (isInventoryFull) {
                    player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.open_message_full_inventory").setStyle(new Style().setColor(TextFormatting.RED)));
                } else {
                    player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.open_message_empty").setStyle(new Style().setColor(TextFormatting.RED)));
                }
            } else {
                player.sendMessage(TextComponentHelper.createComponentTranslation(player, "pouchofunknown.open_message", String.valueOf(dropCount)).setStyle(new Style().setColor(TextFormatting.GREEN)));
            }
            player.sendContainerToPlayer(player.inventoryContainer);
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
}
