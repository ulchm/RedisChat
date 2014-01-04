package com.norcode.bukkit.redischat.chat;

import net.minecraft.server.v1_7_R1.ChatClickable;
import net.minecraft.server.v1_7_R1.ChatComponentText;
import net.minecraft.server.v1_7_R1.ChatHoverable;
import net.minecraft.server.v1_7_R1.EnumChatFormat;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;


public class Text extends ChatComponentText {

	public Text append(String text) {
		return (Text) a(text);
	}

	public Text append(IChatBaseComponent node) {
		return (Text) a(node);
	}

	public Text append(IChatBaseComponent... nodes) {
		for (IChatBaseComponent node : nodes) {
			a(node);
		}
		return this;
	}

	public Text appendItem(ItemStack stack) {
		net.minecraft.server.v1_7_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound tag = new NBTTagCompound();
		nms.save(tag);
		return append(new Text(nms.getName()).setColor(ChatColor.getByChar(nms.w().e.getChar())).setHover(HoverAction.SHOW_ITEM, new ChatComponentText(tag.toString())));
	}

	public net.minecraft.server.v1_7_R1.ChatModifier getChatModifier() {
		return b();
	}

	public Text setBold(boolean bold) {
		getChatModifier().setBold(bold);
		return this;
	}

	public Text setItalic(boolean italic) {
		getChatModifier().setItalic(italic);
		return this;
	}

	public Text setUnderline(boolean underline) {
		getChatModifier().setUnderline(underline);
		return this;
	}

	public Text setRandom(boolean random) {
		getChatModifier().setRandom(random);
		return this;
	}

	public Text setStrikethrough(boolean strikethrough) {
		getChatModifier().setStrikethrough(strikethrough);
		return this;
	}

	public Text setColor(ChatColor color) {
		getChatModifier().setColor(EnumChatFormat.valueOf(color.name()));
		return this;
	}

	public Text setClick(ClickAction action, String value) {
		getChatModifier().a(new ChatClickable(action.getNMS(), value));
		return this;
	}

	public Text setHover(HoverAction action, IChatBaseComponent value) {
		getChatModifier().a(new ChatHoverable(action.getNMS(), value));
		return this;
	}

	public Text setHoverText(String text) {
		return setHover(HoverAction.SHOW_TEXT, new Text(text));
	}

	public Text(String s) {
		super(s);
	}

	@Override
	public IChatBaseComponent f() {
		return h();
	}
}
