package lv.rtu.autograderserver.service;

import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TaskRepository taskRepository;

    public TaskService(@NotNull TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> fetchAllByUserId(long userId) {
        return taskRepository.findAllByUserBy(userId);
    }

    public Optional<Task> fetchByTaskIdAndUserId(long taskId, long userId) {
        // If it is superuser then just find it by ID
        if (userId == 0L) {
            return taskRepository.findById(taskId);
        }

        return taskRepository.findTaskByIdAndUserId(taskId, userId);
    }

    public Task saveTask(@NotNull Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(@NotNull Task task) {
        taskRepository.delete(task);
    }

    public Task createNewProblem(@NotNull Task task, @NotNull Problem problem) {
        problem.setTask(task);
        task.getProblems().add(problem);
        return saveTask(task);
    }

    public Task deleteProblem(@NotNull Task task, @NotNull Problem problem) {
        problem.setTask(null);
        task.getProblems().removeIf(v -> v.equals(problem));

        return saveTask(task);
    }
}
