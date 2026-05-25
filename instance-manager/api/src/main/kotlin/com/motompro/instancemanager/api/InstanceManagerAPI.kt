package com.motompro.instancemanager.api

import com.motompro.instancemanager.api.instance.Instance
import com.motompro.instancemanager.api.node.Node
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * The API entry point for the Instance Manager.
 */
interface InstanceManagerAPI {

    /**
     * The nodes that the Instance Manager is managing.
     */
    val nodes: Map<UUID, Node>

    /**
     * The instances that the Instance Manager is managing.
     */
    val instances: CompletableFuture<Map<UUID, Instance>>

    /**
     * Refreshes the node list to keep track of any nodes that have been added or removed.
     */
    fun refreshNodes()
}