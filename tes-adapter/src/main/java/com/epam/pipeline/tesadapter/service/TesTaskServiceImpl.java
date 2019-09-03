package com.epam.pipeline.tesadapter.service;

import com.epam.pipeline.entity.datastorage.AbstractDataStorage;
import com.epam.pipeline.entity.pipeline.TaskStatus;
import com.epam.pipeline.tesadapter.entity.TesCancelTaskResponse;
import com.epam.pipeline.tesadapter.entity.TesListTasksResponse;
import com.epam.pipeline.tesadapter.entity.TesServiceInfo;
import com.epam.pipeline.vo.RunStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class TesTaskServiceImpl implements TesTaskService {
    private final CloudPipelineAPIClient cloudPipelineAPIClient;

    @Autowired
    public TesTaskServiceImpl(CloudPipelineAPIClient cloudPipelineAPIClient) {
        this.cloudPipelineAPIClient = cloudPipelineAPIClient;
    }

    @Override
    public TesListTasksResponse listTesTask() {
        return new TesListTasksResponse();
    }

    @Override
    public void stub() {
        //stubbed method
    }

    @Override
    public TesCancelTaskResponse cancelTesTask(String id) {
        RunStatusVO updateStatus = new RunStatusVO();
        updateStatus.setStatus(TaskStatus.STOPPED);
        cloudPipelineAPIClient.updateRunStatus(parseRunId(id), updateStatus);
        return new TesCancelTaskResponse();
    }

    private Long parseRunId(String id) {
        Assert.hasText(id, "INVALID RUN ID");
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.error("INVALID RUN ID");
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public TesServiceInfo getServiceInfo(@Value("${TesTaskServiceImpl.nameOfService}") String nameOfService,
                                         @Value("${TesTaskServiceImpl.doc}") String doc) {
        Stream<TesServiceInfo> tesServiceInfoStream = Stream.of(new TesServiceInfo())
                .peek(t -> t.setName(nameOfService))
                .peek(t -> t.setDoc(doc))
                .peek(t -> t.setStorage(getDataStorage()));
        return tesServiceInfoStream.findFirst().get();
    }

    private List<String> getDataStorage(){
        List<String> listPathDataStorage = new ArrayList<>();
        List<AbstractDataStorage> storageList = cloudPipelineAPIClient.loadAllDataStorages();
        ListUtils.emptyIfNull(storageList);
        storageList.stream().forEach(storage -> listPathDataStorage.add(storage.getPath()));
        return listPathDataStorage;
    }
}
