/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.gateway.services;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.ConfigurableEnvironment;
import org.zowe.apiml.auth.AuthenticationScheme;
import org.zowe.apiml.config.ApiInfo;
import org.zowe.apiml.eurekaservice.client.util.EurekaMetadataParser;
import org.zowe.apiml.gateway.config.GatewayConfig;
import org.zowe.apiml.product.gateway.GatewayClient;
import org.zowe.apiml.product.gateway.GatewayConfigProperties;
import org.zowe.apiml.product.routing.transform.TransformService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.zowe.apiml.constants.EurekaMetadataDefinition.*;

@ExtendWith(MockitoExtension.class)
class ServicesInfoServiceTest {

    // Gateway configuration
    private static final String GW_HOSTNAME = "gateway";
    private static final String GW_PORT = "10010";
    private static final String GW_SCHEME = "https";
    private static final String GW_BASE_URL = GW_SCHEME + "://" + GW_HOSTNAME + ":" + GW_PORT;

    // Client test configuration
    private final static String CLIENT_SERVICE_ID = "testclient";
    private final static String CLIENT_INSTANCE_ID = "testclient:10";
    private final static String CLIENT_HOSTNAME = "client";
    private final static String CLIENT_IP = "192.168.0.1";
    private static final int CLIENT_PORT = 10;
    private static final String CLIENT_SCHEME = "https";
    private static final String CLIENT_HOMEPAGE = "https://client:10";
    private static final String CLIENT_RELATIVE_HEALTH_URL = "/actuator/health";
    private static final String CLIENT_STATUS_URL = "https://client:10/actuator/info";
    private static final String CLIENT_API_ID = "zowe.client.api";
    private static final String CLIENT_API_VERSION = "1.0.0";
    private static final String CLIENT_API_GW_URL = "api/v1";
    private static final boolean CLIENT_API_DEFAULT = true;
    private static final String CLIENT_API_SWAGGER_URL = CLIENT_HOMEPAGE + "/apiDoc";
    private static final String CLIENT_API_DOC_URL = "https://www.zowe.org";
    private static final String CLIENT_API_BASE_PATH = "/" + CLIENT_SERVICE_ID + "/" + CLIENT_API_GW_URL;
    private static final String CLIENT_API_BASE_URL = GW_BASE_URL + CLIENT_API_BASE_PATH;
    private static final String CLIENT_ROUTE_UI = "ui/v1";
    private static final String CLIENT_SERVICE_TITLE = "Client service";
    private static final String CLIENT_SERVICE_DESCRIPTION = "Client test service";
    private static final String CLIENT_SERVICE_HOMEPAGE = GW_BASE_URL + "/" + CLIENT_SERVICE_ID + "/" + CLIENT_ROUTE_UI;
    private static final AuthenticationScheme CLIENT_AUTHENTICATION_SCHEME = AuthenticationScheme.ZOSMF;
    private static final String CLIENT_AUTHENTICATION_APPLID = "authid";
    private static final boolean CLIENT_AUTHENTICATION_SSO = true;

    @Mock
    private EurekaClient eurekaClient;

    @Mock
    ConfigurableEnvironment env;

    private final GatewayConfigProperties gatewayConfigProperties = new GatewayConfig(env)
            .getGatewayConfigProperties(GW_HOSTNAME, GW_PORT, GW_SCHEME);
    private final EurekaMetadataParser eurekaMetadataParser = new EurekaMetadataParser();
    private final TransformService transformService = new TransformService(new GatewayClient(gatewayConfigProperties));

    private ServicesInfoService servicesInfoService;

    @BeforeEach
    void setUp() {
        servicesInfoService = new ServicesInfoService(eurekaClient, gatewayConfigProperties, eurekaMetadataParser, transformService);
    }

