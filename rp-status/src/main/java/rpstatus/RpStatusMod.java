package rpstatus;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.EnumSet;

public class RpStatusMod implements ModInitializer {
	public static final String MOD_ID = "rpstatus";
	public static final Logger LOGGER = LoggerFactory.getLogger("RP Status");

	@Override
	public void onInitialize() {
		Path dir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
		StatusConfig.load(dir);
		StatusStore.load(dir);

		registerPlaceholders();

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			// /status          -> open the status screen
			// /status clear    -> turn everything off
			// /status set ...  -> used by the screen's Save button:
			//   /status set <none|in_character|out_of_character> <none|open_to_interactions|closed_to_interactions> <dnd> <recording> <streaming>
			dispatcher.register(Commands.literal("status")
					.executes(ctx -> {
						ServerPlayer player = requirePlayer(ctx.getSource());
						if (player == null) return 0;
						StatusDialog.show(player);
						return 1;
					})
					.then(Commands.literal("clear").executes(ctx -> {
						ServerPlayer player = requirePlayer(ctx.getSource());
						if (player == null) return 0;
						StatusStore.clear(player.getUUID());
						ctx.getSource().sendSuccess(() -> Component.literal("Your statuses have been cleared."), false);
						return 1;
					}))
					.then(Commands.literal("set")
							.then(Commands.argument("character", StringArgumentType.word())
							.then(Commands.argument("interaction", StringArgumentType.word())
							.then(Commands.argument("dnd", BoolArgumentType.bool())
							.then(Commands.argument("recording", BoolArgumentType.bool())
							.then(Commands.argument("streaming", BoolArgumentType.bool())
									.executes(ctx -> {
										ServerPlayer player = requirePlayer(ctx.getSource());
										if (player == null) return 0;

										EnumSet<Status> next = EnumSet.noneOf(Status.class);
										Status character = Status.byId(StringArgumentType.getString(ctx, "character"));
										if (character != null && character.group == Status.Group.CHARACTER) next.add(character);
										Status interaction = Status.byId(StringArgumentType.getString(ctx, "interaction"));
										if (interaction != null && interaction.group == Status.Group.INTERACTIONS) next.add(interaction);
										if (BoolArgumentType.getBool(ctx, "dnd")) next.add(Status.DO_NOT_DISTURB);
										if (BoolArgumentType.getBool(ctx, "recording")) next.add(Status.RECORDING);
										if (BoolArgumentType.getBool(ctx, "streaming")) next.add(Status.STREAMING);

										StatusStore.set(player.getUUID(), next);

										String names = StatusText.names(player.getUUID());
										Component msg = Component.literal("Status updated: ")
												.append(StatusText.squares(player.getUUID()))
												.append(Component.literal(names.isEmpty() ? "no status" : names));
										ctx.getSource().sendSuccess(() -> msg, false);
										return 1;
									}))))))));
		});

		LOGGER.info("[RP Status] Loaded. Add %rpstatus:squares% to your TAB tablist prefix to show statuses.");
	}

	private static ServerPlayer requirePlayer(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.literal("Only players can use this."));
		}
		return player;
	}

	/**
	 * %rpstatus:squares% -> colored squares, e.g. "■■ "
	 * %rpstatus:names%   -> "In Character, Streaming"
	 * TAB on Fabric picks these up through Placeholder API.
	 */
	private static void registerPlaceholders() {
		Placeholders.registerServer(Identifier.fromNamespaceAndPath(MOD_ID, "squares"), (ctx, arg) -> {
			if (!ctx.hasServerPlayer()) return PlaceholderResult.invalid("No player!");
			return PlaceholderResult.value(StatusText.squares(ctx.serverPlayer().getUUID()));
		});
		Placeholders.registerServer(Identifier.fromNamespaceAndPath(MOD_ID, "names"), (ctx, arg) -> {
			if (!ctx.hasServerPlayer()) return PlaceholderResult.invalid("No player!");
			return PlaceholderResult.value(StatusText.names(ctx.serverPlayer().getUUID()));
		});
	}
}
