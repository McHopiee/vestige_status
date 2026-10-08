package rpstatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * config/rpstatus/config.json - lets the server owner change the symbol and colors.
 */
public final class StatusConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static String symbol = "■";
	public static boolean spaceAfterSquares = true;
	private static final Map<Status, Integer> COLORS = new EnumMap<>(Status.class);

	private StatusConfig() {}

	public static int color(Status status) {
		return COLORS.getOrDefault(status, status.defaultColor);
	}

	public static void load(Path dir) {
		Path file = dir.resolve("config.json");
		try {
			if (Files.exists(file)) {
				JsonObject root = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
				if (root != null) {
					if (root.has("symbol")) symbol = root.get("symbol").getAsString();
					if (root.has("space_after_squares")) spaceAfterSquares = root.get("space_after_squares").getAsBoolean();
					if (root.has("colors")) {
						JsonObject colors = root.getAsJsonObject("colors");
						for (Status s : Status.values()) {
							if (colors.has(s.id)) {
								Integer rgb = parseHex(colors.get(s.id).getAsString());
								if (rgb != null) COLORS.put(s, rgb);
								else RpStatusMod.LOGGER.warn("[RP Status] Bad color for {} in config.json, using default", s.id);
							}
						}
					}
				}
			}
			save(file); // writes defaults for anything missing
		} catch (Exception e) {
			RpStatusMod.LOGGER.error("[RP Status] Couldn't read config.json, using defaults", e);
		}
	}

	private static void save(Path file) throws IOException {
		JsonObject root = new JsonObject();
		root.addProperty("symbol", symbol);
		root.addProperty("space_after_squares", spaceAfterSquares);
		JsonObject colors = new JsonObject();
		for (Status s : Status.values()) {
			colors.addProperty(s.id, String.format("#%06X", color(s)));
		}
		root.add("colors", colors);
		Files.createDirectories(file.getParent());
		Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
	}

	private static Integer parseHex(String s) {
		String hex = s.trim();
		if (hex.startsWith("#")) hex = hex.substring(1);
		if (hex.length() != 6) return null;
		try {
			return Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
