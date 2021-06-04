package com.github.squirrelgrip.travis

import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.travis.model.Body
import com.github.squirrelgrip.travis.model.EnvConfig
import com.github.squirrelgrip.travis.model.Request
import com.github.squirrelgrip.travis.model.RequestConfig
import org.apache.http.HttpEntity
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.*

@Mojo(name = "trigger",
        aggregator = false,
        executionStrategy = "once-per-session",
        inheritByDefault = true,
        instantiationStrategy = InstantiationStrategy.SINGLETON,
        defaultPhase = LifecyclePhase.NONE,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE,
        requiresDirectInvocation = true,
        requiresOnline = true,
        requiresProject = true,
        threadSafe = true)
class Travis : AbstractMojo() {
    @Parameter(name = "token",
            property = "travis.token",
            required = true)
    private var token: String? = null

    @Parameter(name = "slug",
            property = "travis.slug",
            required = true)
    private var slug: String? = null

    @Parameter(name = "user",
            property = "travis.user",
            required = true)
    private var user: String = "SquirrelGrip"

    @Parameter(name = "message",
            property = "travis.message",
            required = false)
    private var message: String = "Triggered from travis-maven-plugin"

    @Parameter(name = "branch",
            property = "travis.branch",
            required = false)
    private var branch: String = "develop"

    override fun execute() {
        val httpPost = HttpPost("https://api.travis-ci.com/repo/$user%2F$slug/requests")
        httpPost.addHeader("Content-Type", "application/json")
        httpPost.addHeader("Accept", "application/json")
        httpPost.addHeader("Travis-API-Version", "3")
        httpPost.addHeader("Authorization", "token $token")
        val body = Body(Request(message, branch, RequestConfig(EnvConfig(emptyMap()))))
        val json = body.toJson()
        log.debug(json)
        httpPost.entity = StringEntity(json)

        val httpClient = HttpClients.createDefault()
        val responseHandler: ResponseHandler<String> = ResponseHandler<String> { response ->
            val status: Int = response.statusLine.statusCode
            if (status in 200..299) {
                val entity: HttpEntity = response.entity
                if (entity != null) EntityUtils.toString(entity) else ""
            } else {
                throw MojoExecutionException("Unexpected response status: $status")
            }
        }
        val responseBody: String = httpClient.execute(httpPost, responseHandler)
        if (log.isDebugEnabled) {
            log.debug(responseBody)
        } else {
            log.info("Travis build triggered")
        }
    }
}