package io.github.hsyyid.inspector;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.hsyyid.inspector.cmdexecutors.reloadInspectorExecutor;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import io.github.hsyyid.inspector.cmdexecutors.InspectorExecutor;
import io.github.hsyyid.inspector.cmdexecutors.RollbackExecutor;
import io.github.hsyyid.inspector.cmdexecutors.ToggleInspectorExecutor;
import io.github.hsyyid.inspector.listeners.ExplosionListener;
import io.github.hsyyid.inspector.listeners.PlayerBreakBlockListener;
import io.github.hsyyid.inspector.listeners.PlayerInteractBlockListener;
import io.github.hsyyid.inspector.listeners.PlayerJoinListener;
import io.github.hsyyid.inspector.listeners.PlayerPlaceBlockListener;
import io.github.hsyyid.inspector.utilities.DatabaseManager;
import io.github.hsyyid.inspector.utilities.Region;
import me.flibio.updatifier.Updatifier;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.world.World;

@Updatifier(repoName = "Inspector", repoOwner = "hsyyid", version = "v" + PluginInfo.VERSION)
@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, dependencies = @Dependency(id = "Updatifier", version = "1.0", optional = true) )
public class Inspector
{
	private DatabaseManager databaseManager;

	private static Inspector instance;

	public static ConfigurationNode config;
	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
	public static Set<UUID> inspectorEnabledPlayers = Sets.newHashSet();
	public static Set<Region> regions = Sets.newHashSet();

	public DatabaseManager getDatabaseManager()
	{
		return databaseManager;
	}

	public static Inspector instance()
	{
		return instance;
	}

	@Inject
	private PluginContainer pluginContainer;

	public PluginContainer getPluginContainer()
	{
		return pluginContainer;
	}

	@Inject
	private Logger logger;

	public Logger getLogger()
	{
		return logger;
	}

	@Inject
	@DefaultConfig(sharedRoot = true)
	private File dConfig;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> confManager;

	@Listener
	public void onGameInit(GameStartedServerEvent event)
	{
		getLogger().info("Inspector loading...");
		instance = this;
		this.databaseManager = new DatabaseManager();

		try
		{
            loadConfig();

		}
		catch (IOException exception)
		{
			getLogger().error("The default configuration could not be loaded or created!");
		}

		HashMap<List<String>, CommandSpec> inspectorSubcommands = new HashMap<List<String>, CommandSpec>();

        inspectorSubcommands.put(Arrays.asList("reload"), CommandSpec.builder()
                .description(Text.of("reload Inspector"))
                .permission("inspector.reload")
                .executor(new reloadInspectorExecutor())
                .build());

		inspectorSubcommands.put(Arrays.asList("toggle"), CommandSpec.builder()
			.description(Text.of("Toggle Inspector Command"))
			.permission("inspector.toggle")
			.executor(new ToggleInspectorExecutor())
			.build());

		inspectorSubcommands.put(Arrays.asList("rollback"), CommandSpec.builder()
			.description(Text.of("Rollback Command"))
			.permission("inspector.rollback")
			.arguments(GenericArguments.seq(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))), GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("time"))))))
			.executor(new RollbackExecutor())
			.build());

		CommandSpec inspectorCommandSpec = CommandSpec.builder()
			.description(Text.of("Inspector Command"))
			.permission("inspector.use")
			.executor(new InspectorExecutor())
			.children(inspectorSubcommands)
			.build();

		Sponge.getCommandManager().register(this, inspectorCommandSpec, "inspector", "ins", "insp");

		Sponge.getEventManager().registerListeners(this, new PlayerPlaceBlockListener());
		Sponge.getEventManager().registerListeners(this, new PlayerInteractBlockListener());
		Sponge.getEventManager().registerListeners(this, new PlayerBreakBlockListener());
		Sponge.getEventManager().registerListeners(this, new ExplosionListener());
		Sponge.getEventManager().registerListeners(this, new PlayerJoinListener());

		getLogger().info("-----------------------------");
		getLogger().info("Inspector was created by HassanS6000!");
        getLogger().info("This version was improved by Tollainmear!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("Inspector loaded!");
	}

    public void loadConfig() throws IOException {
        if (!dConfig.exists())
        {
            dConfig.createNewFile();
            config = confManager.load();
            config.getNode("database", "mysql", "enabled").setValue(false);
            config.getNode("database", "mysql", "host").setValue("localhost");
            config.getNode("database", "mysql", "port").setValue("8080");
            config.getNode("database", "mysql", "username").setValue("username");
            config.getNode("database", "mysql", "password").setValue("pass");
            config.getNode("database", "mysql", "database").setValue("Inspector");
            config.getNode("inspector", "select", "tool").setValue("minecraft:diamond_hoe");
            Iterator<World> ite = Sponge.getServer().getWorlds().iterator();
            while (ite.hasNext()){
                config.getNode("worlds").getNode(ite.next().getName()).setValue(false);
            }
            confManager.save(config);
        }
        configurationManager = confManager;
        config = confManager.load();
    }

    public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager()
	{
		return configurationManager;
	}
}
