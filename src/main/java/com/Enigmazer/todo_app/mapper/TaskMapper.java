package com.Enigmazer.todo_app.mapper;

import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.model.Task;
import org.mapstruct.Mapper;

/**
 * Mapper class for mapping {@link Task} object to {@link TaskResponseDTO}
 */
@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface TaskMapper {
    TaskResponseDTO toDto(Task task);
}
