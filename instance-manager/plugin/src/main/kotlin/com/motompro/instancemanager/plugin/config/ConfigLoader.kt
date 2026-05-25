package com.motompro.instancemanager.plugin.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

object ConfigLoader {

    private val mapper = ObjectMapper(YAMLFactory()).apply { registerKotlinModule() }

    /**
     * Load a YAML file and map its content into the given class.
     * @param file the YAML file
     * @param configClass the mapped class
     */
    fun <T> loadConfig(file: File, configClass: Class<T>): T {
        val inputStream = file.inputStream()
        return mapper.readValue<T>(inputStream, configClass)
    }
}