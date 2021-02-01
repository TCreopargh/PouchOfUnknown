package xyz.tcreopargh.pouchofunknown;

import net.minecraftforge.common.config.Config;

@Config(
        modid = PouchOfUnknownMod.MODID
)
public class PouchConfig {
    @Config.LangKey("pouchofunknown.config.show_message")
    @Config.Comment("Whether to show a message when a player drops an unknown item or puts it into the pouch. [default: true]")
    public static boolean showMessage = true;

    @Config.LangKey("pouchofunknown.config.ignore_nbt")
    @Config.Ignore()
    @Config.Comment({
            "THIS OPTION IS DISABLED FOR NOW, CHANGING IT HAS NO EFFECT",
            "Whether to ignore the NBT tag of any item and stage an item with any NBT tag to the stage of it's base item." ,
            "WARNING: DON'T SET THIS TO TRUE IF YOUR SCRIPT STAGED SOME ITEMS WITH SPECIFIC NBT TAGS! [default: false]"})
    public static boolean ignoreNBT = false;

    @Config.RangeInt(min = 1, max = 4096)
    @Config.LangKey("pouchofunknown.config.pouch_capacity")
    @Config.Comment("The maximum stacks of items that a pouch can hold. [default: 540]")
    public static int pouchCapacity = 540;

    @Config.LangKey("pouchofunknown.config.show_item_name")
    @Config.Comment({
            "Whether to show item's actual display name when it's thown away or put into pouch.",
            "If set to false, the item's unfamiliar name will be shown instead. [default: false]"})
    public static boolean showItemName = false;

    @Config.LangKey("pouchofunknown.config.destroy_item_without_pouch")
    @Config.Comment({
            "Whether to destroy items immediately instead of throwing them if the player doesn't have a pouch. [default: false]"})
    public static boolean destroyItemWithoutPouch = false;

}
