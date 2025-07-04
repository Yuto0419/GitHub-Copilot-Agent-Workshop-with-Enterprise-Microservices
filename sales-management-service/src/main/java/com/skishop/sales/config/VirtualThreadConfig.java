package com.skishop.sales.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Java 21 Virtual Threads Configuration
 * Enables Virtual Threads for improved performance
 */
@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * Virtual Threads-based TaskExecutor
     * Achieves high scalability using Java 21 lightweight threads
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Custom Executor using Virtual Threads
     * For asynchronous processing only
     */
    @Bean("virtualThreadTaskExecutor")
    public AsyncTaskExecutor virtualThreadTaskExecutor() {
        return new TaskExecutorAdapter(
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                .name("virtual-task-", 0)
                .factory())
        );
    }
}
