package com.peterhenell.discman.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peterhenell.discman.data.entities.Player
import com.peterhenell.discman.data.repository.DataStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val dataStorage: DataStorageService
) : ViewModel() {

    val players = dataStorage.getAllPlayers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addPlayer(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                dataStorage.insertPlayer(Player(name = name.trim()))
            }
        }
    }

    fun updatePlayer(player: Player) {
        viewModelScope.launch {
            dataStorage.updatePlayer(player)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            dataStorage.deletePlayer(player)
        }
    }
}
