package com.motompro.instancemanager.api.node

import com.motompro.instancemanager.api.instance.Instance
import com.motompro.instancemanager.api.instance.InstanceSpecs
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents a node in the network where instances are located.
 */
interface Node {

    /**
     * The unique identifier of the node.
     */
    val uuid: UUID

    /**
     * The name of the node.
     */
    val name: String

    /**
     * The specifications of the node.
     */
    val specs: CompletableFuture<NodeSpecs>

    /**
     * The instances running on the node.
     */
    val instances: CompletableFuture<Map<UUID, Instance>>

    /**
     * Creates an instance on the node.
     * @param name The name of the instance.
     * @param specs The specifications of the instance.
     * @return A [CompletableFuture] that completes with the created instance.
     */
    fun createInstance(name: String, specs: InstanceSpecs): CompletableFuture<Instance>

    /**
     * Deletes an instance from the node.
     * @param instance The instance to delete.
     * @return A [CompletableFuture] that completes when the instance is deleted.
     */
    fun deleteInstance(instance: Instance): CompletableFuture<Void>
}