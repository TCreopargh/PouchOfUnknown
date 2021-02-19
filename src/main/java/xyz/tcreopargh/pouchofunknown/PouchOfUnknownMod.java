package xyz.tcreopargh.pouchofunknown;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = PouchOfUnknownMod.MODID,
        name = PouchOfUnknownMod.MODNAME,
        version = PouchOfUnknownMod.VERSION,
        dependencies = "required-after:itemstages"
)
public class PouchOfUnknownMod {

    public static final String MODID = "pouchofunknown";
    public static final String MODNAME = "Pouch Of Unknown";
    public static final String VERSION = "1.8";

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MODID)
    public static PouchOfUnknownMod INSTANCE;

    public static final String CLIENT_PROXY = "xyz.tcreopargh.pouchofunknown.ClientProxy";
    public static final String COMMON_PROXY = "xyz.tcreopargh.pouchofunknown.CommonProxy";

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    public static Logger logger;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
