package com.peterhenell.discman.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peterhenell.discman.data.entities.Course
import com.peterhenell.discman.data.entities.Hole
import com.peterhenell.discman.data.repository.DataStorageService
import com.peterhenell.discman.ui.model.CourseWithHoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val dataStorage: DataStorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState = _uiState.asStateFlow()

    val courses = dataStorage.getAllCourses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadCourse(courseId: Long) {
        _uiState.value = _uiState.value.copy(isEditMode = true, isLoading = true)
        viewModelScope.launch {
            val course = dataStorage.getCourseById(courseId)
            if (course != null) {
                dataStorage.getHolesByCourse(courseId).collect { holes ->
                    _uiState.value = _uiState.value.copy(
                        editingCourse = course,
                        editingHoles = holes.toMutableList(),
                        isLoading = false,
                        isEditMode = true
                    )
                }
            }
        }
    }

    fun startNewCourse() {
        _uiState.value = _uiState.value.copy(
            editingCourse = Course(name = "", location = ""),
            editingHoles = mutableListOf(),
            isEditMode = false,
            isLoading = false
        )
    }

    fun updateCourseName(name: String) {
        _uiState.value = _uiState.value.copy(
            editingCourse = _uiState.value.editingCourse?.copy(name = name)
        )
    }

    fun updateCourseLocation(location: String) {
        _uiState.value = _uiState.value.copy(
            editingCourse = _uiState.value.editingCourse?.copy(location = location)
        )
    }

    fun addHole() {
        val currentHoles = _uiState.value.editingHoles.toMutableList()
        val newHole = Hole(
            courseId = _uiState.value.editingCourse?.courseId ?: 0,
            holeNumber = currentHoles.size + 1,
            par = 3
        )
        currentHoles.add(newHole)
        _uiState.value = _uiState.value.copy(editingHoles = currentHoles)
    }

    fun updateHole(index: Int, hole: Hole) {
        val currentHoles = _uiState.value.editingHoles.toMutableList()
        if (index in currentHoles.indices) {
            currentHoles[index] = hole
            _uiState.value = _uiState.value.copy(editingHoles = currentHoles)
        }
    }

    fun removeHole(index: Int) {
        val currentHoles = _uiState.value.editingHoles.toMutableList()
        if (index in currentHoles.indices) {
            currentHoles.removeAt(index)
            // Update hole numbers
            currentHoles.forEachIndexed { idx, hole ->
                currentHoles[idx] = hole.copy(holeNumber = idx + 1)
            }
            _uiState.value = _uiState.value.copy(editingHoles = currentHoles)
        }
    }

    fun moveHole(from: Int, to: Int) {
        val currentHoles = _uiState.value.editingHoles.toMutableList()
        if (from in currentHoles.indices && to in currentHoles.indices) {
            val hole = currentHoles.removeAt(from)
            currentHoles.add(to, hole)
            // Update hole numbers
            currentHoles.forEachIndexed { idx, hole ->
                currentHoles[idx] = hole.copy(holeNumber = idx + 1)
            }
            _uiState.value = _uiState.value.copy(editingHoles = currentHoles)
        }
    }

    fun saveCourse() {
        viewModelScope.launch {
            val course = _uiState.value.editingCourse
            val holes = _uiState.value.editingHoles

            if (course != null && course.name.isNotBlank()) {
                val courseId = if (course.courseId == 0L) {
                    dataStorage.insertCourse(course)
                } else {
                    dataStorage.updateCourse(course)
                    course.courseId
                }

                // Delete existing holes and insert new ones
                dataStorage.deleteHolesByCourse(courseId)
                val holesWithCourseId = holes.map { it.copy(courseId = courseId) }
                dataStorage.insertHoles(holesWithCourseId)

                clearEditing()
            }
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            dataStorage.deleteCourse(course)
        }
    }

    fun clearEditing() {
        _uiState.value = _uiState.value.copy(
            editingCourse = null,
            editingHoles = mutableListOf(),
            isEditMode = false,
            isLoading = false
        )
    }
}

data class CourseUiState(
    val editingCourse: Course? = null,
    val editingHoles: MutableList<Hole> = mutableListOf(),
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false  // Track if we're editing an existing course
)
