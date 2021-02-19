package xyz.tcreopargh.pouchofunknown;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Util {

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound();
        } else {
            NBTTagCompound newTag = new NBTTagCompound();
            stack.setTagCompound(newTag);
            return newTag;
        }
    }

    public static NBTBase getOrCreateSubtag(NBTTagCompound tagCompound, String key, NBTBase defaultTag) {
        if (tagCompound.hasKey(key)) {
            if (tagCompound.getTag(key).getId() == defaultTag.getId()) {
                return tagCompound.getTag(key);
            } else {
                tagCompound.setTag(key, defaultTag);
                return defaultTag;
            }
        } else {
            tagCompound.setTag(key, defaultTag);
            return defaultTag;
        }
    }

    public static ItemStack insertItem(ItemStack pouch, ItemStack stack) {
        NBTTagList tagList = (NBTTagList) getOrCreateSubtag(getOrCreateTag(pouch), ItemPouchOfUnknown.INVENTORY_TAG_NAME, new NBTTagList());
        ItemStack remnant = stack;

        for (int i = 0; i < getTagListSize(tagList); i++) {
            NBTBase itemTag = tagList.get(i);
            if (itemTag instanceof NBTTagCompound) {
                ItemStack tagStack = new ItemStack((NBTTagCompound) itemTag);
                StackResult result = stackItem(tagStack, stack);
                remnant = result.getRemnant();
                tagList.set(i, result.getResult().serializeNBT());
                if (remnant.isEmpty()) {
                    break;
                }
            }
        }
        ItemStack ret;
        if (getTagListSize(tagList) < PouchConfig.pouchCapacity && !remnant.isEmpty()) {
            tagList.appendTag(remnant.serializeNBT());
            ret = ItemStack.EMPTY;
        } else {
            ret = remnant;
        }
        Objects.requireNonNull(pouch.getTagCompound()).setTag(ItemPouchOfUnknown.INVENTORY_TAG_NAME, tagList);
        return ret;
    }

    public static ItemStack extractItem(ItemStack pouch) {
        NBTTagList tagList = (NBTTagList) getOrCreateSubtag(getOrCreateTag(pouch), ItemPouchOfUnknown.INVENTORY_TAG_NAME, new NBTTagList());
        int size = getTagListSize(tagList);
        if (size == 0) {
            return ItemStack.EMPTY;
        } else {
            NBTBase itemTag = tagList.get(size - 1);
            ItemStack ret = ItemStack.EMPTY;
            if (itemTag instanceof NBTTagCompound) {
                ret = new ItemStack((NBTTagCompound) tagList.get(size - 1).copy());
                tagList.removeTag(size - 1);
            }
            Objects.requireNonNull(pouch.getTagCompound()).setTag(ItemPouchOfUnknown.INVENTORY_TAG_NAME, tagList);
            return ret;
        }
    }

    public static List<ItemStack> getItems(ItemStack pouch) {
        NBTTagList tagList = (NBTTagList) getOrCreateSubtag(getOrCreateTag(pouch), ItemPouchOfUnknown.INVENTORY_TAG_NAME, new NBTTagList());
        List<ItemStack> stackList = new ArrayList<>();
        for (NBTBase tag : tagList) {
            if (tag instanceof NBTTagCompound) {
                stackList.add(new ItemStack((NBTTagCompound) tag));
            }
        }
        return stackList;
    }

    public static int getTagListSize(NBTTagList tagList) {
        int i = 0;
        for (NBTBase ignored : tagList) {
            i++;
        }
        return i;
    }

    public static void setItems(ItemStack pouch, List<ItemStack> items) {
        NBTTagCompound tag = getOrCreateTag(pouch);
        NBTTagList newList = new NBTTagList();
        for (ItemStack stack : items) {
            if(!stack.isEmpty()) {
                newList.appendTag(stack.serializeNBT());
            }
        }
        tag.setTag(ItemPouchOfUnknown.INVENTORY_TAG_NAME, newList);
        pouch.setTagCompound(tag);
    }

    public static StackResult stackItem(ItemStack existing, ItemStack another) {
        int limit = another.getMaxStackSize();
        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(another, existing)) {
                return new StackResult(existing, another);
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return new StackResult(existing, another);
        }
        boolean reachedLimit = another.getCount() > limit;
        if (existing.isEmpty()) {
            existing = reachedLimit ? ItemHandlerHelper.copyStackWithSize(another, limit) : another;
        } else {
            existing.grow(reachedLimit ? limit : another.getCount());
        }
        ItemStack result = existing;
        ItemStack remnant = reachedLimit ? ItemHandlerHelper.copyStackWithSize(another, another.getCount() - limit) : ItemStack.EMPTY;
        return new StackResult(result, remnant);
    }

    public static class StackResult {
        private final ItemStack result;
        private final ItemStack remnant;

        public StackResult(ItemStack result, ItemStack remnant) {
            this.result = result;
            this.remnant = remnant;
        }

        public ItemStack getResult() {
            return result;
        }

        public ItemStack getRemnant() {
            return remnant;
        }
    }
}
