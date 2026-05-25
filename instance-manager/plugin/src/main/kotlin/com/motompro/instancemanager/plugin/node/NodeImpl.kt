package com.motompro.instancemanager.plugin.node

import com.motompro.instancemanager.api.instance.Instance
import com.motompro.instancemanager.api.instance.InstanceSpecs
import com.motompro.instancemanager.api.node.Node
import com.motompro.instancemanager.api.node.NodeSpecs
import com.motompro.instancemanager.plugin.InstanceManagerPlugin
import com.motompro.instancemanager.plugin.instance.InstanceImpl
import com.velocitypowered.api.proxy.server.ServerInfo
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.CompletableFuture

class NodeImpl(
    val id: Int,
    override val uuid: UUID,
    override val name: String,
) : Node {

    private val proxy = InstanceManagerPlugin.instance.server
    private val logger = InstanceManagerPlugin.instance.logger
    private val pterodactylService = InstanceManagerPlugin.instance.pterodactylService

    override val specs: CompletableFuture<NodeSpecs>
        get() = pterodactylService.getNodeDetails(this)

    override val instances: CompletableFuture<Map<UUID, Instance>>
        get() = pterodactylService.getNodeInstances(this).thenApply { instances -> instances.associateBy { it.uuid } }

    override fun createInstance(name: String, specs: InstanceSpecs): CompletableFuture<Instance> {
        return pterodactylService.createInstance(this, name, specs).thenApply { (instance, ip, port) ->
            val address = InetSocketAddress(ip, port)
            val serverInfo = ServerInfo(instance.uuid.toString(), address)
            proxy.registerServer(serverInfo)
            logger.info("Registered server ${instance.uuid} at $address on node ${this.name}")
            return@thenApply instance
        }
    }

    override fun deleteInstance(instance: Instance): CompletableFuture<Void> {
        return pterodactylService.deleteInstance(instance as InstanceImpl).thenAccept {
            logger.info("Deleted server ${instance.uuid} on node ${this.name}")
        }
    }
}