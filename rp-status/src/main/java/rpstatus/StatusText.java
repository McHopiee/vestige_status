package rpstatus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

/** Builds the colored squares that go next to a player's name. */
public final class StatusText {
	private StatusText() {}

	public static MutableComponent square(Status status) {
		return Component.literal(StatusConfig.symbol)
				.withStyle(style -> style.withColor(TextColor.fromRgb(StatusConfig.color(status))));
	}

	/** All of a player's squares, e.g. "■■ " - or nothing at all if they have no status on. */
	public static MutableComponent squares(UUID player) {
		MutableComponent out = Component.literal("");
		Set<Status> set = StatusStore.get(player);
		if (set.isEmpty()) return out;
		for (Status s : Status.values()) {
			if (set.contains(s)) out.append(square(s));
		}
		// End on plain white so the player's name after it doesn't pick up the last square's color
		out.append(Component.literal(StatusConfig.spaceAfterSquares ? " " : "").withStyle(ChatFormatting.WHITE));
		return out;
	}

	/** Plain names, e.g. "In Character, Streaming". */
	public static String names(UUID player) {
		StringJoiner joiner = new StringJoiner(", ");
		Set<Status> set = StatusStore.get(player);
		for (Status s : Status.values()) {
			if (set.contains(s)) joiner.add(s.displayName);
		}
		return joiner.toString();
	}
}
