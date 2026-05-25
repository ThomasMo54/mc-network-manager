package com.motompro.instancemanager.plugin

import com.google.inject.Inject
import com.motompro.instancemanager.api.InstanceManagerAPI
import com.motompro.instancemanager.api.InstanceManagerProvider
import com.motompro.instancemanager.api.instance.Instance
import com.motompro.instancemanager.api.node.Node
import com.motompro.instancemanager.plugin.config.ConfigLoader
import com.motompro.instancemanager.plugin.config.general.GeneralConfig
import com.motompro.instancemanager.plugin.pterodactyl.PterodactylService
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture


@Plugin(
    id = "mtp-instance-manager",
    name = "InstanceManager",
    version = "1.0.0",
    authors = ["TomMo"],
)
class InstanceManagerPlugin @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @param:DataDirectory private val dataDirectory: Path,
) : InstanceManagerAPI {

    val config: GeneralConfig

    val pterodactylService: PterodactylService

    private var _nodes: Map<UUID, Node> = emptyMap()
    override val nodes: Map<UUID, Node>
        get() = _nodes

    override val instances: CompletableFuture<Map<UUID, Instance>>
        get() = TODO("Not yet implemented")

    init {
        _instance = this

        // Register API implementation
        InstanceManagerProvider.register(this)

        // Load configuration
        val configFile = dataDirectory.resolve(CONFIG_FILE_NAME).toFile()
        config = ConfigLoader.loadConfig(configFile, GeneralConfig::class.java)

        // Init pterodactyl service
        val pterodactylConfig = config.pterodactyl
        pterodactylService = PterodactylService(
            pterodactylConfig.panelUrl,
            pterodactylConfig.appKey,
            pterodactylConfig.clientKey,
            pterodactylConfig.userId,
        )

        logger.info("InstanceManagerPlugin initialized")
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        // Fetch nodes
        refreshNodes()
    }

    override fun refreshNodes(): CompletableFuture<Map<UUID, Node>> {
        return pterodactylService.listNodes().thenApply { nodes ->
            _nodes = nodes.associateBy { it.uuid }
            logger.info("Fetched ${nodes.size} nodes: ${nodes.joinToString { it.name }}")
            _nodes
        }
    }

    companion object {
        private const val CONFIG_FILE_NAME = "config.yml"

        private var _instance: InstanceManagerPlugin? = null
        val instance: InstanceManagerPlugin
            get() = _instance ?: throw IllegalStateException("InstanceManagerPlugin not registered")
    }
}