package rpstatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.Set;

/**
 * The /status screen, built on Minecraft's built-in dialog window so players
 * don't need to install anything.
 *
 *   ✦ Status Settings ✦
 *   Set your current status so others know how to interact with you!
 *   [ Character Status: ■ In Character      ]   (click to cycle)
 *   [ Interaction Status: ■ Open to Interactions ]
 *   [x] ■ Do Not Disturb
 *   [ ] ■ Recording
 *   [ ] ■ Streaming
 *   [ Save ] [ Clear all ]
 *            [ Cancel ]
 *
 * Save runs "/status set <character> <interaction> <dnd> <recording> <streaming>".
 */
public final class StatusDialog {
	private static final String PURPLE = "#B98CFF";
	private static final String GREEN = "#6BE38A";
	private static final String GOLD = "#FFCC55";
	private static final String GRAY = "#AAAAAA";
	private static final String WHITE = "#FFFFFF";

	private StatusDialog() {}

	public static void show(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		JsonObject json = build(player);

		Optional<Holder<Dialog>> dialog = Dialog.CODEC
				.parse(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), json)
				.resultOrPartial(error -> RpStatusMod.LOGGER.error("[RP Status] Couldn't build the status screen: {}", error));

		if (dialog.isPresent()) {
			player.openDialog(dialog.get());
		} else {
			player.sendSystemMessage(Component.literal("The status screen couldn't open. Please tell a server admin to check the server log."), false);
		}
	}

	private static JsonObject build(ServerPlayer player) {
		Set<Status> active = StatusStore.get(player.getUUID());

		JsonObject dialog = new JsonObject();
		dialog.addProperty("type", "minecraft:multi_action");
		dialog.add("title", text("✦ Status Settings ✦", PURPLE, true));
		dialog.addProperty("can_close_with_escape", true);
		dialog.addProperty("pause", false);
		dialog.addProperty("columns", 2);

		// --- Text at the top ---
		JsonArray body = new JsonArray();
		body.add(message(text("Set your current status so others know how to interact with you!", GRAY, false)));
		body.add(message(preview(player, active)));
		dialog.add("body", body);

		// --- The choices ---
		JsonArray inputs = new JsonArray();

		inputs.add(choice("character", text("✦ Character Status", PURPLE, false),
				current(active, Status.IN_CHARACTER, Status.OUT_OF_CHARACTER),
				Status.IN_CHARACTER, Status.OUT_OF_CHARACTER));

		inputs.add(choice("interaction", text("✦ Interaction Status", GREEN, false),
				current(active, Status.OPEN_TO_INTERACTIONS, Status.CLOSED_TO_INTERACTIONS),
				Status.OPEN_TO_INTERACTIONS, Status.CLOSED_TO_INTERACTIONS));

		inputs.add(checkbox("dnd", Status.DO_NOT_DISTURB, active));
		inputs.add(checkbox("recording", Status.RECORDING, active));
		inputs.add(checkbox("streaming", Status.STREAMING, active));
		dialog.add("inputs", inputs);

		// --- Buttons ---
		JsonArray actions = new JsonArray();

		JsonObject save = new JsonObject();
		save.addProperty("type", "minecraft:dynamic/run_command");
		save.addProperty("template", "status set $(character) $(interaction) $(dnd) $(recording) $(streaming)");
		actions.add(button(text("✔ Save", GREEN, true), save));

		JsonObject clear = new JsonObject();
		clear.addProperty("type", "minecraft:run_command");
		clear.addProperty("command", "status clear");
		actions.add(button(text("Clear all", "#FF6B6B", false), clear));

		dialog.add("actions", actions);

		JsonObject exit = new JsonObject();
		exit.add("label", text("Cancel", WHITE, false));
		exit.addProperty("width", 150);
		dialog.add("exit_action", exit);

		return dialog;
	}

	/** "Tab list:  ■■ McHopie" */
	private static JsonObject preview(ServerPlayer player, Set<Status> active) {
		JsonObject line = text("Tab list:  ", GRAY, false);
		JsonArray extra = new JsonArray();
		for (Status s : Status.values()) {
			if (active.contains(s)) extra.add(square(s));
		}
		if (!active.isEmpty()) extra.add(text(" ", WHITE, false));
		extra.add(text(player.getGameProfile().name(), WHITE, false));
		line.add("extra", extra);
		return line;
	}

	private static String current(Set<Status> active, Status a, Status b) {
		if (active.contains(a)) return a.id;
		if (active.contains(b)) return b.id;
		return "none";
	}

	/** A button that cycles None -> a -> b when clicked. */
	private static JsonObject choice(String key, JsonObject label, String selected, Status a, Status b) {
		JsonArray options = new JsonArray();
		options.add(option("none", text("None", GRAY, false), selected));
		options.add(option(a.id, labelFor(a), selected));
		options.add(option(b.id, labelFor(b), selected));

		JsonObject input = new JsonObject();
		input.addProperty("type", "minecraft:single_option");
		input.addProperty("key", key);
		input.add("label", label);
		input.addProperty("width", 300);
		input.add("options", options);
		return input;
	}

	private static JsonObject option(String id, JsonObject display, String selected) {
		JsonObject option = new JsonObject();
		option.addProperty("id", id);
		option.add("display", display);
		if (id.equals(selected)) option.addProperty("initial", true);
		return option;
	}

	private static JsonObject checkbox(String key, Status status, Set<Status> active) {
		JsonObject input = new JsonObject();
		input.addProperty("type", "minecraft:boolean");
		input.addProperty("key", key);
		input.add("label", labelFor(status));
		input.addProperty("initial", active.contains(status));
		input.addProperty("on_true", "true");
		input.addProperty("on_false", "false");
		return input;
	}

	/** "■ In Character" with the square in the status color. */
	private static JsonObject labelFor(Status s) {
		JsonObject label = square(s);
		JsonArray extra = new JsonArray();
		extra.add(text(" " + s.displayName, WHITE, false));
		label.add("extra", extra);
		return label;
	}

	private static JsonObject square(Status s) {
		return text(StatusConfig.symbol, String.format("#%06X", StatusConfig.color(s) & 0xFFFFFF), false);
	}

	private static JsonObject button(JsonObject label, JsonObject action) {
		JsonObject button = new JsonObject();
		button.add("label", label);
		button.addProperty("width", 150);
		button.add("action", action);
		return button;
	}

	private static JsonObject message(JsonObject contents) {
		JsonObject msg = new JsonObject();
		msg.addProperty("type", "minecraft:plain_message");
		msg.add("contents", contents);
		msg.addProperty("width", 300);
		return msg;
	}

	private static JsonObject text(String text, String color, boolean bold) {
		JsonObject obj = new JsonObject();
		obj.addProperty("text", text);
		obj.addProperty("color", color);
		if (bold) obj.addProperty("bold", true);
		return obj;
	}
}
