package com.norcode.bukkit.redischat.chat;

import net.minecraft.server.v1_7_R1.ChatClickable;
import net.minecraft.server.v1_7_R1.ChatComponentText;
import net.minecraft.server.v1_7_R1.ChatHoverable;
import net.minecraft.server.v1_7_R1.ChatMessage;
import net.minecraft.server.v1_7_R1.EnumChatFormat;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Trans extends ChatMessage {

	public Trans(String s, Object... objects) {
		super(s, objects);
	}

	@Override
	public IChatBaseComponent f() {
		return h();
	}

	public Trans append(String text) {
		return (Trans) a(text);
	}

	public Trans append(IChatBaseComponent node) {
		return (Trans) a(node);
	}

	public Trans append(IChatBaseComponent... nodes) {
		for (IChatBaseComponent node : nodes) {
			a(node);
		}
		return this;
	}

	public Trans appendItem(ItemStack stack) {
		net.minecraft.server.v1_7_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound tag = new NBTTagCompound();
		nms.save(tag);
		return append(new Trans(nms.getName()).setColor(ChatColor.getByChar(nms.w().e.getChar())).setBold(true).setHover(HoverAction.SHOW_ITEM, new ChatComponentText(tag.toString())));
	}

	public net.minecraft.server.v1_7_R1.ChatModifier getChatModifier() {
		return b();
	}

	public Trans setBold(boolean bold) {
		getChatModifier().setBold(bold);
		return this;
	}

	public Trans setItalic(boolean italic) {
		getChatModifier().setItalic(italic);
		return this;
	}

	public Trans setUnderline(boolean underline) {
		getChatModifier().setUnderline(underline);
		return this;
	}

	public Trans setRandom(boolean random) {
		getChatModifier().setRandom(random);
		return this;
	}

	public Trans setStrikethrough(boolean strikethrough) {
		getChatModifier().setStrikethrough(strikethrough);
		return this;
	}

	public Trans setColor(ChatColor color) {
		getChatModifier().setColor(EnumChatFormat.valueOf(color.name()));
		return this;
	}

	public Trans setClick(ClickAction action, String value) {
		getChatModifier().a(new ChatClickable(action.getNMS(), value));
		return this;
	}

	public Trans setHover(HoverAction action, IChatBaseComponent value) {
		getChatModifier().a(new ChatHoverable(action.getNMS(), value));
		return this;
	}

	public Trans setHoverText(String text) {
		return setHover(HoverAction.SHOW_TEXT, new Text(text));
	}
}
