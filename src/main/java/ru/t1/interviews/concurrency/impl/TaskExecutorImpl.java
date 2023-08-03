package ru.t1.interviews.concurrency.impl;

import ru.t1.interviews.concurrency.TaskExecutor;
import ru.t1.interviews.concurrency.TaskGenerator;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class TaskExecutorImpl implements TaskExecutor {
    private final TaskGenerator taskGenerator;

   @Override
    public UUID execute(final UUID taskId, final Boolean fast) {
        return taskGenerator.generate(taskId, fast);
    }
}
