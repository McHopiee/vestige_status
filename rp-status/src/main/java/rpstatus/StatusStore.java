package rpstatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps each player's statuses and saves them to config/rpstatus/players.json,
 * so they survive relogs and restarts.
 *
 * TAB reads placeholders from its own thread, so each player's set is replaced
 * as a whole (never edited in place) to keep reads safe.
 */
public final class StatusStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Map<UUID, Set<Status>> STATUSES = new ConcurrentHashMap<>();
	private static Path file;

	private StatusStore() {}

	public static Set<Status> get(UUID player) {
		Set<Status> set = STATUSES.get(player);
		return set == null ? Collections.emptySet() : set;
	}

	public static boolean has(UUID player, Status status) {
		return get(player).contains(status);
	}

	/** Turns a status on or off. Turning one on switches off the other one in its pair. */
	public static void toggle(UUID player, Status status) {
		EnumSet<Status> next = copy(get(player));
		if (next.contains(status)) {
			next.remove(status);
		} else {
			if (status.group != Status.Group.NONE) {
				next.removeIf(other -> other.group == status.group);
			}
			next.add(status);
		}
		put(player, next);
	}

	public static void clear(UUID player) {
		put(player, EnumSet.noneOf(Status.class));
	}

	private static void put(UUID player, EnumSet<Status> next) {
		if (next.isEmpty()) {
			STATUSES.remove(player);
		} else {
			STATUSES.put(player, Collections.unmodifiableSet(next));
		}
		save();
	}

	private static EnumSet<Status> copy(Set<Status> set) {
		return set.isEmpty() ? EnumSet.noneOf(Status.class) : EnumSet.copyOf(set);
	}

	public static void load(Path dir) {
		file = dir.resolve("players.json");
		STATUSES.clear();
		if (!Files.exists(file)) return;
		try {
			JsonObject root = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
			if (root == null) return;
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				EnumSet<Status> set = EnumSet.noneOf(Status.class);
				for (JsonElement el : entry.getValue().getAsJsonArray()) {
					Status s = Status.byId(el.getAsString());
					if (s != null) set.add(s);
				}
				if (!set.isEmpty()) {
					STATUSES.put(UUID.fromString(entry.getKey()), Collections.unmodifiableSet(set));
				}
			}
		} catch (Exception e) {
			RpStatusMod.LOGGER.error("[RP Status] Couldn't read players.json", e);
		}
	}

	public static synchronized void save() {
		if (file == null) return;
		JsonObject root = new JsonObject();
		STATUSES.forEach((uuid, set) -> {
			JsonArray arr = new JsonArray();
			for (Status s : set) arr.add(s.id);
			root.add(uuid.toString(), arr);
		});
		try {
			Files.createDirectories(file.getParent());
			Path tmp = file.resolveSibling("players.json.tmp");
			Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			RpStatusMod.LOGGER.error("[RP Status] Couldn't save players.json", e);
		}
	}
}
