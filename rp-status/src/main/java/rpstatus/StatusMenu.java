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
			case
