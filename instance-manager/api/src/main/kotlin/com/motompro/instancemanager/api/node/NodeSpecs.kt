package com.motompro.instancemanager.api.node

/**
 * Represents the specifications of a node.
 * @param memory The amount of memory allocated to this node in MiB.
 * @param disk The amount of disk space allocated to this node in MiB.
 */
data class NodeSpecs(
    val memory: Int,
    val disk: Int,
)