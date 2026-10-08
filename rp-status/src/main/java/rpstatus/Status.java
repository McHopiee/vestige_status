package rpstatus;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Every status a player can pick. The order here is the order the squares
 * appear in the tab list.
 */
public enum Status {
	IN_CHARACTER("in_character", "In Character", Group.CHARACTER, 0x55FF55,
			Items.LIME_CONCRETE, Items.LIME_STAINED_GLASS,
			"You're playing your character."),
	OUT_OF_CHARACTER("out_of_character", "Out of Character", Group.CHARACTER, 0xFFFF55,
			Items.YELLOW_CONCRETE, Items.YELLOW_STAINED_GLASS,
			"You're talking as yourself, not your character."),
	DO_NOT_DISTURB("do_not_disturb", "Do Not Disturb", Group.NONE, 0xFF5555,
			Items.RED_CONCRETE, Items.RED_STAINED_GLASS,
			"Please don't message or approach you right now."),
	OPEN_TO_INTERACTIONS("open_to_interactions", "Open to Interactions", Group.INTERACTIONS, 0x55FFFF,
			Items.LIGHT_BLUE_CONCRETE, Items.LIGHT_BLUE_STAINED_GLASS,
			"Others are welcome to start a scene with you."),
	CLOSED_TO_INTERACTIONS("closed_to_interactions", "Closed to Interactions", Group.INTERACTIONS, 0xFFAA00,
			Items.ORANGE_CONCRETE, Items.ORANGE_STAINED_GLASS,
			"You'd rather not be pulled into new scenes."),
	RECORDING("recording", "Recording", Group.NONE, 0xFF55FF,
			Items.PINK_CONCRETE, Items.PINK_STAINED_GLASS,
			"You're recording, so anything said may be captured."),
	STREAMING("streaming", "Streaming", Group.NONE, 0xAA00AA,
			Items.PURPLE_CONCRETE, Items.PURPLE_STAINED_GLASS,
			"You're live, so anything said may be on stream.");

	/** Statuses in the same group (other than NONE) can't be on at the same time. */
	public enum Group { NONE, CHARACTER, INTERACTIONS }

	public final String id;
	public final String displayName;
	public final Group group;
	public final int defaultColor;
	public final Item onItem;
	public final Item offItem;
	public final String description;

	Status(String id, String displayName, Group group, int defaultColor, Item onItem, Item offItem, String description) {
		this.id = id;
		this.displayName = displayName;
		this.group = group;
		this.defaultColor = defaultColor;
		this.onItem = onItem;
		this.offItem = offItem;
		this.description = description;
	}

	public static Status byId(String id) {
		for (Status s : values()) {
			if (s.id.equals(id)) return s;
		}
		return null;
	}
}
