package com.motompro.instancemanager.api.instance

import com.motompro.instancemanager.api.node.Node
import com.velocitypowered.api.proxy.server.RegisteredServer
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents an instance in the network.
 */
interface Instance {

    /**
     * The unique identifier of the instance.
     */
    val uuid: UUID

    /**
     * The name of the instance.
     */
    val name: String

    /**
     * The node that the instance is running on.
     */
    val node: Node

    /**
     * The specifications of the instance.
     */
    val specs: CompletableFuture<InstanceSpecs>

    /**
     * The Velocity server linked to the instance.
     */
    val velocityServer: RegisteredServer

    /**
     * Whether the instance is online.
     */
    val isOnline: CompletableFuture<Boolean>

    /**
     * Whether the instance is currently running.
     */
    val isRunning: CompletableFuture<Boolean>

    /**
     * Starts the instance.
     * @return A [CompletableFuture] that completes when the instance is started.
     */
    fun start(): CompletableFuture<Void>

    /**
     * Stops the instance.
     * @return A [CompletableFuture] that completes when the instance is stopped.
     */
    fun stop(): CompletableFuture<Void>

    /**
     * Restarts the instance.
     * @return A [CompletableFuture] that completes when the instance is restarted.
     */
    fun restart(): CompletableFuture<Void>

    /**
     * Kills the instance.
     * @return A [CompletableFuture] that completes when the instance is killed.
     */
    fun kill(): CompletableFuture<Void>

    /**
     * Sends a command to the instance's console.
     * @param command The command to send.
     * @return A [CompletableFuture] that completes when the command is sent.
     */
    fun sendCommand(command: String): CompletableFuture<Void>
}