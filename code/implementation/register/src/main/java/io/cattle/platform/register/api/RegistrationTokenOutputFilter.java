package io.cattle.platform.register.api;

import io.cattle.platform.archaius.util.ArchaiusUtil;
import io.cattle.platform.core.model.Credential;
import io.cattle.platform.iaas.config.ScopedConfig;
import io.cattle.platform.register.util.RegisterConstants;
import io.cattle.platform.register.util.RegistrationToken;
import io.cattle.platform.server.context.ServerContext;
import io.github.ibuildthecloud.gdapi.context.ApiContext;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.response.ResourceOutputFilter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.inject.Inject;

import com.netflix.config.DynamicStringProperty;

public class RegistrationTokenOutputFilter implements ResourceOutputFilter {

    private static final DynamicStringProperty DOCKER_CMD = ArchaiusUtil.getString("docker.register.command");
    private static final DynamicStringProperty REQUIRED_IMAGE = ArchaiusUtil.getString("bootstrap.required.image");

    @Inject ScopedConfig scopedConfig;

    @Override
    public Resource filter(ApiRequest request, Object original, Resource converted) {
        if (!(original instanceof Credential)) {
            return converted;
        }

        Credential cred = (Credential) original;

        String accessKey = cred.getPublicValue();
        String secretKey = cred.getSecretValue();

        if (accessKey != null && secretKey != null) {
            String token = RegistrationToken.createToken(accessKey, secretKey);
            URL url = null;
            if (ServerContext.isCustomApiHost()) {
                try {
                    url = new URL(scopedConfig.getApiUrl(null) + "/scripts/" + token);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid URL", e);
                }
            } else {
                url = ApiContext.getUrlBuilder().resourceReferenceLink("scripts", token);
            }

            Map<String, Object> fields = converted.getFields();
            Map<String, URL> links = converted.getLinks();

            fields.put("command", String.format(DOCKER_CMD.get(), REQUIRED_IMAGE.get(), url.toExternalForm()));
            fields.put("token", token);
            fields.put("registrationUrl", url.toExternalForm());
            links.put("registrationUrl", url);
        }

        return converted;
    }

    @Override
    public String[] getTypes() {
        return new String[] { RegisterConstants.KIND_CREDENTIAL_REGISTRATION_TOKEN };
    }

    @Override
    public Class<?>[] getTypeClasses() {
        return new Class<?>[0];
    }

}
