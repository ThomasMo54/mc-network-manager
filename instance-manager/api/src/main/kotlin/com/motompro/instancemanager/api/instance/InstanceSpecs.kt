package com.motompro.instancemanager.api.instance

/**
 * Represents the specifications of an instance.
 * @param egg The egg ID to use for the instance.
 * @param memory The amount of memory to allocate to the instance in MiB.
 * @param swap The amount of swap to allocate to the instance in MiB.
 * @param disk The amount of disk space to allocate to the instance in MiB.
 * @param io The I/O performance of the instance. 10 is the minimum. 1000 is the maximum.
 * @param cpu The CPU usage limit of the instance in percentage.
 * @param environment The environment variables to set for the instance.
 */
data class InstanceSpecs(
    val egg: Int,
    val memory: Int,
    val swap: Int,
    val disk: Int,
    val io: Int,
    val cpu: Int,
    val environment: Map<String, String>?,
)