    @Test
    void whenInstanceProvidesFullInfo_thenReturnAllDetails() {
        when(eurekaClient.getApplication(CLIENT_SERVICE_ID))
                .thenReturn(new Application(CLIENT_SERVICE_ID, Collections.singletonList(createFullTestInstance())));

        ServiceInfo serviceInfo = servicesInfoService.getServiceInfo(CLIENT_SERVICE_ID);

        assertEquals(CLIENT_SERVICE_ID, serviceInfo.getServiceId());
        assertEquals(InstanceInfo.InstanceStatus.UP, serviceInfo.getStatus());

        assertEquals(CLIENT_API_ID, serviceInfo.getApiml().getApiInfo().get(0).getApiId());
        assertEquals(CLIENT_API_VERSION, serviceInfo.getApiml().getApiInfo().get(0).getVersion());
        assertEquals(CLIENT_API_GW_URL, serviceInfo.getApiml().getApiInfo().get(0).getGatewayUrl());
        assertEquals(CLIENT_API_SWAGGER_URL, serviceInfo.getApiml().getApiInfo().get(0).getSwaggerUrl());
        assertEquals(CLIENT_API_DOC_URL, serviceInfo.getApiml().getApiInfo().get(0).getDocumentationUrl());
        assertEquals(CLIENT_API_DEFAULT, serviceInfo.getApiml().getApiInfo().get(0).isDefaultApi());
        assertEquals(CLIENT_API_BASE_PATH, serviceInfo.getApiml().getApiInfo().get(0).getBasePath());
        assertEquals(CLIENT_API_BASE_URL, serviceInfo.getApiml().getApiInfo().get(0).getBaseUrl());

        assertEquals(CLIENT_SERVICE_TITLE, serviceInfo.getApiml().getService().getTitle());
        assertEquals(CLIENT_SERVICE_DESCRIPTION, serviceInfo.getApiml().getService().getDescription());
        assertEquals(CLIENT_SERVICE_HOMEPAGE, serviceInfo.getApiml().getService().getHomePageUrl());

        assertEquals(CLIENT_AUTHENTICATION_SCHEME, serviceInfo.getApiml().getAuthentication().get(0).getScheme());
        assertEquals(CLIENT_AUTHENTICATION_APPLID, serviceInfo.getApiml().getAuthentication().get(0).getApplid());
        assertEquals(CLIENT_AUTHENTICATION_SSO, serviceInfo.getApiml().getAuthentication().get(0).supportsSso());

        assertEquals(InstanceInfo.InstanceStatus.UP, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getStatus());
        assertEquals(CLIENT_HOSTNAME, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getHostname());
        assertEquals(CLIENT_IP, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getIpAddr());
        assertEquals(CLIENT_PORT, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getPort());
        assertEquals(CLIENT_SCHEME, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getProtocol());
        assertEquals(CLIENT_HOMEPAGE, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getHomePageUrl());
        assertEquals(CLIENT_HOMEPAGE + CLIENT_RELATIVE_HEALTH_URL, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getHealthCheckUrl());
        assertEquals(CLIENT_STATUS_URL, serviceInfo.getInstances().get(CLIENT_INSTANCE_ID).getStatusPageUrl());
    }

    @Test
    void whenInstanceProvidesLittleInfo_thenStillReturnUp() {
        when(eurekaClient.getApplication(CLIENT_SERVICE_ID))
                .thenReturn(new Application(CLIENT_SERVICE_ID, Collections.singletonList(createBasicTestInstance())));

        ServiceInfo serviceInfo = servicesInfoService.getServiceInfo(CLIENT_SERVICE_ID);

        assertEquals(InstanceInfo.InstanceStatus.UP, serviceInfo.getStatus());
    }

    @Test
    void whenNoInstances_thenReturnServiceDown() {
        when(eurekaClient.getApplication(CLIENT_SERVICE_ID)).thenReturn(new Application(CLIENT_SERVICE_ID, Collections.emptyList()));

        ServiceInfo serviceInfo = servicesInfoService.getServiceInfo(CLIENT_SERVICE_ID);

        assertEquals(CLIENT_SERVICE_ID, serviceInfo.getServiceId());
        assertEquals(InstanceInfo.InstanceStatus.DOWN, serviceInfo.getStatus());
        assertNull(serviceInfo.getInstances());
        assertNull(serviceInfo.getApiml());
    }

    @Test
    void whenServiceNeverRegistered_thenReturnServiceUnknown() {
        when(eurekaClient.getApplication(CLIENT_SERVICE_ID)).thenReturn(null);

        ServiceInfo serviceInfo = servicesInfoService.getServiceInfo(CLIENT_SERVICE_ID);

        assertEquals(CLIENT_SERVICE_ID, serviceInfo.getServiceId());
        assertEquals(InstanceInfo.InstanceStatus.UNKNOWN, serviceInfo.getStatus());
        assertNull(serviceInfo.getInstances());
        assertNull(serviceInfo.getApiml());
    }

