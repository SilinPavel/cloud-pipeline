package com.epam.pipeline.tesadapter.controller;

import com.epam.pipeline.tesadapter.entity.TesCancelTaskResponse;
import com.epam.pipeline.tesadapter.entity.TesCreateTaskResponse;
import com.epam.pipeline.tesadapter.entity.TesTask;
import com.epam.pipeline.tesadapter.service.TesApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class TesApiController implements TesApiService {

    private final ObjectMapper objectMapper;
    private final TesApiService tesApiService;

    @Autowired
    public TesApiController(TesApiService tesApiService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.tesApiService = tesApiService;
    }


    @Override
    public ResponseEntity<TesCreateTaskResponse> submitTesTask(TesTask body) {
        try {
            return new ResponseEntity<TesCreateTaskResponse>(objectMapper.readValue("STUBBED",
                    TesCreateTaskResponse.class), HttpStatus.NOT_IMPLEMENTED);
        } catch (IOException e) {
            log.error("STUBBED");
            return new ResponseEntity<TesCreateTaskResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<TesTask> getTesTask(String id) {
        try {
            return new ResponseEntity<TesTask>(objectMapper.readValue("STUBBED", TesTask.class),
                    HttpStatus.NOT_IMPLEMENTED);
        } catch (IOException e) {
            log.error("STUBBED");

            return new ResponseEntity<TesTask>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<TesCancelTaskResponse> cancelTesTask(String id) {
        try {
            return new ResponseEntity<TesCancelTaskResponse>(objectMapper.readValue("",
                    TesCancelTaskResponse.class), HttpStatus.NOT_IMPLEMENTED);
        } catch (IOException e) {
            log.error("STUBBED");
            return new ResponseEntity<TesCancelTaskResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
