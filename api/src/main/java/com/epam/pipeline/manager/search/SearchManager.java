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

package com.epam.pipeline.manager.search;

import com.epam.pipeline.controller.vo.search.ElasticSearchRequest;
import com.epam.pipeline.controller.vo.search.FacetedSearchRequest;
import com.epam.pipeline.entity.datastorage.AbstractDataStorage;
import com.epam.pipeline.entity.datastorage.StorageUsage;
import com.epam.pipeline.entity.search.FacetedSearchResult;
import com.epam.pipeline.entity.search.SearchResult;
import com.epam.pipeline.exception.search.SearchException;
import com.epam.pipeline.manager.preference.PreferenceManager;
import com.epam.pipeline.manager.preference.SystemPreferences;
import com.epam.pipeline.manager.utils.GlobalSearchElasticHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchManager {

    private static final String TYPE_AGGREGATION = "by_type";
    
    private final PreferenceManager preferenceManager;
    private final GlobalSearchElasticHelper globalSearchElasticHelper;
    private final SearchResultConverter resultConverter;
    private final SearchRequestBuilder requestBuilder;

    public SearchResult search(final ElasticSearchRequest searchRequest) {
        validateRequest(searchRequest);
        try (RestHighLevelClient client = globalSearchElasticHelper.buildClient()) {
            final String typeFieldName = getTypeFieldName();
            final Set<String> metadataSourceFields =
                    new HashSet<>(ListUtils.emptyIfNull(searchRequest.getMetadataFields()));
            final SearchRequest request = requestBuilder.buildRequest(
                    searchRequest, typeFieldName, TYPE_AGGREGATION, metadataSourceFields);
            final SearchResponse searchResult = client.search(request, RequestOptions.DEFAULT);
            return resultConverter.buildResult(searchResult, TYPE_AGGREGATION, typeFieldName, getAclFilterFields(),
                    metadataSourceFields, searchRequest.getScrollingParameters());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new SearchException(e.getMessage(), e);
        }
    }

    public StorageUsage getStorageUsage(final AbstractDataStorage dataStorage, final String path,
                                        final Set<String> storageSizeMasks) {
        return getStorageUsage(dataStorage, path, false, storageSizeMasks);
    }

    public StorageUsage getStorageUsage(final AbstractDataStorage dataStorage, final String path,
                                        final boolean allowNoIndex, final Set<String> storageSizeMasks) {
        try (RestHighLevelClient client = globalSearchElasticHelper.buildClient()) {
            final MultiSearchRequest request = requestBuilder.buildStorageSumRequest(
                    dataStorage.getId(), dataStorage.getType(), path, allowNoIndex, storageSizeMasks);
            final MultiSearchResponse searchResponse = client.msearch(request, RequestOptions.DEFAULT);
            final int responsesExpected = CollectionUtils.isEmpty(storageSizeMasks) ? 1 : 2;
            return resultConverter.buildStorageUsageResponse(searchResponse, dataStorage, path, responsesExpected);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new SearchException(e.getMessage(), e);
        }
    }

    public FacetedSearchResult facetedSearch(final FacetedSearchRequest searchRequest) {
        Assert.notNull(searchRequest.getPageSize(), "Page Size is required");
        if (Objects.isNull(searchRequest.getScrollingParameters()) && Objects.isNull(searchRequest.getOffset())) {
            searchRequest.setOffset(0);
        }
        try (RestHighLevelClient client = globalSearchElasticHelper.buildClient()) {
            final String typeFieldName = getTypeFieldName();
            final Set<String> metadataSourceFields =
                    new HashSet<>(ListUtils.emptyIfNull(searchRequest.getMetadataFields()));
            final SearchRequest request = requestBuilder.buildFacetedRequest(
                    searchRequest, typeFieldName, metadataSourceFields);
            final SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return resultConverter.buildFacetedResult(response, typeFieldName, getAclFilterFields(),
                    metadataSourceFields, searchRequest.getScrollingParameters());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new SearchException(e.getMessage(), e);
        }
    }

    private Set<String> getAclFilterFields() {
        final Set<String> aclFields = new HashSet<>();
        aclFields.add(preferenceManager.getSystemPreference(
                SystemPreferences.SEARCH_ELASTIC_DENIED_GROUPS_FIELD).getValue());
        aclFields.add(preferenceManager.getSystemPreference(
                SystemPreferences.SEARCH_ELASTIC_DENIED_USERS_FIELD).getValue());
        aclFields.add(preferenceManager.getSystemPreference(
                SystemPreferences.SEARCH_ELASTIC_ALLOWED_GROUPS_FIELD).getValue());
        aclFields.add(preferenceManager.getSystemPreference(
                SystemPreferences.SEARCH_ELASTIC_ALLOWED_USERS_FIELD).getValue());
        return aclFields;
    }

    private void validateRequest(final ElasticSearchRequest request) {
        Assert.isTrue(StringUtils.isNotBlank(request.getQuery()), "Search Query is required");
        Assert.notNull(request.getPageSize(), "Page Size is required");
        if (Objects.isNull(request.getScrollingParameters()) && Objects.isNull(request.getOffset())) {
            request.setOffset(0);
        }
    }

    private String getTypeFieldName() {
        return preferenceManager.getPreference(SystemPreferences.SEARCH_ELASTIC_TYPE_FIELD);
    }
}
