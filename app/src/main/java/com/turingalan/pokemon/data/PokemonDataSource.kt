package com.turingalan.pokemon.data

import com.turingalan.pokemon.data.model.Pokemon
import kotlinx.coroutines.flow.Flow

/**
 * DataSource modelo de datos comun, formato que todos entiendan
 */
interface PokemonDataSource {
    fun observe(): Flow<Result<List<Pokemon>>>
    suspend fun  readAll(): Result<List<Pokemon>>
    suspend fun  readOne(id:Long): Result<Pokemon>

    suspend fun addAll(pokemonList:List<Pokemon>)
}