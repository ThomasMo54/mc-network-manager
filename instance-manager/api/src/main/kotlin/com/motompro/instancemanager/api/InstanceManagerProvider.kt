package com.motompro.instancemanager.api

object InstanceManagerProvider {

    private var _instance: InstanceManagerAPI? = null

    val instance: InstanceManagerAPI
        get() = _instance ?: throw IllegalStateException("InstanceManagerAPI not registered")

    fun register(impl: InstanceManagerAPI) {
        check(_instance == null) { "InstanceManagerAPI already registered" }
        _instance = impl
    }
}