package io.cattle.platform.servicediscovery.process;

import io.cattle.platform.core.model.Service;
import io.cattle.platform.engine.handler.HandlerResult;
import io.cattle.platform.engine.handler.ProcessPostListener;
import io.cattle.platform.engine.process.ProcessInstance;
import io.cattle.platform.engine.process.ProcessState;
import io.cattle.platform.json.JsonMapper;
import io.cattle.platform.object.util.DataAccessor;
import io.cattle.platform.process.common.handler.AbstractObjectProcessLogic;
import io.cattle.platform.servicediscovery.api.constants.ServiceDiscoveryConstants;
import io.cattle.platform.servicediscovery.api.dao.ServiceExposeMapDao;
import io.cattle.platform.util.type.Priority;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * this handler takes care of renaming service instances names based on environment/service name change
 */
@Named
public class ServiceUpdatePostListener extends AbstractObjectProcessLogic implements ProcessPostListener,
        Priority {

    @Inject
    ServiceExposeMapDao svcExposeDao;

    @Inject
    JsonMapper jsonMapper;

    @Override
    public String[] getProcessNames() {
        return new String[] { ServiceDiscoveryConstants.PROCESS_SERVICE_UPDATE };
    }

    @Override
    public HandlerResult handle(ProcessState state, ProcessInstance process) {
        Service service = (Service) state.getResource();

        // rename instances
        // rename only if name is different
        Map<?, ?> old = DataAccessor.fromMap(state.getData())
                .withKey("old").as(jsonMapper, Map.class);

        if (old != null) {
            Object oldName = old.get("name");
            if (oldName != null && !oldName.toString().equalsIgnoreCase(service.getName())) {
                svcExposeDao.updateServiceName(service);
            }
        }

        return null;
    }

    @Override
    public int getPriority() {
        return Priority.DEFAULT;
    }

}
