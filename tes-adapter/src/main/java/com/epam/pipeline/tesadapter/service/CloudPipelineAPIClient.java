package com.epam.pipeline.tesadapter.service;

import com.epam.pipeline.client.pipeline.CloudPipelineAPI;
import com.epam.pipeline.client.pipeline.CloudPipelineApiBuilder;
import com.epam.pipeline.entity.cluster.AllowedInstanceAndPriceTypes;
import com.epam.pipeline.entity.datastorage.AbstractDataStorage;
import com.epam.pipeline.entity.pipeline.PipelineRun;
import com.epam.pipeline.entity.pipeline.Tool;
import com.epam.pipeline.entity.pipeline.run.PipelineStart;
import com.epam.pipeline.entity.region.AbstractCloudRegion;
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

    public AllowedInstanceAndPriceTypes loadAllowedInstanceAndPriceTypes(final Long toolId, final Long regionId,
                                                                         final Boolean spot) {
        return QueryUtils.execute(cloudPipelineAPI.loadAllowedInstanceAndPriceTypes(toolId, regionId, spot));
    }

    public Tool loadTool(String image) {
        return QueryUtils.execute(cloudPipelineAPI.loadTool(null, image));
    }

    public List<AbstractCloudRegion> loadAllRegions(){
        return QueryUtils.execute(cloudPipelineAPI.loadAllRegions());
    }

    public List<AbstractDataStorage> loadAllDataStorages(){
        return QueryUtils.execute(cloudPipelineAPI.loadAllDataStorages());
    }
}
