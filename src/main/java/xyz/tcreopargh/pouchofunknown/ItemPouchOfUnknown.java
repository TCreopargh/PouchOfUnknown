package xyz.tcreopargh.pouchofunknown;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class ItemPouchOfUnknown {
    public static Item pouchOfUnknownItem;

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                pouchOfUnknownItem = new Item()
                        .setTranslationKey("pouchofunknown.pouch_of_unknown")
                        .setRegistryName(PouchOfUnknownMod.MODID, "pouch")
                        .setCreativeTab(CreativeTabs.MISC)
                        .setMaxStackSize(1)
        );
    }

    @SubscribeEvent
    public static void onModelReg(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(ItemPouchOfUnknown.pouchOfUnknownItem, 0, new ModelResourceLocation(ItemPouchOfUnknown.pouchOfUnknownItem.getRegistryName(), "inventory"));
    }
    @SubscribeEvent
    public static void registerRenders(ModelRegistryEvent event) {
        registerRender(pouchOfUnknownItem);
    }
    private static void registerRender(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
