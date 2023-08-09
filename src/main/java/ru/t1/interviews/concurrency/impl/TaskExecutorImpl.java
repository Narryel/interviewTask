package ru.t1.interviews.concurrency.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.t1.interviews.concurrency.TaskExecutor;
import ru.t1.interviews.concurrency.TaskGenerator;

import java.util.HashMap;
import java.util.UUID;

@RequiredArgsConstructor
public class TaskExecutorImpl implements TaskExecutor {
    private final TaskGenerator taskGenerator;
    private final HashMap<UUID, UUID> taskIdToResultMap = new HashMap<>();

    @Override
    public UUID execute(final UUID taskId, final Boolean fast) {
        return readOrComputeAndCacheValue(taskId, fast);
    }

    private synchronized UUID readOrComputeAndCacheValue(UUID taskId, boolean fast) {
        var result = taskIdToResultMap.get(taskId);
        if (result == null) {
            val generated = taskGenerator.generate(taskId, fast);
            taskIdToResultMap.put(taskId, generated);
        }
        return result;
    }
}
