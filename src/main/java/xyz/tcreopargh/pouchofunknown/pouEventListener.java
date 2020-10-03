package xyz.tcreopargh.pouchofunknown;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = PouchOfUnknownMod.MODID)
public final class pouEventListener {
    @SubscribeEvent
    public void onEventFired(TickEvent.PlayerTickEvent event) {
       
    }
}

