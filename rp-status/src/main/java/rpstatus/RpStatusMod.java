package rpstatus;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
			// /status                -> open the status screen
			// /status clear          -> turn everything off (from chat)
			// /status toggle <id>    -> used by the screen's buttons
			LiteralArgumentBuilder<CommandSourceStack> toggle = Commands.literal("toggle");
			for (Status status : Status.values()) {
				toggle.then(Commands.literal(status.id).executes(ctx -> {
					ServerPlayer player = requirePlayer(ctx.getSource());
					if (player == null) return 0;
					StatusStore.toggle(player.getUUID(), status);
					StatusDialog.show(player);
					return 1;
				}));
			}
			toggle.then(Commands.literal("clear").executes(ctx -> {
				ServerPlayer player = requirePlayer(ctx.getSource());
				if (player == null) return 0;
				StatusStore.clear(player.getUUID());
				StatusDialog.show(player);
				return 1;
			}));

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
					.then(toggle));
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
