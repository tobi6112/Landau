package de.tobi6112.landau.app.connect.service

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.reactor.monoResponse
import reactor.core.publisher.Mono

/** Service client for CodeWars */
class CodewarsServiceClient : ServiceClient {
  private val apiUrl = "https://www.codewars.com/api/v1"

  override fun isValidIdentifier(identifier: String): Mono<Boolean> {
    return Fuel.get("$apiUrl/users/$identifier")
        .header(Headers.ACCEPT to "application/json")
        .monoResponse()
        .flatMap { response ->
          when (response.statusCode) {
            200 -> Mono.just(true)
            404 -> Mono.just(false)
            else ->
              Mono.error(
                  RuntimeException(
                    "Received unexpected response with status code ${response.statusCode}"))
          }
        }
  }
}
