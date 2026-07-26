package com.accessiq.mapper;

import com.accessiq.dto.WorkflowStepRequest;
import com.accessiq.model.WorkflowStepDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WorkflowStepDefinitionMapper {
    WorkflowStepDefinitionMapper INSTANCE = Mappers.getMapper(WorkflowStepDefinitionMapper.class);

    @Mapping(target = "workflow", ignore = true)
    WorkflowStepDefinition toWorkflowStepDefinition(WorkflowStepRequest dto);
}