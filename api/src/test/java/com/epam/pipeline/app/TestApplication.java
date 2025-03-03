/*
 * Copyright 2017-2020 EPAM Systems, Inc. (https://www.epam.com/)
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

package com.epam.pipeline.app;

import com.epam.pipeline.common.MessageHelper;
import com.epam.pipeline.dao.monitoring.MonitoringESDao;
import com.epam.pipeline.manager.billing.BillingManager;
import com.epam.pipeline.manager.cloud.CloudFacade;
import com.epam.pipeline.manager.cloud.credentials.CloudProfileCredentialsManager;
import com.epam.pipeline.manager.cloud.credentials.CloudProfileCredentialsManagerProvider;
import com.epam.pipeline.manager.cluster.InstanceOfferScheduler;
import com.epam.pipeline.manager.cluster.performancemonitoring.ESMonitoringManager;
import com.epam.pipeline.manager.cluster.pool.NodePoolUsageService;
import com.epam.pipeline.manager.datastorage.lifecycle.DataStorageLifecycleManager;
import com.epam.pipeline.manager.ldap.LdapManager;
import com.epam.pipeline.manager.notification.ContextualNotificationManager;
import com.epam.pipeline.manager.notification.ContextualNotificationRegistrationManager;
import com.epam.pipeline.manager.notification.ContextualNotificationSettingsManager;
import com.epam.pipeline.manager.ontology.OntologyManager;
import com.epam.pipeline.manager.quota.QuotaService;
import com.epam.pipeline.manager.scheduling.AutowiringSpringBeanJobFactory;
import com.epam.pipeline.manager.user.OnlineUsersService;
import com.epam.pipeline.manager.user.UserRunnersManager;
import com.epam.pipeline.repository.datastorage.lifecycle.DataStorageLifecycleRuleExecutionRepository;
import com.epam.pipeline.repository.datastorage.lifecycle.DataStorageLifecycleRuleRepository;
import com.epam.pipeline.repository.datastorage.lifecycle.DataStorageRestoreActionRepository;
import com.epam.pipeline.repository.run.PipelineRunServiceUrlRepository;
import com.epam.pipeline.repository.user.PipelineUserRepository;
import com.epam.pipeline.security.jwt.JwtTokenGenerator;
import com.epam.pipeline.security.jwt.JwtTokenVerifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.FileNotFoundException;
import java.util.concurrent.Executor;

@SpringBootConfiguration
@Import({AppMVCConfiguration.class,
        DBConfiguration.class,
        CacheConfiguration.class,
        MappersConfiguration.class,
        ContextualPreferenceConfiguration.class,
        BillingConfiguration.class})
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class})
@ComponentScan(
        basePackages = {
                "com.epam.pipeline.dao",
                "com.epam.pipeline.manager",
                "com.epam.pipeline.security",
                "com.epam.pipeline.acl"},
        excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern="com.epam.pipeline.manager.security.acl.*"),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InstanceOfferScheduler.class)})
@TestPropertySource(value={"classpath:test-application.properties"})
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @MockBean // TODO: remove and fix what's wrong
    public MonitoringESDao monitoringESDao;

    @MockBean
    public ESMonitoringManager esMonitoringManager;

    @MockBean
    public CloudFacade cloudFacade;

    @MockBean
    public KeyManager keyManager;

    @MockBean
    public JwtTokenGenerator jwtTokenGenerator;

    @MockBean
    public JwtTokenVerifier jwtTokenVerifier;

    @MockBean
    public Executor dataStoragePathExecutor;

    @MockBean
    public TaskScheduler scheduler;

    @MockBean
    public BillingManager billingManager;

    @MockBean
    public OntologyManager ontologyManager;

    @MockBean
    public ContextualNotificationManager contextualNotificationManager;

    @MockBean
    public ContextualNotificationSettingsManager contextualNotificationSettingsManager;

    @MockBean
    public ContextualNotificationRegistrationManager contextualNotificationRegistrationManager;

    @MockBean
    public CloudProfileCredentialsManagerProvider cloudProfileCredentialsManagerProvider;

    @MockBean
    public CloudProfileCredentialsManager cloudProfileCredentialsManager;

    @MockBean
    public UserRunnersManager mockUserRunnersManager;

    @MockBean
    public PipelineRunServiceUrlRepository pipelineRunServiceUrlRepository;

    @MockBean
    public LdapManager ldapManager;

    @MockBean
    public QuotaService mockQuotaService;

    @MockBean
    public PipelineUserRepository pipelineUserRepository;

    @MockBean
    protected DataStorageLifecycleRuleRepository lifecycleRuleRepository;

    @MockBean
    protected DataStorageLifecycleRuleExecutionRepository lifecycleRuleExecutionRepository;

    @MockBean
    protected DataStorageRestoreActionRepository dataStorageRestoreActionRepository;

    @MockBean
    public OnlineUsersService onlineUsersService;

    @MockBean
    public NodePoolUsageService nodePoolUsageService;

    @MockBean
    public DataStorageLifecycleManager dataStorageLifecycleManager;

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() throws FileNotFoundException {

        return container -> {
            if(container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory containerFactory =
                        (TomcatEmbeddedServletContainerFactory) container;
                containerFactory.addConnectorCustomizers((connector -> connector.setSecure(false)));
            }
        };
    }

    @Bean
    public SidRetrievalStrategy sidRetrievalStrategy() {
        return new SidRetrievalStrategyImpl();
    }

    @Bean
    public MessageHelper messageHelper() {
        return new MessageHelper(messageSource());
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }


    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3); // For PodMonior, AutoscaleManager and ToolScanSceduler
        return scheduler;
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        return new DenyAllPermissionEvaluator();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(final ApplicationContext applicationContext) {
        final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        final AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        schedulerFactory.setJobFactory(jobFactory);
        return schedulerFactory;
    }

}

