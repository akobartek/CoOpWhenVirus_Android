package pl.pomocnawirus.utils

import pl.pomocnawirus.model.Task

class Filters {

    var selectedTaskStatus = Task.TASK_STATUS_ADDED

    var selectedTaskTypes = arrayListOf(
        Task.TASK_TYPE_SHOPPING, Task.TASK_TYPE_PETS, Task.TASK_TYPE_HOME, Task.TASK_TYPE_OTHER
    )
}