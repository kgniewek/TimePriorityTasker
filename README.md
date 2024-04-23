# TimePriorityTasker

### Project Description
This Android application, developed using Kotlin, helps users manage their tasks effectively with a focus on prioritization and deadlines. It features a multi-screen interface where tasks are categorized by priority levels—High, Medium, and Low. Each task is displayed with a deadline and categorized under different screens based on its priority. The app utilizes the Room Database to store task details, which include the task name, deadline, and priority level. As time progresses, the app dynamically updates to show if a task is expiring soon or has already expired, alongside the number of hours remaining until the deadline or since it has passed. The app simplifies task management with features that allow users to add, delete, and view tasks efficiently. It's designed with a clear and user-friendly UI implemented with Jetpack Compose, promoting a smooth navigation experience across different priority-based task lists.

### Technologies Used
- Platform: Android
- Programming Language: Kotlin
- Database: Room Database (a SQLite abstraction for robust data handling). Utilized for local data persistence, with the AppDatabase class managing the SQLite database and the TaskDao interface providing access to CRUD operations for the TaskEntity.
- Jetpack Compose: Used for building the modern and reactive user interface.
- Architecture Components: LiveData for responsive UI updates, Room for ORM-based database access, and Coroutine for asynchronous data operations.

### Project Overview
The app consists of several screens, including:
- Main Screen: Displays a list of all tasks categorized by priority level. Users can navigate to view tasks with high priority or medium/low priority.
- High Priority Screen: Shows tasks with high priority, allowing users to delete tasks directly from this screen.
- Medium/Low Priority Screen: Displays tasks with medium or low priority, similar to the high priority screen.
- Task Detail Screen: Each task item shows its name, remaining time until the deadline (or if it's expired), and its priority level. Users can delete tasks by clicking the delete icon next to each task.
