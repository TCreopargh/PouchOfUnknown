package xyz.tcreopargh.pouchofunknown;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class PlayerInventoryListener implements IContainerListener {
    private final EntityPlayerMP player;

    private boolean lock = false;

    public PlayerInventoryListener(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
        if(lock) {
            return;
        }
        lock = true;
        PouchOfUnknownEvents.detect(player);
        lock = false;
    }

    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if(lock) {
            return;
        }
        if (!stack.isEmpty())
        {
            lock = true;
            PouchOfUnknownEvents.detect(player, slotInd);
            lock = false;
        }
    }

    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {

    }

    @Override
    public void sendAllWindowProperties(Container containerIn, IInventory inventory) {

    }
}
