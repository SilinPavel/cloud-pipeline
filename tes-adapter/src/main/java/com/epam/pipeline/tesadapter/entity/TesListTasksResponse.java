package com.epam.pipeline.tesadapter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "ListTasksResponse describes a response from the ListTasks endpoint.")
@Data
public class TesListTasksResponse {
    @ApiModelProperty(value = "List of tasks.")
    @JsonProperty("tasks")
    @Valid
    private List<TesTask> tasks = null;

    @ApiModelProperty(value = "Token used to return the next page of results. See TaskListRequest.next_page_token")
    @JsonProperty("next_page_token")
    private String nextPageToken = null;

    public TesListTasksResponse tasks(List<TesTask> tasks) {
        this.tasks = tasks;
        return this;
    }

    public TesListTasksResponse addTasksItem(TesTask tasksItem) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<TesTask>();
        }
        this.tasks.add(tasksItem);
        return this;
    }

    public TesListTasksResponse nextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
        return this;
    }
}

