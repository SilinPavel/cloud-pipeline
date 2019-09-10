package com.epam.pipeline.tesadapter.service;

import com.epam.pipeline.client.pipeline.CloudPipelineAPI;
import com.epam.pipeline.client.pipeline.CloudPipelineApiBuilder;
import com.epam.pipeline.entity.datastorage.AbstractDataStorage;
import com.epam.pipeline.entity.pipeline.PipelineRun;
import com.epam.pipeline.entity.pipeline.RunLog;
import com.epam.pipeline.entity.pipeline.run.PipelineStart;
import com.epam.pipeline.utils.QueryUtils;
import com.epam.pipeline.vo.RunStatusVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CloudPipelineAPIClient {
    private CloudPipelineAPI cloudPipelineAPI;

    public CloudPipelineAPIClient(@Value("${cloud.pipeline.host}") String cloudPipelineHostUrl,
                                  @Value("${cloud.pipeline.token}") String cloudPipelineToken) {
        this.cloudPipelineAPI =
                new CloudPipelineApiBuilder(0, 0, cloudPipelineHostUrl, cloudPipelineToken)
                        .buildClient();
    }

    public PipelineRun runPipeline(PipelineStart runVo) {
        return QueryUtils.execute(cloudPipelineAPI.runPipeline(runVo));
    }

    public PipelineRun loadPipelineRun(final Long pipelineRunId) {
        return QueryUtils.execute(cloudPipelineAPI.loadPipelineRun(pipelineRunId));
    }

    public PipelineRun updateRunStatus(final Long pipelineRunId, RunStatusVO statusUpdate) {
        return QueryUtils.execute(cloudPipelineAPI.updateRunStatus(pipelineRunId, statusUpdate));
    }

    public List<AbstractDataStorage> loadAllDataStorages(){
        return QueryUtils.execute(cloudPipelineAPI.loadAllDataStorages());
    }

    public List<RunLog> getRunLog(final Long runId){
        return QueryUtils.execute(cloudPipelineAPI.loadLogs(runId));
    }


}
