package com.turingalan.pokemon.data.remote

import com.turingalan.pokemon.data.PokemonDataSource
import com.turingalan.pokemon.data.model.Pokemon
import com.turingalan.pokemon.data.remote.model.PokemonRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.lang.RuntimeException
import javax.inject.Inject

class PokemonRemoteDataSource
@Inject constructor(
    private val api: PokemonApi,
    private val scope: CoroutineScope
): PokemonDataSource {
    override fun observe(): Flow<Result<List<Pokemon>>> {
        return flow {
            emit(Result.success(listOf()))
            emit(readAll())
        }.shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000L),
            replay = 1
        )
    }

    override suspend fun readAll(): Result<List<Pokemon>> {
        //TODO REMOVE CODIGO MALO
        val response = api.readAll(limit = 40, offset = 0)
        val finalList = mutableListOf<Pokemon>()
        return if(response.isSuccessful){
            val body = response.body()!!
            for(result in body.results){
                val remotePokemonResult = readOne(result.name)

                remotePokemonResult.getOrNull()?.let{ pokemon ->
                    finalList.add(pokemon)
                }
            }
            Result.success(finalList)
      }else{
            Result.failure(RuntimeException())
        }
    }

    suspend fun readOne(name: String): Result<Pokemon> {
        val response = try {
            api.readOne(name)
        } catch (e: Exception) {
            // Manejar errores de red, timeouts, etc.
            return Result.failure(e)
        }

        return if (response.isSuccessful) {
            val pokemonResponse = response.body()
            if (pokemonResponse != null) {
                val pokemon = pokemonResponse.toExternal() // Asegúrate de que .toExternal() existe y es correcto
                Result.success(pokemon)
            } else {
                // Respuesta exitosa (2xx) pero sin cuerpo (cuerpo nulo)
                Result.failure(IllegalStateException("Respuesta exitosa, pero el cuerpo está vacío."))
            }
        } else {
            // La respuesta NO fue exitosa (p. ej., 404, 500, etc.)
            val errorBody = response.errorBody()?.string() ?: "Desconocido"
            val errorMessage = "Error en la solicitud: Código ${response.code()}, Mensaje: $errorBody"
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun readOne(id: Long): Result<Pokemon> {
        val response = try {
            api.readOne(id)
        } catch (e: Exception) {
            // Manejar errores de red/conexión
            return Result.failure(e)
        }

        return if (response.isSuccessful) {
            val pokemonResponse = response.body()

            if (pokemonResponse != null) {
                // Éxito: Envolvemos el objeto Pokemon en Result.success
                val pokemon = pokemonResponse.toExternal()
                Result.success(pokemon)
            } else {
                // Respuesta 2xx pero sin cuerpo
                Result.failure(IllegalStateException("Respuesta exitosa, pero el cuerpo del Pokémon con ID $id está vacío."))
            }
        } else {
            // Fallo: Devolvemos Result.failure con información del error HTTP
            val errorBody = response.errorBody()?.string() ?: "Desconocido"
            val errorMessage = "Error al leer Pokémon con ID $id: Código ${response.code()}, Mensaje: $errorBody"
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun addAll(pokemonList: List<Pokemon>) {
        TODO("Not yet implemented")
    }
}

fun PokemonRemote.toExternal():Pokemon{
    return Pokemon(
        id = this.id,
        name = this.name,
        sprite = this.sprites.front_default,
        artwork = "",
    )

}