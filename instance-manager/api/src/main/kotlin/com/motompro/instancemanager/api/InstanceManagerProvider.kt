package com.motompro.instancemanager.api

object InstanceManagerProvider {

    private lateinit var instance: InstanceManagerAPI

    fun register(impl: InstanceManagerAPI) {
        instance = impl
    }

    fun get(): InstanceManagerAPI = instance
}