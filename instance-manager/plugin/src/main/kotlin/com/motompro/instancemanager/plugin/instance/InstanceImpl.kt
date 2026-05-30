package com.motompro.instancemanager.plugin.instance

import com.motompro.instancemanager.api.instance.Instance
import com.motompro.instancemanager.api.instance.InstanceSpecs
import com.motompro.instancemanager.api.node.Node
import com.motompro.instancemanager.plugin.InstanceManagerPlugin
import com.velocitypowered.api.proxy.server.RegisteredServer
import java.util.UUID
import java.util.concurrent.CompletableFuture

class InstanceImpl(
    val id: Int,
    override val uuid: UUID,
    override val name: String,
    override val node: Node,
) : Instance {

    private val proxy = InstanceManagerPlugin.instance.server
    private val logger = InstanceManagerPlugin.instance.logger
    private val pterodactylService = InstanceManagerPlugin.instance.pterodactylService

    override val specs: CompletableFuture<InstanceSpecs>
        get() = pterodactylService.getInstanceDetails(this)

    override val velocityServer: RegisteredServer
        get() = proxy.getServer(uuid.toString()).get()

    override val isOnline: CompletableFuture<Boolean>
        get() = pterodactylService.getInstanceApplicationStatus(this).thenApply { it == "null" }

    override val isRunning: CompletableFuture<Boolean>
        get() = pterodactylService.getInstanceStatus(this).thenApply { it == "running" }

    override fun start(): CompletableFuture<Void> {
        return pterodactylService.sendPowerCommand(this, "start").thenAccept {
            var polls = 0
            while (polls < STATUS_POLLING_TIMEOUT && !isRunning.join()) {
                polls++
                Thread.sleep(3000)
            }
            if (polls == STATUS_POLLING_TIMEOUT) throw IllegalStateException()
            logger.info("Started instance $uuid")
        }
    }

    override fun stop(): CompletableFuture<Void> {
        return pterodactylService.sendPowerCommand(this, "stop").thenAccept {
            var polls = 0
            while (polls < STATUS_POLLING_TIMEOUT && isRunning.join()) {
                polls++
                Thread.sleep(3000)
            }
            if (polls == STATUS_POLLING_TIMEOUT) throw IllegalStateException()
            logger.info("Stopped instance $uuid")
        }
    }

    override fun restart(): CompletableFuture<Void> {
        return pterodactylService.sendPowerCommand(this, "restart").thenAccept {
            var polls = 0
            while (polls < STATUS_POLLING_TIMEOUT && !isRunning.join()) {
                polls++
                Thread.sleep(3000)
            }
            if (polls == STATUS_POLLING_TIMEOUT) throw IllegalStateException()
            logger.info("Restarted instance $uuid")
        }
    }

    override fun kill(): CompletableFuture<Void> {
        return pterodactylService.sendPowerCommand(this, "kill").thenAccept {
            var polls = 0
            while (polls < STATUS_POLLING_TIMEOUT && isRunning.join()) {
                polls++
                Thread.sleep(3000)
            }
            if (polls == STATUS_POLLING_TIMEOUT) throw IllegalStateException()
            logger.info("Killed instance $uuid")
        }
    }

    override fun sendCommand(command: String): CompletableFuture<Void> {
        return pterodactylService.sendCommand(this, command)
    }

    companion object {
        private const val STATUS_POLLING_TIMEOUT = 100
    }
}