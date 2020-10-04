package xyz.tcreopargh.pouchofunknown;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(
        modid = PouchOfUnknownMod.MODID
)
public class PouchConfig {
    @Config.LangKey("pouchofunknown.config.show_message")
    @Config.Comment("Whether to show a message when a player drops an unknown item or puts it into the pouch. [default: true]")
    public static boolean showMessage = true;

    @Config.LangKey("pouchofunknown.config.ignore_nbt")
    @Config.Comment("Whether to ignore the NBT tag of any item and stage an item with any NBT tag to the stage of it's base item. \n" +
            "§cWARNING: DON'T SET THIS TO TRUE IF YOUR SCRIPT STAGED SOME ITEMS WITH SPECIFIC NBT TAGS! [default: false]")
    public static boolean ignoreNBT = false;

    @Config.RangeInt(min = 1, max = 4096)
    @Config.LangKey("pouchofunknown.config.pouch_capacity")
    @Config.Comment("The maximum stacks of items that a pouch can hold. Range: 1 - 4096 [default: 540]")
    public static int pouchCapacity = 540;

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if(eventArgs.getModID().equals(PouchOfUnknownMod.MODID)){
            System.out.println("Pouch Of Unknown config changed!");
            ConfigManager.sync(PouchOfUnknownMod.MODID, Config.Type.INSTANCE);
        }
    }
}
