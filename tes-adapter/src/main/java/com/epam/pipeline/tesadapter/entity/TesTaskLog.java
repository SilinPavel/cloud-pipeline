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


@ApiModel(description = "TaskLog describes logging information related to a Task.")
@Data
public class TesTaskLog {
    @ApiModelProperty(value = "Logs for each executor")
    @JsonProperty("logs")
    @Valid
    private List<TesExecutorLog> logs = null;

    @ApiModelProperty(value = "Arbitrary logging metadata included by the implementation.")
    @JsonProperty("metadata")
    @Valid
    private Map<String, String> metadata = null;

    @ApiModelProperty(value = "When the task started, in RFC 3339 format.")
    @JsonProperty("start_time")
    private String startTime = null;

    @ApiModelProperty(value = "When the task ended, in RFC 3339 format.")
    @JsonProperty("end_time")
    private String endTime = null;

    @ApiModelProperty(value = "Information about all output files. Directory outputs are flattened into separate items.")
    @JsonProperty("outputs")
    @Valid
    private List<TesOutputFileLog> outputs = null;

    @ApiModelProperty(value = "System logs are any logs the system decides are relevant, which are not tied " +
            "directly to an Executor process. Content is implementation specific: format, size, etc.  " +
            "System logs may be collected here to provide convenient access.  For example, the system may " +
            "include the name of the host where the task is executing, an error message that caused a SYSTEM_ERROR " +
            "state (e.g. disk is full), etc.  System logs are only included in the FULL task view.")
    @JsonProperty("system_logs")
    @Valid
    private List<String> systemLogs = null;

    public TesTaskLog logs(List<TesExecutorLog> logs) {
        this.logs = logs;
        return this;
    }

    public TesTaskLog addLogsItem(TesExecutorLog logsItem) {
        if (this.logs == null) {
            this.logs = new ArrayList<TesExecutorLog>();
        }
        this.logs.add(logsItem);
        return this;
    }

    public TesTaskLog metadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public TesTaskLog putMetadataItem(String key, String metadataItem) {
        if (this.metadata == null) {
            this.metadata = new HashMap<String, String>();
        }
        this.metadata.put(key, metadataItem);
        return this;
    }

    public TesTaskLog startTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public TesTaskLog endTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public TesTaskLog outputs(List<TesOutputFileLog> outputs) {
        this.outputs = outputs;
        return this;
    }

    public TesTaskLog addOutputsItem(TesOutputFileLog outputsItem) {
        if (this.outputs == null) {
            this.outputs = new ArrayList<TesOutputFileLog>();
        }
        this.outputs.add(outputsItem);
        return this;
    }

    public TesTaskLog systemLogs(List<String> systemLogs) {
        this.systemLogs = systemLogs;
        return this;
    }

    public TesTaskLog addSystemLogsItem(String systemLogsItem) {
        if (this.systemLogs == null) {
            this.systemLogs = new ArrayList<String>();
        }
        this.systemLogs.add(systemLogsItem);
        return this;
    }
}

