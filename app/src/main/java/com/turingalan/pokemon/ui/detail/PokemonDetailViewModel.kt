package com.turingalan.pokemon.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.turingalan.pokemon.data.model.Pokemon
import com.turingalan.pokemon.data.repository.PokemonRepository
import com.turingalan.pokemon.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val name:String = "",
    val artwork:String? = null
)


@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository

): ViewModel() {


    private val _uiState: MutableStateFlow<DetailUiState> =
        MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val route = savedStateHandle.toRoute<Route.Detail>()
            val pokemonId = route.id

            val result: Result<Pokemon> = pokemonRepository.readOne(pokemonId)

            // Extraer Pokemon del Result
            val pokemon: Pokemon? = result.getOrNull()

            // Actualizar el estado si existe
            if (pokemon != null) {
                _uiState.value = pokemon.toDetailUiState()
            } else {
                _uiState.value = DetailUiState(
                    name = "Desconocido",
                    artwork = null
                )
            }
        }
    }

}

fun Pokemon.toDetailUiState(): DetailUiState = DetailUiState(
    name = this.name,
    artwork = this.artwork,
)

