/*
 * Copyright 2017-2021 EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.pipeline.dts.sync.service.impl;

import com.epam.pipeline.dts.common.service.CloudPipelineAPIClient;
import com.epam.pipeline.dts.common.service.IdentificationService;
import com.epam.pipeline.dts.sync.service.PreferenceService;
import com.epam.pipeline.dts.sync.model.AutonomousSyncRule;
import com.epam.pipeline.entity.dts.submission.DtsRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@EnableScheduling
@Slf4j
public class CloudPipelineApiPreferenceService implements PreferenceService {

    private final CloudPipelineAPIClient apiClient;
    private final ConcurrentMap<String, String> preferences;
    private final IdentificationService identificationService;
    private final String dtsShutdownKey;
    private final String dtsSyncRulesKey;
    private final String dtsHeartbeatEnabledKey;
    private final String dtsSourceDeletionEnabledKey;

    @Autowired
    public CloudPipelineApiPreferenceService(
            @Value("${dts.preference.shutdown.key:dts.restart.force}")
            final String dtsShutdownKey,
            @Value("${dts.preference.sync.rules.key:dts.local.sync.rules}")
            final String dtsSyncRulesKey,
            @Value("${dts.preference.heartbeat.enabled.key:dts.heartbeat.enabled}")
            final String dtsHeartbeatEnabledKey,
            @Value("${dts.preference.source.deletion.enabled.key:dts.source.deletion.enabled}")
            final String dtsSourceDeletionEnabledKey,
            final CloudPipelineAPIClient apiClient,
            final IdentificationService identificationService) {
        this.apiClient = apiClient;
        this.identificationService = identificationService;
        this.preferences = new ConcurrentHashMap<>();
        this.dtsShutdownKey = dtsShutdownKey;
        this.dtsSyncRulesKey = dtsSyncRulesKey;
        this.dtsHeartbeatEnabledKey = dtsHeartbeatEnabledKey;
        this.dtsSourceDeletionEnabledKey = dtsSourceDeletionEnabledKey;
        log.info("Synchronizing preferences for current host: `{}`", identificationService.getId());
    }

    @Scheduled(fixedDelayString = "${dts.sync.poll:60000}")
    public void synchronizePreferences() {
        final Map<String, String> updatedPreferences = Optional.of(identificationService.getId())
            .flatMap(apiClient::findDtsRegistryByNameOrId)
            .map(DtsRegistry::getPreferences)
            .orElse(Collections.emptyMap());
        log.info("Following preferences received during sync iteration: {}", updatedPreferences.toString());
        preferences.clear();
        preferences.putAll(updatedPreferences);
    }

    @Override
    public Optional<List<AutonomousSyncRule>> getSyncRules() {
        final String rulesAsString = preferences.getOrDefault(dtsSyncRulesKey, "[]");
        try {
            return Optional.of(new ObjectMapper()
                                   .readValue(rulesAsString, new TypeReference<List<AutonomousSyncRule>>() {}));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isShutdownRequired() {
        return getBooleanPreference(dtsShutdownKey);
    }

    @Override
    public boolean isHeartbeatEnabled() {
        return getBooleanPreference(dtsHeartbeatEnabledKey);
    }

    @Override
    public boolean isSourceDeletionEnabled() {
        return getBooleanPreference(dtsSourceDeletionEnabledKey);
    }

    private boolean getBooleanPreference(final String preference) {
        return Optional.of(preference)
                .map(preferences::get)
                .map(BooleanUtils::toBoolean)
                .orElse(false);
    }

    @Override
    public void clearShutdownFlag() {
        log.info("Clear flag of remote shutdown `{}` for DTS registry `{}`",
                dtsShutdownKey, identificationService.getId());
        apiClient.deleteDtsRegistryPreferences(identificationService.getId(),
                Collections.singletonList(dtsShutdownKey));
    }
}
