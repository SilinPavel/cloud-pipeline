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

package com.epam.pipeline.manager.pipeline;

import com.epam.pipeline.common.MessageHelper;
import com.epam.pipeline.controller.vo.PipelineVO;
import com.epam.pipeline.dao.datastorage.rules.DataStorageRuleDao;
import com.epam.pipeline.dao.pipeline.PipelineDao;
import com.epam.pipeline.dao.pipeline.PipelineRunDao;
import com.epam.pipeline.entity.datastorage.rules.DataStorageRule;
import com.epam.pipeline.entity.git.GitProject;
import com.epam.pipeline.entity.pipeline.Pipeline;
import com.epam.pipeline.entity.pipeline.PipelineRun;
import com.epam.pipeline.entity.pipeline.PipelineType;
import com.epam.pipeline.entity.utils.DateUtils;
import com.epam.pipeline.exception.git.GitClientException;
import com.epam.pipeline.manager.git.GitManager;
import com.epam.pipeline.manager.git.PipelineRepositoryService;
import com.epam.pipeline.manager.metadata.MetadataManager;
import com.epam.pipeline.manager.security.AuthManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.TooManyStaticImports")
public class PipelineManagerTest {

    private static final String REPOSITORY_NAME = "repository";
    private static final String NEW_REPOSITORY_NAME = "new-repository";
    private static final String REPOSITORY_HTTPS = "https://example.com:repository/repository.git";
    private static final String REPOSITORY_SSH = "git@example.com:repository/repository.git";
    private static final String REPOSITORY_TOKEN = "token";
    private static final Long ID = 1L;
    private final GitProject gitProject = new GitProject();

    @Mock
    private GitManager gitManager;

    @Mock
    private PipelineRepositoryService pipelineRepositoryService;

    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private MessageHelper messageHelper;

    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private AuthManager securityManager;

    @Mock
    private PipelineCRUDManager crudManager;

    @Mock
    private PipelineDao pipelineDao;

    @Mock
    private MetadataManager metadataManager;

    @Mock
    private DataStorageRuleDao dataStorageRuleDao;

    @Mock
    private PipelineRunDao pipelineRunDao;

    @InjectMocks
    private PipelineManager pipelineManager = new PipelineManager();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        gitProject.setRepoUrl(REPOSITORY_HTTPS);
        gitProject.setRepoSsh(REPOSITORY_SSH);
        when(gitManager.createRepository(any(), eq(REPOSITORY_NAME), any())).thenReturn(gitProject);
        when(gitManager.createRepository(eq(REPOSITORY_NAME), any())).thenReturn(gitProject);
        when(pipelineRepositoryService.getRepository(any(), eq(REPOSITORY_HTTPS), eq(REPOSITORY_TOKEN)))
                .thenReturn(gitProject);
        when(gitManager.checkProjectExists(eq(REPOSITORY_NAME))).thenReturn(false);
        when(crudManager.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(crudManager.savePipeline(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    }

    @Test
    public void createShouldCreateRepositoryInDefaultGitlabIfThereIsNoRepositoryUrlInVO() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);

        pipelineManager.create(pipelineVO);

        verify(gitManager).createRepository(any(), eq(REPOSITORY_NAME), any());
    }

