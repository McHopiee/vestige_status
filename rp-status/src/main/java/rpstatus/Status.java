package rpstatus;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Every status a player can pick. The order here is the order the squares
 * appear in the tab list.
 */
public enum Status {
	IN_CHARACTER("in_character", "In Character", Group.CHARACTER, 0x55FF55,
			Items.CONCRETE.lime(), Items.STAINED_GLASS.lime(),
			"You're playing your character."),
	OUT_OF_CHARACTER("out_of_character", "Out of Character", Group.CHARACTER, 0xFFFF55,
			Items.CONCRETE.yellow(), Items.STAINED_GLASS.yellow(),
			"You're talking as yourself, not your character."),
	DO_NOT_DISTURB("do_not_disturb", "Do Not Disturb", Group.NONE, 0xFF5555,
			Items.CONCRETE.red(), Items.STAINED_GLASS.red(),
			"Please don't message or approach you right now."),
	OPEN_TO_INTERACTIONS("open_to_interactions", "Open to Interactions", Group.INTERACTIONS, 0x55FFFF,
			Items.CONCRETE.lightBlue(), Items.STAINED_GLASS.lightBlue(),
			"Others are welcome to start a scene with you."),
	CLOSED_TO_INTERACTIONS("closed_to_interactions", "Closed to Interactions", Group.INTERACTIONS, 0xFFAA00,
			Items.CONCRETE.orange(), Items.STAINED_GLASS.orange(),
			"You'd rather not be pulled into new scenes."),
	RECORDING("recording", "Recording", Group.NONE, 0xFF55FF,
			Items.CONCRETE.pink(), Items.STAINED_GLASS.pink(),
			"You're recording, so anything said may be captured."),
	STREAMING("streaming", "Streaming", Group.NONE, 0xAA00AA,
			Items.CONCRETE.purple(), Items.STAINED_GLASS.purple(),
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
