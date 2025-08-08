package com.peterhenell.discman.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.peterhenell.discman.data.entities.Hole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditScreen(
    navController: NavController,
    viewModel: CourseViewModel = hiltViewModel(),
    courseId: Long? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val course = uiState.editingCourse
    val holes = uiState.editingHoles

    // Initialize based on courseId parameter
    LaunchedEffect(courseId) {
        if (courseId != null) {
            // Edit existing course
            viewModel.loadCourse(courseId)
        } else {
            // Create new course
            viewModel.startNewCourse()
        }
    }

    // Show loading while waiting for course data
    if (course == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar with actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                viewModel.clearEditing()
                navController.popBackStack()
            }) {
                Text("Cancel")
            }

            Text(
                text = if (course.courseId == 0L) "Add Course" else "Edit Course",
                style = MaterialTheme.typography.headlineSmall
            )

            TextButton(
                onClick = {
                    viewModel.saveCourse()
                    navController.popBackStack()
                }
            ) {
                Text("Done")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Course details
        OutlinedTextField(
            value = course.name,
            onValueChange = { viewModel.updateCourseName(it) },
            label = { Text("Course Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = course.location,
            onValueChange = { viewModel.updateCourseLocation(it) },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Holes section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Holes (${holes.size})",
                style = MaterialTheme.typography.titleMedium
            )

            Button(onClick = { viewModel.addHole() }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Hole")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(holes) { index, hole ->
                    HoleEditItem(
                        hole = hole,
                        onUpdate = { updatedHole -> viewModel.updateHole(index, updatedHole) },
                        onRemove = { viewModel.removeHole(index) },
                        canMoveUp = index > 0,
                        canMoveDown = index < holes.size - 1,
                        onMoveUp = { viewModel.moveHole(index, index - 1) },
                        onMoveDown = { viewModel.moveHole(index, index + 1) }
                    )
                }
            }

            // Custom scrollbar indicator
            if (holes.size > 1) {
                val firstVisibleIndex by remember {
                    derivedStateOf { listState.firstVisibleItemIndex }
                }
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val visibleItems = layoutInfo.visibleItemsInfo.size

                // Only show scrollbar if there are items that extend beyond the visible area
                if (totalItems > visibleItems) {
                    val scrollProgress = if (totalItems > 1) {
                        firstVisibleIndex.toFloat() / (totalItems - visibleItems).coerceAtLeast(1)
                    } else 0f

                    // Scrollbar track
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(4.dp)
                            .padding(vertical = 8.dp)
                            .background(
                                Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    )

                    // Scrollbar thumb
                    val thumbHeight = 0.3f // 30% of track height
                    val thumbOffset = scrollProgress * (1f - thumbHeight)

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(thumbHeight)
                            .width(4.dp)
                            .padding(horizontal = 0.dp)
                            .offset(y = (thumbOffset * 200).dp) // Approximate offset calculation
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun HoleEditItem(
    hole: Hole,
    onUpdate: (Hole) -> Unit,
    onRemove: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hole ${hole.holeNumber}",
                    style = MaterialTheme.typography.titleSmall
                )

                Row {
                    if (canMoveUp) {
                        IconButton(onClick = onMoveUp) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up")
                        }
                    }
                    if (canMoveDown) {
                        IconButton(onClick = onMoveDown) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down")
                        }
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Hole")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Par adjustment
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Par:", modifier = Modifier.width(60.dp))
                IconButton(
                    onClick = { if (hole.par > 1) onUpdate(hole.copy(par = hole.par - 1)) }
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Par")
                }
                Text(
                    text = hole.par.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(40.dp)
                )
                IconButton(
                    onClick = { onUpdate(hole.copy(par = hole.par + 1)) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Par")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Distance field
            OutlinedTextField(
                value = hole.distance?.toString() ?: "",
                onValueChange = { value ->
                    val distance = value.toIntOrNull()
                    onUpdate(hole.copy(distance = distance))
                },
                label = { Text("Distance (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description field
            OutlinedTextField(
                value = hole.description ?: "",
                onValueChange = { onUpdate(hole.copy(description = it.ifBlank { null })) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
