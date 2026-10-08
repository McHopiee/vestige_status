package rpstatus;

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
			dispatcher.register(Commands.literal("status")
					.executes(ctx -> openMenu(ctx.getSource()))
					.then(Commands.literal("clear").executes(ctx -> {
						ServerPlayer player = ctx.getSource().getPlayer();
						if (player == null) {
							ctx.getSource().sendFailure(Component.literal("Only players can use this."));
							return 0;
						}
						StatusStore.clear(player.getUUID());
						ctx.getSource().sendSuccess(() -> Component.literal("Your statuses have been cleared."), false);
						return 1;
					})));
		});

		LOGGER.info("[RP Status] Loaded. Add %rpstatus:squares% to your TAB tablist prefix to show statuses.");
	}

	private static int openMenu(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.literal("Only players can use this."));
			return 0;
		}
		new StatusMenu(player).open();
		return 1;
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
