package com.Enigmazer.todo_app.scheduler;

import com.Enigmazer.todo_app.model.Task;
import com.Enigmazer.todo_app.repository.TaskRepository;
import com.Enigmazer.todo_app.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled task that sends email reminders for upcoming tasks.
 * This component runs periodically to check for tasks that are due within the next hour
 * and sends email notifications to users who have enabled reminders.
 */
@Component
@RequiredArgsConstructor
public class TaskReminderScheduler {

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    /**
     * Scheduled task that runs every 5 minutes to check for and send task reminders.
     * This method:
     * <ul>
     *     <li>Finds all the remindable tasks which are due within the next hour</li>
     *     <li>Sends an HTML email reminder for each matching task</li>
     *     <li>Marks the task as having a reminder sent to prevent duplicate notifications</li>
     * </ul>
     *
     * @throws MessagingException if there is an error sending the email notification
     */
    @Transactional
    @Scheduled(fixedRate = 300000) // Runs every 5 minutes (300,000 milliseconds)
    public void sendReminders() throws MessagingException {
        Instant now = Instant.now();
        Instant oneHourFromNow = now.plusSeconds(60 * 60);

        List<Task> tasks = taskRepository.findRemindableTasks(now, oneHourFromNow);

        for (Task task : tasks) {
            // Send email notification
            emailService.sendTaskReminder(
                    task.getUser().getEmail(),
                    task.getUser().getName(),
                    "Task Reminder: " + task.getTitle(),
                    task.getTitle()
            );
                
            // Mark reminder as sent to prevent duplicates
            task.setReminderSent(true);
            taskRepository.save(task);
        }
    }
}
