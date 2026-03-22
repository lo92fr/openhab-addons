package org.openhab.binding.smartthings.internal.type;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.action.ThingAction;
import org.openhab.core.thing.action.ThingActionProvider;
import org.openhab.core.thing.action.ThingActionRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(service = { SmartThingsActionProviderImpl.class, ThingActionProvider.class })
public class SmartThingsActionProviderImpl implements SmartThingsActionProvider {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsActionProviderImpl.class);

    @Reference
    private @Nullable ThingActionRegistry thingActionRegistry;

    public SmartThingsActionProviderImpl() {
        logger.info("constr");
    }

    @Override
    public Collection<ThingAction> getActions() {
        // TODO Auto-generated method stub
        logger.info("test");
        throw new RuntimeException("aa");
    }

}