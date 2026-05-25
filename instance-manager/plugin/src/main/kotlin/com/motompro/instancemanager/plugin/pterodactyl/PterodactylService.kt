package com.motompro.instancemanager.plugin.pterodactyl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.motompro.instancemanager.api.instance.Instance
import com.motompro.instancemanager.api.instance.InstanceSpecs
import com.motompro.instancemanager.api.node.Node
import com.motompro.instancemanager.api.node.NodeSpecs
import com.motompro.instancemanager.plugin.instance.InstanceImpl
import com.motompro.instancemanager.plugin.node.NodeImpl
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PterodactylService(
    panelUrl: String,
    private val appKey: String,
    private val clientKey: String,
    private val userId: String,
) {

    private val applicationApiUri = "$panelUrl/api/application"
    private val clientApiUri = "$panelUrl/api/client"

    private val jsonMapper = jacksonObjectMapper()

    private val httpClient = HttpClient.newHttpClient()

    fun listNodes(): CompletableFuture<List<Node>> {
        return CompletableFuture<List<Node>>().completeAsync {
            val request = buildApplicationRequest("$applicationApiUri/nodes")
                .GET()
                .build()
            val response = sendRequest(request)
            return@completeAsync response.get("data").map { jsonNode ->
                val attributes = jsonNode.get("attributes")
                NodeImpl(
                    attributes.get("id").asInt(),
                    UUID.fromString(attributes.get("uuid").asText()),
                    attributes.get("name").asText(),
                )
            }
        }
    }

    fun getNodeDetails(node: NodeImpl): CompletableFuture<NodeSpecs> {
        return CompletableFuture<NodeSpecs>().completeAsync {
            val request = buildApplicationRequest("$applicationApiUri/nodes/${node.id}")
                .GET()
                .build()
            val response = sendRequest(request)
            val attributes = response.get("attributes")
            return@completeAsync NodeSpecs(
                attributes.get("memory").asInt(),
                attributes.get("disk").asInt(),
            )
        }
    }

    fun getNodeInstances(node: NodeImpl): CompletableFuture<List<Instance>> {
        return CompletableFuture<List<Instance>>().completeAsync {
            val request = buildApplicationRequest("$applicationApiUri/nodes/${node.id}?include=servers")
                .GET()
                .build()
            val response = sendRequest(request)
            return@completeAsync response.get("attributes").get("relationships").get("servers").get("data").map { jsonInstanceData ->
                parseInstance(node, jsonInstanceData)
            }
        }
    }

    fun listNodeAllocations(node: NodeImpl, page: Int = 1): CompletableFuture<List<Allocation>> {
        return CompletableFuture<List<Allocation>>().completeAsync {
            val request = buildApplicationRequest("$applicationApiUri/nodes/${node.id}/allocations?page=${page}&per_page=100")
                .GET()
                .build()
            val response = sendRequest(request)
            return@completeAsync response.get("data").map { jsonAllocationData ->
                val attributes = jsonAllocationData.get("attributes")
                Allocation(
                    attributes.get("id").asInt(),
                    attributes.get("ip").asText(),
                    attributes.get("port").asInt(),
                    attributes.get("assigned").asBoolean(),
                )
            }
        }
    }

    fun createInstance(node: NodeImpl, name: String, specs: InstanceSpecs): CompletableFuture<InstanceDetails> {
        return listNodeAllocations(node).thenApply { allocations ->
            val freeAllocation = allocations.firstOrNull { !it.assigned } ?: throw IllegalStateException("No free allocation found")

            val body = jsonMapper.createObjectNode()
            body.put("name", name)
            body.put("user", userId)
            body.put("egg", specs.egg)
            body.put("docker_image", "ghcr.io/pterodactyl/yolks:java_21")
            body.put("startup", "java -Xms${specs.memory}M -XX:MaxRAMPercentage=95.0 -Dterminal.jline=false -Dterminal.ansi=true -jar {{SERVER_JARFILE}}")
            val limits = jsonMapper.createObjectNode()
            limits.put("memory", specs.memory)
            limits.put("swap", specs.swap)
            limits.put("disk", specs.disk)
            limits.put("io", specs.io)
            limits.put("cpu", specs.cpu)
            limits.put("oom_disabled", true)
            body.set<JsonNode>("limits", limits)
            val environment = jsonMapper.createObjectNode()
            environment.put("SERVER_JARFILE", "server.jar")
            environment.put("BUILD_NUMBER", "latest")
            specs.environment?.forEach { (key, value) -> environment.put(key, value) }
            body.set<JsonNode>("environment", environment)
            val featureLimits = jsonMapper.createObjectNode()
            featureLimits.put("databases", 0)
            featureLimits.put("allocations", 0)
            featureLimits.put("backups", 0)
            body.set<JsonNode>("feature_limits", featureLimits)
            val allocation = jsonMapper.createObjectNode()
            allocation.put("default", freeAllocation.id)
            body.set<JsonNode>("allocation", allocation)

            val request = buildApplicationRequest("$applicationApiUri/servers")
                .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(body)))
                .build()
            val response = sendRequest(request)
            return@thenApply InstanceDetails(
                parseInstance(node, response),
                freeAllocation.ip,
                freeAllocation.port,
            )
        }
    }

    fun deleteInstance(instance: InstanceImpl): CompletableFuture<Void> {
        return CompletableFuture<Void>().completeAsync {
            val request = buildApplicationRequest("$applicationApiUri/servers/${instance.id}?force=true")
                .DELETE()
                .build()
            sendRequest(request)
            return@completeAsync null
        }
    }

    fun getInstanceDetails(instance: InstanceImpl): CompletableFuture<InstanceSpecs> {
        return CompletableFuture<InstanceSpecs>().completeAsync {
            val request = buildApplicationRequest("$applicationApiUri/servers/${instance.id}")
                .GET()
                .build()
            val response = sendRequest(request)
            val attributes = response.get("attributes")
            return@completeAsync InstanceSpecs(
                attributes.get("egg").asInt(),
                attributes.get("limits").get("memory").asInt(),
                attributes.get("limits").get("swap").asInt(),
                attributes.get("limits").get("disk").asInt(),
                attributes.get("limits").get("io").asInt(),
                attributes.get("limits").get("cpu").asInt(),
                null
            )
        }
    }

    fun getInstanceStatus(instance: InstanceImpl): CompletableFuture<String> {
        return CompletableFuture<String>().completeAsync {
            val request = buildClientRequest("$clientApiUri/servers/${instance.uuid.toString().split("-").first()}")
                .GET()
                .build()
            val response = sendRequest(request)
            return@completeAsync response.get("attributes").get("status").asText()
        }
    }

    fun sendPowerCommand(instance: InstanceImpl, command: String): CompletableFuture<Void> {
        return CompletableFuture<Void>().completeAsync {
            val request = buildClientRequest("$clientApiUri/servers/${instance.uuid.toString().split("-").first()}/power")
                .POST(HttpRequest.BodyPublishers.ofString("{ \"signal\": \"$command\" }"))
                .build()
            sendRequest(request)
            return@completeAsync null
        }
    }

    fun sendCommand(instance: InstanceImpl, command: String): CompletableFuture<Void> {
        return CompletableFuture<Void>().completeAsync {
            val request = buildClientRequest("$clientApiUri/servers/${instance.uuid.toString().split("-").first()}/command")
                .POST(HttpRequest.BodyPublishers.ofString("{ \"command\": \"$command\" }"))
                .build()
            sendRequest(request)
            return@completeAsync null
        }
    }

    fun getWebSocketConnection(instance: InstanceImpl): CompletableFuture<WebSocketConnection> {
        return CompletableFuture<WebSocketConnection>().completeAsync {
            val request = buildClientRequest("$clientApiUri/servers/${instance.uuid.toString().split("-").first()}/websocket")
                .GET()
                .build()
            val response = sendRequest(request)
            return@completeAsync WebSocketConnection(
                response.get("data").get("token").asText(),
                response.get("data").get("socket").asText()
            )
        }
    }

    private fun buildApplicationRequest(uri: String): HttpRequest.Builder {
        return HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("Authorization", "Bearer $appKey")
            .header("Accept", "Application/vnd.pterodactyl.v1+json")
            .header("Content-Type", "application/json")
    }

    private fun buildClientRequest(uri: String): HttpRequest.Builder {
        return HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("Authorization", "Bearer $clientKey")
            .header("Accept", "Application/vnd.pterodactyl.v1+json")
            .header("Content-Type", "application/json")
    }

    private fun sendRequest(request: HttpRequest): JsonNode {
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return jsonMapper.readTree(response.body())
    }

    private fun parseInstance(node: NodeImpl, jsonInstanceData: JsonNode): Instance {
        val attributes = jsonInstanceData.get("attributes")
        return InstanceImpl(
            attributes.get("id").asInt(),
            UUID.fromString(attributes.get("uuid").asText()),
            attributes.get("name").asText(),
            node,
        )
    }

    data class Allocation(
        val id: Int,
        val ip: String,
        val port: Int,
        val assigned: Boolean,
    )

    data class InstanceDetails(
        val instance: Instance,
        val ip: String,
        val port: Int,
    )

    data class WebSocketConnection(
        val token: String,
        val socket: String,
    )
}