    @Test
    public void createShouldCreateVersionedStorageRepositoryIfItIsSpecifiedInVO() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);
        pipelineVO.setPipelineType(PipelineType.VERSIONED_STORAGE);
        Pipeline pipeline = pipelineManager.create(pipelineVO);

        verify(gitManager).createRepository(eq(REPOSITORY_NAME), any());
        Assert.assertEquals(pipeline.getPipelineType(), PipelineType.VERSIONED_STORAGE);
    }

    @Test
    public void createShouldCreatePipelineTypeRepositoryIfItIsNotSpecifiedInVO() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);
        Pipeline pipeline = pipelineManager.create(pipelineVO);

        verify(gitManager).createRepository(any(), eq(REPOSITORY_NAME), any());
        Assert.assertEquals(pipeline.getPipelineType(), PipelineType.PIPELINE);
    }

    @Test
    public void createShouldSetRepositoryUrlAndSshIfThereIsNoRepositoryUrlInVO() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);

        final Pipeline pipeline = pipelineManager.create(pipelineVO);

        assertThat(pipeline.getRepository(), is(REPOSITORY_HTTPS));
        assertThat(pipeline.getRepositorySsh(), is(REPOSITORY_SSH));
    }

    @Test
    public void createShouldCreateRepositoryInExternalGitlabIfItDoesNotExists() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);
        pipelineVO.setRepository(REPOSITORY_HTTPS);
        pipelineVO.setRepositoryToken(REPOSITORY_TOKEN);
        when(pipelineRepositoryService.getPipelineRevisions(any(), any()))
                .thenThrow(new IllegalArgumentException(REPOSITORY_NAME));
        when(pipelineRepositoryService.createGitRepositoryWithRepoUrl(pipelineVO)).thenReturn(gitProject);
        when(pipelineRepositoryService.getRepository(any(), eq(REPOSITORY_HTTPS), eq(REPOSITORY_TOKEN)))
                .thenReturn(gitProject);

        pipelineManager.create(pipelineVO);

        verify(pipelineRepositoryService).createGitRepositoryWithRepoUrl(pipelineVO);
    }

    @Test
    public void createShouldCreateVSRepositoryInExternalGitlabIfItDoesNotExists() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);
        pipelineVO.setPipelineType(PipelineType.VERSIONED_STORAGE);
        pipelineVO.setRepository(REPOSITORY_HTTPS);
        pipelineVO.setRepositoryToken(REPOSITORY_TOKEN);
        when(pipelineRepositoryService.getPipelineRevisions(any(), any()))
                .thenThrow(new IllegalArgumentException(REPOSITORY_NAME));
        when(pipelineRepositoryService.createGitRepositoryWithRepoUrl(pipelineVO)).thenReturn(gitProject);

        pipelineManager.create(pipelineVO);

        verify(pipelineRepositoryService).createGitRepositoryWithRepoUrl(pipelineVO);
    }

    @Test
    public void createShouldSetRepositoryUrlAndSshFromExternalGitlabIfItDoesNotExists() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);
        pipelineVO.setRepository(REPOSITORY_HTTPS);
        pipelineVO.setRepositoryToken(REPOSITORY_TOKEN);
        when(pipelineRepositoryService.getPipelineRevisions(any(), any()))
                .thenThrow(new IllegalArgumentException(REPOSITORY_NAME));
        when(pipelineRepositoryService.createGitRepositoryWithRepoUrl(pipelineVO)).thenReturn(gitProject);

        final Pipeline pipeline = pipelineManager.create(pipelineVO);

        assertThat(pipeline.getRepository(), is(REPOSITORY_HTTPS));
        assertThat(pipeline.getRepositorySsh(), is(REPOSITORY_SSH));
    }

    @Test
    public void createShouldNotCreateRepositoryInExternalGitlabIfItAlreadyExists() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);
        pipelineVO.setRepository(REPOSITORY_HTTPS);
        pipelineVO.setRepositoryToken(REPOSITORY_TOKEN);
        when(pipelineRepositoryService.getPipelineRevisions(any(), any())).thenReturn(Collections.emptyList());
        when(pipelineRepositoryService.getRepository(any(), eq(REPOSITORY_HTTPS), eq(REPOSITORY_TOKEN)))
                .thenReturn(gitProject);

        pipelineManager.create(pipelineVO);

        verify(pipelineRepositoryService, times(0)).createGitRepositoryWithRepoUrl(pipelineVO);
    }

    @Test
    public void createShouldSetRepositoryUrlAndSshFromExternalGitlabIfItAlreadyExists() throws GitClientException {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setName(REPOSITORY_NAME);
        pipelineVO.setRepository(REPOSITORY_HTTPS);
        pipelineVO.setRepositoryToken(REPOSITORY_TOKEN);
        when(pipelineRepositoryService.getPipelineRevisions(any(), any())).thenReturn(Collections.emptyList());
        when(pipelineRepositoryService.getRepository(any(), eq(REPOSITORY_HTTPS), eq(REPOSITORY_TOKEN)))
                .thenReturn(gitProject);

        final Pipeline pipeline = pipelineManager.create(pipelineVO);

        assertThat(pipeline.getRepository(), is(REPOSITORY_HTTPS));
        assertThat(pipeline.getRepositorySsh(), is(REPOSITORY_SSH));
    }

    @Test
    public void shouldCopyPipeline() {
        final String sourceRepositoryName = "repository-clone";
        final String newName = sourceRepositoryName + "_copy";
        final String newRepository = "https://example.com:repository/repositoryclone_copy.git";
        final String newRepositorySsh = "git@example.com:repository/repositoryclone_copy.git";
        final String storageRuleMask = "*.test";

        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setId(ID);
        pipelineVO.setName(sourceRepositoryName);
        pipelineVO.setRepository(REPOSITORY_HTTPS);
        pipelineVO.setRepositoryToken(REPOSITORY_TOKEN);
        pipelineVO.setRepositorySsh(REPOSITORY_SSH);

        when(gitManager.copyRepository(any(), any(), any())).thenReturn(null);
        when(metadataManager.hasMetadata(any())).thenReturn(true);
        when(pipelineDao.loadPipeline(ID)).thenReturn(pipelineVO.toPipeline());
        when(dataStorageRuleDao.loadDataStorageRulesForPipeline(ID))
                .thenReturn(Collections.singletonList(buildStorageRule(ID, storageRuleMask)));

        final Pipeline copiedPipeline = pipelineManager.copyPipeline(ID, null, newName);

        assertThat(copiedPipeline.getName(), is(newName));
        assertThat(copiedPipeline.getRepository(), is(newRepository));
        assertThat(copiedPipeline.getRepositorySsh(), is(newRepositorySsh));
        assertThat(copiedPipeline.getDescription(), is(pipelineVO.getDescription()));
        assertThat(copiedPipeline.getParentFolderId(), is(pipelineVO.getParentFolderId()));
        assertThat(copiedPipeline.getRepositoryToken(), is(pipelineVO.getRepositoryToken()));

        final ArgumentCaptor<DataStorageRule> ruleCaptor = ArgumentCaptor.forClass(DataStorageRule.class);
        verify(dataStorageRuleDao).createDataStorageRule(ruleCaptor.capture());
        Assert.assertEquals(storageRuleMask, ruleCaptor.getValue().getFileMask());
    }

    @Test
    public void shouldUpdatePipelineNameForPipelineRuns() {
        final PipelineVO pipelineVO = new PipelineVO();
        pipelineVO.setId(ID);
        pipelineVO.setName(NEW_REPOSITORY_NAME);

        final PipelineRun pipelineRun = new PipelineRun();
        pipelineRun.setPipelineName(REPOSITORY_NAME);
        doReturn(Collections.singletonList(pipelineRun)).when(pipelineRunDao).loadAllRunsForPipeline(any());

        final Pipeline pipeline = new Pipeline();
        pipeline.setName(REPOSITORY_NAME);
        pipeline.setRepository(REPOSITORY_HTTPS);
        pipeline.setRepositoryToken(REPOSITORY_TOKEN);
        pipeline.setRepositorySsh(REPOSITORY_SSH);
        doReturn(pipeline).when(pipelineDao).loadPipeline(any());

        pipelineManager.update(pipelineVO);

        final ArgumentCaptor<List<PipelineRun>> runsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(pipelineRunDao).updateRuns(runsCaptor.capture());
        assertThat(runsCaptor.getValue().size(), is(1));
        assertThat(runsCaptor.getValue().get(0).getPipelineName(), is(NEW_REPOSITORY_NAME));
        verify(pipelineRunDao).loadAllRunsForPipeline(ID);
    }

    private DataStorageRule buildStorageRule(final Long pipelineId, final String mask) {
        final DataStorageRule dataStorageRule = new DataStorageRule();
        dataStorageRule.setPipelineId(pipelineId);
        dataStorageRule.setMoveToSts(true);
        dataStorageRule.setCreatedDate(DateUtils.now());
        dataStorageRule.setFileMask(mask);
        return dataStorageRule;
    }
}
