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
public final class ItemPouchOfUnknown extends Item {

    public static final Item itemPouchOfUnknown = new ItemPouchOfUnknown();
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

    @SubscribeEvent
    public static void onModelReg(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemPouchOfUnknown, 0, new ModelResourceLocation(itemPouchOfUnknown.getRegistryName(), "inventory"));
    }
    @SubscribeEvent
    public static void registerRenders(ModelRegistryEvent event) {
        registerRender(itemPouchOfUnknown);
    }
    private static void registerRender(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(itemPouchOfUnknown.getRegistryName(), "inventory"));
    }

}
