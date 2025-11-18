package com.Enigmazer.todo_app.mapper;

import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper class for mapping {@link Task} object to {@link TaskResponseDTO}
 */
@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface TaskMapper {
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "emailEnabled", target = "emailEnabled")
    @Mapping(source = "reminderSent", target = "reminderSent")
    TaskResponseDTO toDto(Task task);
}
