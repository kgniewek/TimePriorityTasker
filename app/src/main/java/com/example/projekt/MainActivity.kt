package com.example.projekt

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import com.example.projekt.ui.theme.ProjektTheme
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import java.util.*
import androidx.room.*
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.withContext


@Entity
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val deadline: Long,
    val priority: String
)
@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM TaskEntity")
    fun getAllTasks(): LiveData<List<TaskEntity>>
}



@Database(entities = [TaskEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "task-database").build()
        taskDao = db.taskDao()

        setContent {
            ProjektTheme {
                AppNavigation(taskDao)
            }
        }
    }
}

@Composable
fun AppNavigation(taskDao: TaskDao) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(taskDao, navController)
        }
        composable("highPriority") {
            HighPriorityScreen(taskDao, navController)
        }
        composable("mediumLowPriority") {
            MediumLowPriorityScreen(taskDao, navController)
        }
    }
}



@Composable
fun MainScreen(taskDao: TaskDao, navController: NavController) {
    ToDoListScreen(taskDao)

    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(onClick = { navController.navigate("highPriority") }) {
            Text(stringResource(R.string.view_high_priority))
        }
        Button(onClick = { navController.navigate("mediumLowPriority") }) {
            Text(stringResource(R.string.view_medium_low_priority))
        }
    }
}



@Composable
fun HighPriorityScreen(taskDao: TaskDao, navController: NavController) {
    val highPriorityTasks = taskDao.getAllTasks().observeAsState(listOf()).value
        .filter { it.priority == TaskPriority.HIGH.name }

    Column {
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { navController.navigate("main") }) {
                Text(stringResource(R.string.view_main))
            }
            Button(onClick = { navController.navigate("mediumLowPriority") }) {
                Text(stringResource(R.string.view_medium_low_priority))
            }
        }
        LazyColumn {
            items(highPriorityTasks) { taskEntity ->
                TaskItem(taskEntity = taskEntity, taskDao = taskDao, onDelete = {
                    CoroutineScope(Dispatchers.IO).launch {
                        taskDao.deleteTask(taskEntity)
                    }
                })
            }
        }

    }
}

@Composable
fun MediumLowPriorityScreen(taskDao: TaskDao, navController: NavController) {
    val mediumLowPriorityTasks = taskDao.getAllTasks().observeAsState(listOf()).value
        .filter { it.priority != TaskPriority.HIGH.name }

    Column {
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { navController.navigate("main") }) {
                Text(stringResource(R.string.view_main))
            }
            Button(onClick = { navController.navigate("highPriority") }) {
                Text(stringResource(R.string.view_high_priority))
            }
        }
        LazyColumn {
            items(mediumLowPriorityTasks) { taskEntity ->
                TaskItem(taskEntity = taskEntity, taskDao = taskDao, onDelete = {
                    CoroutineScope(Dispatchers.IO).launch {
                        taskDao.deleteTask(taskEntity)
                    }
                })
            }
        }

    }
}



@Composable
fun ToDoListScreen(taskDao: TaskDao) {
    val context = LocalContext.current
    var newTask by remember { mutableStateOf("") }
    var deadlineInHours by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.LOW) }
    val allTasks = taskDao.getAllTasks().observeAsState(listOf())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = newTask,
            onValueChange = { newTask = it },
            label = { Text(stringResource(id = R.string.new_task_hint)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = deadlineInHours,
            onValueChange = { deadlineInHours = it },
            label = { Text(stringResource(id = R.string.deadline_in_hours)) },
            modifier = Modifier.fillMaxWidth()
        )

        PrioritySelector(selectedPriority) { selectedPriority = it }

        Button(
            onClick = {
                if (newTask.isNotBlank() && deadlineInHours.isNotBlank()) {
                    val deadline = Calendar.getInstance().apply {
                        add(Calendar.HOUR_OF_DAY, deadlineInHours.toInt())
                    }
                    val taskEntity = TaskEntity(
                        name = newTask,
                        deadline = deadline.timeInMillis,
                        priority = selectedPriority.name
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        taskDao.insertTask(taskEntity)
                    }
                    newTask = ""
                    deadlineInHours = ""
                    selectedPriority = TaskPriority.LOW
                    Toast.makeText(context, context.getString(R.string.toast_task_added), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(id = R.string.add_task))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(allTasks.value) { taskEntity ->
                TaskItem(taskEntity = taskEntity, taskDao = taskDao, onDelete = {
                    CoroutineScope(Dispatchers.IO).launch {
                        taskDao.deleteTask(taskEntity)
                    }
                })
            }
        }
    }
}

@Composable
fun PrioritySelector(selectedPriority: TaskPriority, onPrioritySelected: (TaskPriority) -> Unit) {
    Column {
        TaskPriority.values().forEach { priority ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = priority == selectedPriority,
                    onClick = { onPrioritySelected(priority) }
                )
                Text(text = stringResource(id = when(priority) {
                    TaskPriority.HIGH -> R.string.priority_high
                    TaskPriority.MEDIUM -> R.string.priority_medium
                    TaskPriority.LOW -> R.string.priority_low
                }))
            }
        }
    }
}
@Composable
fun TaskItem(taskEntity: TaskEntity, taskDao: TaskDao, onDelete: () -> Unit) {
    val context = LocalContext.current
    val (resId, formatArgs) = formatTimeLeft(taskEntity.deadline)
    val priorityString = stringResource(id = when(TaskPriority.valueOf(taskEntity.priority)) {
        TaskPriority.HIGH -> R.string.priority_high
        TaskPriority.MEDIUM -> R.string.priority_medium
        TaskPriority.LOW -> R.string.priority_low
    })

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = taskEntity.name)
            Text(text = stringResource(id = resId, formatArgs = formatArgs) + " - " + priorityString)
        }
        IconButton(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                taskDao.deleteTask(taskEntity)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.toast_task_removed), Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Task")
        }
    }
}

fun formatTimeLeft(deadlineInMillis: Long): Pair<Int, Array<Any>> {
    val now = Calendar.getInstance().timeInMillis
    val minutesDifference = (deadlineInMillis - now) / (1000 * 60)

    return when {
        minutesDifference > 60 -> R.string.hours_left to arrayOf(minutesDifference / 60)
        minutesDifference in 1 until 60 -> R.string.expiring_soon to emptyArray()
        minutesDifference == 0L -> R.string.expiring_soon to emptyArray()
        else -> {
            val hoursExpired = Math.abs(minutesDifference / 60)
            if (hoursExpired == 0L) {
                R.string.expired_just_now to emptyArray()
            } else {
                R.string.expired_hours_ago to arrayOf(hoursExpired)
            }
        }
    }
}







@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProjektTheme {
        // ToDoListScreen(mockTaskDao)
    }
}