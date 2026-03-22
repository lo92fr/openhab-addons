/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smartthings.internal.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverter;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverterFactory;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatus;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusCapabilities;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusProperties;
import org.openhab.binding.smartthings.internal.statehandler.SmartThingsStateHandler;
import org.openhab.binding.smartthings.internal.statehandler.SmartThingsStateHandlerFactory;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistryImpl;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsThingHandler.class);

    private String smartThingsName;

    private @Nullable ScheduledFuture<?> pollingJob = null;
    private long lastRefresh = System.nanoTime();

    public SmartThingsThingHandler(Thing thing) {
        super(thing);
        smartThingsName = ""; // Initialize here so it can be NonNull but it should always get a value in initialize()
    }

    /**
     * Called when openHAB receives a command for this handler
     *
     * @param channelUID The channel the command was sent to
     * @param command The command sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = getBridge();

        // Check if the bridge has not been initialized yet
        if (bridge == null) {
            logger.debug(
                    "The bridge has not been initialized yet. Can not process command for channel {} with command {}.",
                    channelUID.getAsString(), command.toFullString());
            return;
        }

        SmartThingsAccountHandler accountHander = (SmartThingsAccountHandler) bridge.getHandler();

        if (accountHander != null && accountHander.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            // channelUID
            SmartThingsConverter converter = SmartThingsConverterFactory.getConverter(channelUID.getIdWithoutGroup());

            String jsonMsg = "";
            if (command instanceof RefreshType) {
                refreshDevice();
            } else {
                try {
                    if (converter != null) {
                        jsonMsg = converter.convertToSmartThings(thing, channelUID, command);
                    }

                    SmartThingsApi api = accountHander.getSmartThingsApi();
                    if (api == null) {
                        return;
                    }
                    Map<String, String> properties = this.getThing().getProperties();
                    String deviceId = properties.get("deviceId");

                    if (deviceId != null) {
                        api.sendCommand(deviceId, jsonMsg);
                    }
                } catch (SmartThingsException ex) {
                    logger.error("Unable to send command: {}", ex.toString());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                }
            }
        }
    }

    public void refreshChannel(String deviceType, String componentId, String namespace, String capaKey, String attr,
            Object value) throws SmartThingsException {
        String channelName = SmartThingsTypeRegistryImpl.getChannelName(attr);

        String groupId = deviceType + "_" + componentId + "_";

        if (!"".equals(namespace)) {
            groupId = groupId + namespace + "_";
        }
        groupId = groupId + capaKey;

        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), groupId, channelName);

        logger.trace("refreshDevice called: channelName:{}", channelName);

        // channelUID
        SmartThingsConverter converter = SmartThingsConverterFactory.getConverter(channelUID.getIdWithoutGroup());
        SmartThingsStateHandler stateHandler = SmartThingsStateHandlerFactory.getStateHandler(deviceType);

        if (converter != null) {
            State state = converter.convertToOpenHab(thing, channelUID, value);
            updateState(channelUID, state);

            if (stateHandler != null) {
                logger.trace("refreshDevice called: stateHandler:{}", stateHandler);
                stateHandler.handleStateChange(channelUID, deviceType, componentId, state, this);
            }
        }
    }

    public void refreshDevice(String deviceType, String componentId, String capa, String attr, Object value) {
        try {
            logger.trace("refreshDevice called: deviceType:{} componentId: {} capa: {} attr :{} value: {}", deviceType,
                    componentId, capa, attr, value);
            String namespace = "";
            String capaKey = capa;
            if (capa.contains(".")) {
                String[] idComponents = capa.split("\\.");
                namespace = idComponents[0];
                capaKey = idComponents[1];
            }

            if (attr.indexOf("Range") >= 0) {
                return;
            }

            if (value instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    Object subValue = entry.getValue();
                    if (key != null && subValue != null) {
                        String subKey = key.toString();
                        refreshChannel(deviceType, componentId, namespace, capaKey, subKey, subValue);
                    }
                }

            } else {
                refreshChannel(deviceType, componentId, namespace, capaKey, attr, value);
            }

        } catch (Exception ex) {
            logger.error("Unable to refresh device: {} {}", this.getThing().getUID(), ex.toString(), ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    public void sendUpdateState(ChannelUID channelUid, State state) {
        updateState(channelUid, state);
    }

    public void refreshDevice() {
        logger.trace("refreh Device called");
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) bridge.getHandler();
        if (accountHandler == null) {
            return;
        }
        SmartThingsApi api = accountHandler.getSmartThingsApi();
        if (api == null) {
            return;
        }

        Map<String, String> properties = getThing().getProperties();

        String deviceId = properties.get("deviceId");

        logger.trace("refrehDevice for deviceId: {}", deviceId);

        if (deviceId != null) {
            try {
                SmartThingsStatus status = api.getStatus(deviceId);

                logger.trace("refrehDevice for deviceId: status : {}", status);

                if (status != null) {
                    for (String componentKey : status.components.keySet()) {
                        SmartThingsStatusComponent component = status.components.get(componentKey);

                        if (component != null) {
                            for (String capaKey : component.keySet()) {
                                SmartThingsStatusCapabilities capa = component.get(capaKey);

                                if (capa != null) {
                                    for (String propertyKey : capa.keySet()) {
                                        SmartThingsStatusProperties props = capa.get(propertyKey);

                                        if (props != null) {
                                            Object value = props.value;

                                            if (value != null) {
                                                logger.trace("refrehDevice for deviceId: value : {}", value);

                                                refreshDevice(thing.getThingTypeUID().getId(), componentKey, capaKey,
                                                        propertyKey, value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SmartThingsException ex) {
                logger.error("Unable to update device : {}", deviceId);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        // Create converters for each channel

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) bridge.getHandler();
        if (accountHandler == null) {
            return;
        }

        SmartThingsTypeRegistry typeRegistry = accountHandler.getSmartThingsTypeRegistry();

        SmartThingsConverterFactory.registerConverters(typeRegistry);
        SmartThingsStateHandlerFactory.registerStateHandler();

        // testCommand();
        refreshDevice();

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 1, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
    }

    public void testCommand() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) bridge.getHandler();
        if (accountHandler == null) {
            return;
        }
        SmartThingsApi api = accountHandler.getSmartThingsApi();
        if (api == null) {
            return;
        }
        String deviceId = "702C1F72-C35A-0000-0000-000000000000";

        String jsonMsg = "";
        jsonMsg += "{";
        jsonMsg += "   \"commands\":";
        jsonMsg += "     [";
        jsonMsg += "        {";
        jsonMsg += "          \"component\":\"main\",";
        jsonMsg += "          \"capability\":\"ovenOperatingState\",";
        jsonMsg += "          \"command\":\"start\",";
        jsonMsg += "          \"arguments\":[\"Baker\", 300 , 210 ]";
        jsonMsg += "        }";
        jsonMsg += "     ]";
        jsonMsg += "}";

        try {
            api.sendCommand(deviceId, jsonMsg);
        } catch (SmartThingsException ex) {
            logger.error("exception: ", ex);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> lcPollingJob = pollingJob;
        if (lcPollingJob != null) {
            lcPollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void pollingCode() {
        Bridge lcBridge = getBridge();

        if (lcBridge == null) {
            return;
        }

        if (lcBridge.getStatus() == ThingStatus.OFFLINE) {
            if (!ThingStatusDetail.COMMUNICATION_ERROR.equals(lcBridge.getStatusInfo().getStatusDetail())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
        }

        if (lcBridge.getStatus() != ThingStatus.ONLINE) {
            if (!ThingStatusDetail.COMMUNICATION_ERROR.equals(lcBridge.getStatusInfo().getStatusDetail())) {
                logger.debug("Bridge is not ready, don't enter polling for now!");
                return;
            }
        }

        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) lcBridge.getHandler();
        if (accountHandler != null) {
            int pollingTime = accountHandler.getPollingTime();
            if (pollingTime != -1) {
                long now = System.nanoTime();
                if (now - lastRefresh > TimeUnit.SECONDS.toNanos(pollingTime)) {
                    refreshDevice();
                    lastRefresh = now;
                }
            }
        }
    }

    // @Override
    public Collection<Class<? extends ThingHandlerService>> getServices2() {

        DynamicType.Builder<?> builder = new ByteBuddy().subclass(ThingActions.class);

        builder = builder.name("SmartThingsActions");

        AnnotationDescription componentAnnotation = AnnotationDescription.Builder.ofType(Component.class)
                .define("scope", ServiceScope.PROTOTYPE).defineTypeArray("service", SmartThingsActions.class).build();

        AnnotationDescription scopeAnnotation = AnnotationDescription.Builder.ofType(ThingActionsScope.class)
                .define("name", "smartthings").build();

        // AnnotationDescription nonNullAnnotation =
        // AnnotationDescription.Builder.ofType(NonNullByDefault.class).build();

        builder = builder.defineField("handler", SmartThingsThingHandler.class, Modifier.PRIVATE);

        builder = builder.annotateType(componentAnnotation, scopeAnnotation);

        // 👉 setThingHandler
        builder = builder.defineMethod("setThingHandler", void.class, Modifier.PUBLIC).withParameter(ThingHandler.class)
                .intercept(FieldAccessor.ofField("handler").withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)
                        .setsArgumentAt(0));

        // 👉 getThingHandler
        builder = builder.defineMethod("getThingHandler", ThingHandler.class, Modifier.PUBLIC)
                .intercept(FieldAccessor.ofField("handler"));

        AnnotationDescription ruleAction = AnnotationDescription.Builder.ofType(RuleAction.class)
                .define("label", "@text/actionLabel").define("description", "@text/actionDesc").build();

        /*
         * AnnotationDescription actionOutput = AnnotationDescription.Builder.ofType(ActionOutput.class)
         * .define("name", "topic").define("type", "String").define("label", "@text/actionInputTopicLabel")
         * .define("description", "@text/actionInputTopicDesc").build();
         *
         * AnnotationDescription stateInput = AnnotationDescription.Builder.ofType(ActionInput.class)
         * .define("name", "state").define("label", "state")
         * .define("description", "State of the fade effect : Run/Stop").build();
         *
         * // AnnotationDescription nullable = AnnotationDescription.Builder.ofType(Nullable.class).build();
         *
         * AnnotationDescription fadeTypeInput = AnnotationDescription.Builder.ofType(ActionInput.class)
         * .define("name", "fadeType").define("label", "fadeType")
         * .define("description", "The type of fade: WakeUp/WakeDown").build();
         * AnnotationDescription durationInput = AnnotationDescription.Builder.ofType(ActionInput.class)
         * .define("name", "duration").define("label", "duration")
         * .define("description", "The duration of the fade effect").build();
         *
         * AnnotationDescription colorTempInput = AnnotationDescription.Builder.ofType(ActionInput.class)
         * .define("name", "colorTemperature").define("label", "colorTemperature")
         * .define("description", "The colorTemperature").build();
         */

        builder = builder.defineMethod("setFade", String.class, Modifier.PUBLIC)

                .withParameters(String.class, String.class, int.class, int.class)

                .intercept(FixedValue.value("test"))

                .annotateMethod(ruleAction);

        // .annotateParameter(0, stateInput).annotateParameter(1, fadeTypeInput)
        // .annotateParameter(2, durationInput).annotateParameter(3, colorTempInput);

        ClassLoader cl = RuleAction.class.getClassLoader();

        Class dynamicType = builder.make().load(cl, ClassLoadingStrategy.Default.CHILD_FIRST).getLoaded();

        String t1 = dynamicType.getClassLoader().toString();

        return List.of(dynamicType);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SmartThingsActions.class);
    }

    // @Override
    public Collection<Class<? extends ThingHandlerService>> getServices3() {

        try {
            ClassPool pool = new ClassPool();
            pool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
            CtClass cc = pool.get("org.openhab.binding.smartthings.internal.handler.SmartThingsActions");

            // cc.getClassFile().setVersionToJava5();

            // @ActionOutput(name = "topic", type = "String", label = "@text/actionInputTopicLabel", description =
            // "@text/actionInputTopicDesc")
            // @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
            // public String setFade(
            // @ActionInput(name = "state", label = "state", description = "State of the fade effect : Run/Stop")
            // @Nullable String state,
            // @ActionInput(name = "fadeType", label = "fadeType", description = "The type of fade: WakeUp/WakeDown")
            // @Nullable String fadeType,
            // @ActionInput(name = "duration", label = "duration", description = "The duration of the fade effect") int
            // duration,
            // @ActionInput(name = "colorTemperature", label = "colorTemperature", description = "The colorTemperature")
            // int colorTemperature) {

            // Ajouter l'annotation
            ConstPool constPool = cc.getClassFile().getConstPool();

            CtMethod m;
            m = CtNewMethod.make("public void setTest() {  }", cc);
            cc.addMethod(m);

            // MethodInfo methodInfo = m.getMethodInfo();
            MethodInfo methodInfo = m.getMethodInfo2();

            // récupérer l'attribut existant s'il existe
            AnnotationsAttribute attr = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);

            if (attr == null) {
                attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                methodInfo.addAttribute(attr);
            }

            Annotation annot = new Annotation("org.openhab.core.automation.annotation.RuleAction", constPool);

            annot.addMemberValue("label", new StringMemberValue("test", constPool));
            annot.addMemberValue("description", new StringMemberValue("testDesc", constPool));

            attr.addAnnotation(annot);

            cc.writeFile("/tmp/out.class");
            Class clazz = cc.toClass(this.getClass().getClassLoader());

            for (Method method : clazz.getDeclaredMethods()) {
                System.out.println(method.getName());
                for (java.lang.annotation.Annotation a : method.getAnnotations()) {
                    System.out.println(" -> " + a);
                }
            }

            return List.of((Class<? extends ThingHandlerService>) clazz);
        } catch (

        Exception ex) {
            logger.error("aa", ex);
            throw new RuntimeException(ex);
        }

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("smartthingsName :").append(smartThingsName);
        sb.append(", thing UID: ").append(thing.getUID());
        sb.append(", thing label: ").append(thing.getLabel());
        return sb.toString();
    }

    public @Nullable SmartThingsApi getApi() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) bridge.getHandler();
        if (accountHandler == null) {
            return null;
        }
        SmartThingsApi api = accountHandler.getSmartThingsApi();
        return api;
    }
}
