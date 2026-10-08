package rpstatus;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The chest menu opened by /status.
 *
 *   Row 1:            [preview]
 *   Row 2:  [IC][OOC]   [DND]   [Open][Closed]
 *   Row 3:      [Recording] [Streaming]      [Clear]
 */
public class StatusMenu extends SimpleGui {
	private static final int PREVIEW_SLOT = 4;
	private static final int CLEAR_SLOT = 26;

	public StatusMenu(ServerPlayer player) {
		super(MenuType.GENERIC_9x3, player, false);
		setTitle(Component.literal("Set Your Status"));

		GuiElementBuilder filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).hideTooltip();
		for (int i = 0; i < getSize(); i++) {
			setSlot(i, filler);
		}
		redraw();
	}

	private static int slotFor(Status status) {
		return switch (status) {
			case IN_CHARACTER -> 10;
			case OUT_OF_CHARACTER -> 11;
			case DO_NOT_DISTURB -> 13;
			case OPEN_TO_INTERACTIONS -> 15;
			case CLOSED_TO_INTERACTIONS -> 16;
			case RECORDING -> 21;
			case STREAMING -> 23;
		};
	}

	private static boolean isNormalClick(ClickType type) {
		return type == ClickType.MOUSE_LEFT || type == ClickType.MOUSE_RIGHT
				|| type == ClickType.MOUSE_LEFT_SHIFT || type == ClickType.MOUSE_RIGHT_SHIFT;
	}

	private void redraw() {
		for (Status status : Status.values()) {
			setSlot(slotFor(status), statusButton(status));
		}
		setSlot(PREVIEW_SLOT, previewItem());
		setSlot(CLEAR_SLOT, clearButton());
	}

	private GuiElementBuilder statusButton(Status status) {
		boolean on = StatusStore.has(player.getUUID(), status);

		MutableComponent label = Component.literal(" " + status.displayName);
		if (on) label.withStyle(ChatFormatting.BOLD);
		Component name = Component.literal("").append(StatusText.square(status)).append(label);

		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal(status.description).withStyle(ChatFormatting.GRAY));
		lore.add(Component.empty());
		if (on) {
			lore.add(Component.literal("● Selected").withStyle(ChatFormatting.GREEN));
			lore.add(Component.literal("Click to turn off").withStyle(ChatFormatting.DARK_GRAY));
		} else {
			lore.add(Component.literal("○ Not selected").withStyle(ChatFormatting.GRAY));
			lore.add(Component.literal("Click to turn on").withStyle(ChatFormatting.DARK_GRAY));
		}
		String pairNote = switch (status.group) {
			case CHARACTER -> "Only one of In Character / Out of Character at a time.";
			case INTERACTIONS -> "Only one of Open / Closed to Interactions at a time.";
			case NONE -> null;
		};
		if (pairNote != null) {
			lore.add(Component.literal(pairNote).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		}

		GuiElementBuilder button = new GuiElementBuilder(on ? status.onItem : status.offItem)
				.setName(name)
				.setLore(lore)
				.hideDefaultTooltip()
				.setCallback((index, type, action, gui) -> {
					if (!isNormalClick(type)) return;
					StatusStore.toggle(player.getUUID(), status);
					redraw();
				});
		if (on) button.glow();
		return button;
	}

	private GuiElementBuilder previewItem() {
		Component name = Component.literal("")
				.append(StatusText.squares(player.getUUID()))
				.append(player.getName().copy().withStyle(ChatFormatting.WHITE));

		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal("This is how you look in the tab list.").withStyle(ChatFormatting.GRAY));
		String names = StatusText.names(player.getUUID());
		lore.add(Component.literal(names.isEmpty() ? "No status set" : names).withStyle(ChatFormatting.DARK_GRAY));

		return new GuiElementBuilder(Items.NAME_TAG)
				.setName(name)
				.setLore(lore)
				.hideDefaultTooltip();
	}

	private GuiElementBuilder clearButton() {
		return new GuiElementBuilder(Items.BARRIER)
				.setName(Component.literal("Clear all statuses").withStyle(ChatFormatting.RED))
				.setLore(List.of(Component.literal("Turns every status off.").withStyle(ChatFormatting.GRAY)))
				.hideDefaultTooltip()
				.setCallback((index, type, action, gui) -> {
					if (!isNormalClick(type)) return;
					StatusStore.clear(player.getUUID());
					redraw();
				});
	}
}
