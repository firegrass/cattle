package io.cattle.platform.register.api;

import io.cattle.platform.core.model.Account;
import io.cattle.platform.iaas.api.auth.AccountAccess;
import io.cattle.platform.iaas.api.auth.AccountLookup;
import io.cattle.platform.iaas.api.auth.impl.BasicAuthImpl;
import io.cattle.platform.iaas.api.auth.impl.DefaultAuthorizationProvider;
import io.cattle.platform.register.auth.RegistrationAuthTokenManager;
import io.cattle.platform.register.util.RegisterConstants;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;

import javax.inject.Inject;

public class RegistrationTokenAccountLookup implements AccountLookup {

    RegistrationAuthTokenManager tokenManager;

    @Override
    public AccountAccess getAccountAccess(ApiRequest request) {
        String[] auth = BasicAuthImpl.getUsernamePassword(request);

        if (auth == null) {
            return null;
        }

        String username = auth[0];
        String password = auth[1];

        if (!RegisterConstants.KIND_CREDENTIAL_REGISTRATION_TOKEN.equals(username)) {
            return null;
        }

        Account account = tokenManager.validateToken(password);

        if (account == null) {
            return null;
        }

        request.setAttribute(DefaultAuthorizationProvider.ACCOUNT_SCHEMA_FACTORY_NAME, RegisterConstants.SCHEMA_NAME);

        return new AccountAccess(account, null);
    }

    @Override
    public boolean challenge(ApiRequest request) {
        return false;
    }

    public RegistrationAuthTokenManager getTokenManager() {
        return tokenManager;
    }

    @Inject
    public void setTokenManager(RegistrationAuthTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

}