    @Test
    void whenOneInstanceIsUpAndOthersNot_ReturnUp() {
        InstanceInfo instance1 = createBasicTestInstance(InstanceInfo.InstanceStatus.STARTING);
        InstanceInfo instance2 = createBasicTestInstance(InstanceInfo.InstanceStatus.UNKNOWN);
        InstanceInfo instance3 = createBasicTestInstance(InstanceInfo.InstanceStatus.DOWN);
        InstanceInfo instance4 = createBasicTestInstance(InstanceInfo.InstanceStatus.UP);
        List<InstanceInfo> instances = Arrays.asList(instance1, instance2, instance3, instance4);
        when(eurekaClient.getApplication(CLIENT_SERVICE_ID)).thenReturn(new Application(CLIENT_SERVICE_ID, instances));

        ServiceInfo serviceInfo = servicesInfoService.getServiceInfo(CLIENT_SERVICE_ID);

        assertEquals(CLIENT_SERVICE_ID, serviceInfo.getServiceId());
        assertEquals(InstanceInfo.InstanceStatus.UP, serviceInfo.getStatus());
    }

    @Test
    void whenNoInstanceIsUp_ReturnDown() {
        InstanceInfo instance1 = createBasicTestInstance(InstanceInfo.InstanceStatus.STARTING);
        InstanceInfo instance2 = createBasicTestInstance(InstanceInfo.InstanceStatus.UNKNOWN);
        InstanceInfo instance3 = createBasicTestInstance(InstanceInfo.InstanceStatus.DOWN);
        InstanceInfo instance4 = createBasicTestInstance(InstanceInfo.InstanceStatus.OUT_OF_SERVICE);
        List<InstanceInfo> instances = Arrays.asList(instance1, instance2, instance3, instance4);
        when(eurekaClient.getApplication(CLIENT_SERVICE_ID)).thenReturn(new Application(CLIENT_SERVICE_ID, instances));

        ServiceInfo serviceInfo = servicesInfoService.getServiceInfo(CLIENT_SERVICE_ID);

        assertEquals(CLIENT_SERVICE_ID, serviceInfo.getServiceId());
        assertEquals(InstanceInfo.InstanceStatus.DOWN, serviceInfo.getStatus());
    }

    private InstanceInfo createBasicTestInstance() {
        return createBasicTestInstance(InstanceInfo.InstanceStatus.UP);
    }

    private InstanceInfo createBasicTestInstance(InstanceInfo.InstanceStatus status) {
        return InstanceInfo.Builder.newBuilder()
                .setAppName(CLIENT_SERVICE_ID)
                .setInstanceId(CLIENT_INSTANCE_ID)
                .setStatus(status)
                .build();
    }

    private InstanceInfo createFullTestInstance() {
        ApiInfo apiInfo = ApiInfo.builder()
                .apiId(CLIENT_API_ID)
                .version(CLIENT_API_VERSION)
                .gatewayUrl(CLIENT_API_GW_URL)
                .isDefaultApi(CLIENT_API_DEFAULT)
                .swaggerUrl(CLIENT_API_SWAGGER_URL)
                .documentationUrl(CLIENT_API_DOC_URL)
                .build();
        Map<String, String> metadata = EurekaMetadataParser.generateMetadata(CLIENT_SERVICE_ID, apiInfo);
        metadata.put(SERVICE_TITLE, CLIENT_SERVICE_TITLE);
        metadata.put(SERVICE_DESCRIPTION, CLIENT_SERVICE_DESCRIPTION);
        metadata.put(ROUTES + ".ui-v1." + ROUTES_SERVICE_URL, "/");
        metadata.put(ROUTES + ".ui-v1." + ROUTES_GATEWAY_URL, CLIENT_ROUTE_UI);
        metadata.put(AUTHENTICATION_SCHEME, CLIENT_AUTHENTICATION_SCHEME.getScheme());
        metadata.put(AUTHENTICATION_APPLID, CLIENT_AUTHENTICATION_APPLID);

        return InstanceInfo.Builder.newBuilder()
                .setAppName(CLIENT_SERVICE_ID)
                .setInstanceId(CLIENT_INSTANCE_ID)
                .setHostName(CLIENT_HOSTNAME)
                .setIPAddr(CLIENT_IP)
                .enablePort(InstanceInfo.PortType.SECURE, true)
                .setSecurePort(CLIENT_PORT)
                .setHomePageUrl(null, CLIENT_HOMEPAGE)
                .setHealthCheckUrls(CLIENT_RELATIVE_HEALTH_URL, null, null)
                .setStatusPageUrl(null, CLIENT_STATUS_URL)
                .setMetadata(metadata)
                .build();
    }

}