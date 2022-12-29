package lv.rtu.autograderserver.service;

import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.ProblemFile;
import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TaskRepository taskRepository;
    private final SandboxConfigurator sandboxConfigurator;

    public TaskService(@NotNull TaskRepository taskRepository, @NotNull SandboxConfigurator sandboxConfigurator) {
        this.taskRepository = taskRepository;
        this.sandboxConfigurator = sandboxConfigurator;
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

    public Task createNewPublication(@NotNull Task task, @NotNull Publication publication) {
        publication.setTask(task);
        task.getPublications().add(publication);

        return saveTask(task);
    }

    public Task createNewProblem(@NotNull Task task, @NotNull Problem problem) throws IOException {
        prepareInitialFiles(problem);

        problem.setTask(task);
        task.getProblems().add(problem);

        return saveTask(task);
    }

    public Task deleteProblem(@NotNull Task task, @NotNull Problem problem) {
        problem.setTask(null);
        task.getProblems().removeIf(v -> v.equals(problem));

        return saveTask(task);
    }

    public Task deletePublication(@NotNull Task task, @NotNull Publication publication) {
        publication.setTask(null);
        task.getPublications().removeIf(v -> v.equals(publication));

        return saveTask(task);
    }

    private void prepareInitialFiles(Problem problem) throws IOException {
        Map<String, String> files = sandboxConfigurator.readInitialFiles(problem.getSandboxType());
        files.forEach((name, content) -> {
            ProblemFile problemFile = new ProblemFile(problem);
            problemFile.setFileName(name);
            problemFile.setContent(content);

            problem.getFiles().add(problemFile);
        });
    }
}
