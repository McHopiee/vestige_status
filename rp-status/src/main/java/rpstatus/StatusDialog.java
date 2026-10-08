package rpstatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The /status screen. Uses Minecraft's built-in dialog screen (a window with
 * real buttons), so players don't need to install anything.
 *
 * Each button runs "/status toggle <id>", which flips the status and shows the
 * screen again with the new state.
 */
public final class StatusDialog {
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

	/** Button order, two per row. */
	private static final List<Status> LAYOUT = List.of(
			Status.IN_CHARACTER, Status.OUT_OF_CHARACTER,
			Status.OPEN_TO_INTERACTIONS, Status.CLOSED_TO_INTERACTIONS,
			Status.DO_NOT_DISTURB, Status.RECORDING,
			Status.STREAMING
	);

	private StatusDialog() {}

	public static void show(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		String json = GSON.toJson(build(player));
		server.getCommands().performPrefixedCommand(
				server.createCommandSourceStack().withSuppressedOutput(),
				"dialog show " + player.getUUID() + " " + json);
	}

	private static JsonObject build(ServerPlayer player) {
		UUID id = player.getUUID();
		Set<Status> active = StatusStore.get(id);

		JsonObject dialog = new JsonObject();
		dialog.addProperty("type", "minecraft:multi_action");
		dialog.add("title", text("Set Your Status", "#FFFFFF", true));
		dialog.addProperty("can_close_with_escape", true);
		dialog.addProperty("pause", false);
		dialog.addProperty("after_action", "wait_for_response");
		dialog.addProperty("columns", 2);

		// Text above the buttons
		JsonArray body = new JsonArray();
		body.add(message(text("Click a status to turn it on or off.", "#AAAAAA", false)));

		JsonObject preview = text("Tab list:  ", "#AAAAAA", false);
		JsonArray extra = new JsonArray();
		for (Status s : Status.values()) {
			if (active.contains(s)) extra.add(text(StatusConfig.symbol, hex(StatusConfig.color(s)), false));
		}
		if (!active.isEmpty()) extra.add(text(" ", "#FFFFFF", false));
		extra.add(text(player.getGameProfile().name(), "#FFFFFF", false));
		preview.add("extra", extra);
		body.add(message(preview));
		dialog.add("body", body);

		// Status buttons
		JsonArray actions = new JsonArray();
		for (Status s : LAYOUT) {
			actions.add(statusButton(s, active.contains(s)));
		}
		actions.add(button(text("Clear all", "#FF5555", false),
				text("Turn every status off.", "#AAAAAA", false),
				"status toggle clear"));
		dialog.add("actions", actions);

		JsonObject exit = new JsonObject();
		exit.add("label", text("Done", "#FFFFFF", false));
		exit.addProperty("width", 200);
		dialog.add("exit_action", exit);

		return dialog;
	}

	private static JsonObject statusButton(Status s, boolean on) {
		// "● ■ In Character" (on, bold) or "○ ■ In Character" (off, gray)
		JsonObject label = text(on ? "● " : "○ ", on ? "#55FF55" : "#777777", false);
		JsonArray parts = new JsonArray();
		parts.add(text(StatusConfig.symbol + " ", hex(StatusConfig.color(s)), false));
		parts.add(text(s.displayName, on ? "#FFFFFF" : "#AAAAAA", on));
		label.add("extra", parts);

		String pairNote = switch (s.group) {
			case CHARACTER -> "\nOnly one of In Character / Out of Character at a time.";
			case INTERACTIONS -> "\nOnly one of Open / Closed to Interactions at a time.";
			case NONE -> "";
		};
		JsonObject tooltip = text(s.description + pairNote, "#AAAAAA", false);

		return button(label, tooltip, "status toggle " + s.id);
	}

	private static JsonObject button(JsonObject label, JsonObject tooltip, String command) {
		JsonObject action = new JsonObject();
		action.addProperty("type", "minecraft:run_command");
		action.addProperty("command", command);

		JsonObject button = new JsonObject();
		button.add("label", label);
		button.add("tooltip", tooltip);
		button.addProperty("width", 150);
		button.add("action", action);
		return button;
	}

	private static JsonObject message(JsonObject contents) {
		JsonObject msg = new JsonObject();
		msg.addProperty("type", "minecraft:plain_message");
		msg.add("contents", contents);
		msg.addProperty("width", 310);
		return msg;
	}

	private static JsonObject text(String text, String color, boolean bold) {
		JsonObject obj = new JsonObject();
		obj.addProperty("text", text);
		obj.addProperty("color", color);
		if (bold) obj.addProperty("bold", true);
		return obj;
	}

	private static String hex(int rgb) {
		return String.format("#%06X", rgb & 0xFFFFFF);
	}
}
