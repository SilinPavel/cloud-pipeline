package com.epam.pipeline.tesadapter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ApiModel(description = "Task describes an instance of a task.")
@Data
public class TesTask {
    @ApiModelProperty(value = "Task identifier assigned by the server.")
    @JsonProperty("id")
    private String id = null;

    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("state")
    private TesState state = null;

    @ApiModelProperty(value = "")
    @JsonProperty("name")
    private String name = null;

    @ApiModelProperty(value = "")
    @JsonProperty("description")
    private String description = null;

    @ApiModelProperty(value = "Input files. Inputs will be downloaded and mounted into the executor container.")
    @JsonProperty("inputs")
    @Valid
    private List<TesInput> inputs = null;

    @ApiModelProperty(value = "Output files. Outputs will be uploaded from the executor container to long-term storage.")
    @JsonProperty("outputs")
    @Valid
    private List<TesOutput> outputs = null;

    @ApiModelProperty(value = "Request that the task be run with these resources.")
    @JsonProperty("resources")
    @Valid
    private TesResources resources = null;

    @ApiModelProperty(value = "A list of executors to be run, sequentially. Execution stops on the first error.")
    @JsonProperty("executors")
    @Valid
    private List<TesExecutor> executors = null;

    @ApiModelProperty(value = "Volumes are directories which may be used to share data between Executors. " +
            "Volumes are initialized as empty directories by the system when the task starts and are mounted " +
            "at the same path in each Executor.  For example, given a volume defined at \"/vol/A\", executor 1" +
            " may write a file to \"/vol/A/exec1.out.txt\", then executor 2 may read from that file.  " +
            "(Essentially, this translates to a `docker run -v` flag where the container path is the same " +
            "for each executor).")
    @JsonProperty("volumes")
    @Valid
    private List<String> volumes = null;

    @ApiModelProperty(value = "A key-value map of arbitrary tags.")
    @JsonProperty("tags")
    @Valid
    private Map<String, String> tags = null;
    @ApiModelProperty(value = "Task logging information. Normally, this will contain only one entry, but in the" +
            " case where a task fails and is retried, an entry will be appended to this list.")
    @JsonProperty("logs")
    @Valid
    private List<TesTaskLog> logs = null;

    @ApiModelProperty(value = "Date + time the task was created, in RFC 3339 format. This is set by the system, " +
            "not the client.")
    @JsonProperty("creation_time")
    private String creationTime = null;

    public TesTask id(String id) {
        this.id = id;
        return this;
    }

    public TesTask state(TesState state) {
        this.state = state;
        return this;
    }

    public TesTask name(String name) {
        this.name = name;
        return this;
    }

    public TesTask description(String description) {
        this.description = description;
        return this;
    }

    public TesTask inputs(List<TesInput> inputs) {
        this.inputs = inputs;
        return this;
    }

    public TesTask addInputsItem(TesInput inputsItem) {
        if (this.inputs == null) {
            this.inputs = new ArrayList<TesInput>();
        }
        this.inputs.add(inputsItem);
        return this;
    }

    public TesTask outputs(List<TesOutput> outputs) {
        this.outputs = outputs;
        return this;
    }

    public TesTask addOutputsItem(TesOutput outputsItem) {
        if (this.outputs == null) {
            this.outputs = new ArrayList<TesOutput>();
        }
        this.outputs.add(outputsItem);
        return this;
    }

    public TesTask resources(TesResources resources) {
        this.resources = resources;
        return this;
    }

    public TesTask executors(List<TesExecutor> executors) {
        this.executors = executors;
        return this;
    }

    public TesTask addExecutorsItem(TesExecutor executorsItem) {
        if (this.executors == null) {
            this.executors = new ArrayList<TesExecutor>();
        }
        this.executors.add(executorsItem);
        return this;
    }

    public TesTask volumes(List<String> volumes) {
        this.volumes = volumes;
        return this;
    }

    public TesTask addVolumesItem(String volumesItem) {
        if (this.volumes == null) {
            this.volumes = new ArrayList<String>();
        }
        this.volumes.add(volumesItem);
        return this;
    }

    public TesTask tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public TesTask putTagsItem(String key, String tagsItem) {
        if (this.tags == null) {
            this.tags = new HashMap<String, String>();
        }
        this.tags.put(key, tagsItem);
        return this;
    }

    public TesTask logs(List<TesTaskLog> logs) {
        this.logs = logs;
        return this;
    }

    public TesTask addLogsItem(TesTaskLog logsItem) {
        if (this.logs == null) {
            this.logs = new ArrayList<TesTaskLog>();
        }
        this.logs.add(logsItem);
        return this;
    }

    public TesTask creationTime(String creationTime) {
        this.creationTime = creationTime;
        return this;
    }
}

