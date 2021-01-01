package org.openhab.binding.siemenshvac.internal.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelGroupTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacConfigDescriptionProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacThingTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.UidUtils;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameterGroup;
import org.openhab.core.config.core.ConfigDescriptionParameterGroupBuilder;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.types.EventDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component(immediate = true)
@NonNullByDefault
public class SiemensHvacMetadataRegistryImpl implements SiemensHvacMetadataRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SiemensHvacMetadataRegistryImpl.class);

    // private Map<ThingTypeUID, ThingType> thingTypesByUID = new HashMap<>();
    // protected List<HomematicThingTypeExcluder> homematicThingTypeExcluders = new CopyOnWriteArrayList<>();

    // A map contains data point config read from Api and/or WebPages
    private @Nullable Map<String, SiemensHvacMetadata> dptMap = null;
    private @Nullable SiemensHvacMetadata root = null;

    // private siemensHvacConnector cnx = null;
    private boolean interrupted = false;

    private static final String CONFIG_DIR = "." + File.separator + "configurations";
    private static final String CONTENT_DIR = CONFIG_DIR + File.separator + "services";

    private @Nullable static URI configDescriptionUriChannel;

    private @Nullable SiemensHvacThingTypeProvider thingTypeProvider;
    private @Nullable SiemensHvacChannelTypeProvider channelTypeProvider;
    private @Nullable SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SiemensHvacConfigDescriptionProvider configDescriptionProvider;
    private @Nullable SiemensHvacConnector hvacConnector;

    public SiemensHvacMetadataRegistryImpl() {
    }

    @Reference
    protected void setSiemensHvacConnector(SiemensHvacConnector hvacConnector) {
        this.hvacConnector = hvacConnector;
    }

    protected void unsetSiemensHvacConnector(SiemensHvacConnector hvacConnector) {
        this.hvacConnector = null;
    }

    @Reference
    protected void setThingTypeProvider(SiemensHvacThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    protected void unsetThingTypeProvider(SiemensHvacThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = null;
    }

    @Reference
    protected void setChannelTypeProvider(SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    protected void unsetChannelTypeProvider(SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    //
    @Reference
    protected void setChannelGroupTypeProvider(SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = channelGroupTypeProvider;
    }

    protected void unsetChannelGroupTypeProvider(SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = null;
    }

    @Reference
    protected void setConfigDescriptionProvider(SiemensHvacConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = configDescriptionProvider;
    }

    protected void unsetConfigDescriptionProvider(SiemensHvacConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    /**
     * Initializes the type generator.
     */
    @Override
    @Activate
    public void initialize() {
    }

    public void InitDptMap(@Nullable SiemensHvacMetadata node) {

        if (node.getClass() == SiemensHvacMetadataMenu.class) {
            SiemensHvacMetadataMenu mInformation = (SiemensHvacMetadataMenu) node;

            for (SiemensHvacMetadata child : mInformation.getChilds().values()) {
                InitDptMap(child);
            }
        }

        // ((DataPointInformation) childNode).resolveDptDetails();
        if (node != null) {
            if (node.getLongDesc() != null) {
                dptMap.put("byName" + node.getLongDesc(), node);
            }
            if (node.getShortDesc() != null) {
                dptMap.put("byName" + node.getShortDesc(), node);
            }
        }

        dptMap.put("byMenu" + node.getMenuId(), node);
        if (node.getClass() == SiemensHvacMetadataDataPoint.class) {
            SiemensHvacMetadataDataPoint dpi = (SiemensHvacMetadataDataPoint) node;
            dptMap.put("byDptId" + dpi.getDptId(), node);
        }
    }

    @Override
    public @Nullable SiemensHvacMetadataMenu getRoot() {
        return (SiemensHvacMetadataMenu) root;
    }

    @Override
    public void ReadMeta() {
        if (root == null) {
            logger.debug("siemensHvac:InitDptMap():begin");

            LoadMetaDataFromCache();

            if (root == null) {
                root = new SiemensHvacMetadataMenu();
                ReadMetaData(root, -1);
            }

            dptMap = new Hashtable<String, SiemensHvacMetadata>();
            InitDptMap(root);
            SaveMetaDataToCache();
            logger.debug("siemensHvac:InitDptMap():end");
        }

        test();
    }

    private void test() {
        if (thingTypeProvider != null) {
            ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID("");
            ThingType tt = thingTypeProvider.getInternalThingType(thingTypeUID);

            if (tt == null) {

                List<ChannelGroupType> groupTypes = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    List<ChannelDefinition> channelDefinitions = new ArrayList<>();

                    ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(i);

                    ChannelType channelType = channelTypeProvider.getInternalChannelType(channelTypeUID);
                    if (channelType == null) {
                        channelType = createChannelType("dp" + i, channelTypeUID);
                        channelTypeProvider.addChannelType(channelType);
                    }

                    ChannelDefinition channelDef = new ChannelDefinitionBuilder("name" + i, channelType.getUID())
                            .build();
                    channelDefinitions.add(channelDef);

                    // generate group
                    ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(i);
                    ChannelGroupType groupType = channelGroupTypeProvider.getInternalChannelGroupType(groupTypeUID);

                    if (groupType == null) {
                        String groupLabel = "groupLabel" + i;
                        groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, groupLabel)
                                .withChannelDefinitions(channelDefinitions).build();
                        channelGroupTypeProvider.addChannelGroupType(groupType);
                        groupTypes.add(groupType);
                    }
                }

                tt = createThingType("name", groupTypes);
                thingTypeProvider.addThingType(tt);
            }
        }
    }

    private ChannelType createChannelType(String name, ChannelTypeUID channelTypeUID) {
        ChannelType channelType;
        if (name.equals(SiemensHvacBindingConstants.DATAPOINT_NAME_LOWBAT)
                || name.equals(SiemensHvacBindingConstants.DATAPOINT_NAME_LOWBAT_IP)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY;
        } else if (name.equals(SiemensHvacBindingConstants.VIRTUAL_DATAPOINT_NAME_SIGNAL_STRENGTH)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_SIGNAL_STRENGTH;
        } else if (name.equals(SiemensHvacBindingConstants.VIRTUAL_DATAPOINT_NAME_BUTTON)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_BUTTON;
        } else {
            String itemType = SiemensHvacBindingConstants.ITEM_TYPE_CONTACT;
            // MetadataUtils.getItemType(dp);
            String category = SiemensHvacBindingConstants.CATEGORY_TEMPERATURE;
            // MetadataUtils.getCategory(dp, itemType);
            String label = name;
            // MetadataUtils.getLabel(dp);
            String description = "desc" + name;
            // MetadataUtils.getDatapointDescription(dp);

            /*
             * List<StateOption> options = null;
             * if (dp.isEnumType()) {
             * options = MetadataUtils.generateOptions(dp, new OptionsBuilder<StateOption>() {
             *
             * @Override
             * public StateOption createOption(String value, String description) {
             * return new StateOption(value, description);
             * }
             * });
             * }
             *
             */
            StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();
            /*
             * if (dp.isNumberType()) {
             * BigDecimal min = MetadataUtils.createBigDecimal(dp.getMinValue());
             * BigDecimal max = MetadataUtils.createBigDecimal(dp.getMaxValue());
             * BigDecimal step = MetadataUtils.createBigDecimal(dp.getStep());
             * if (ITEM_TYPE_DIMMER.equals(itemType)
             * && (max.compareTo(new BigDecimal("1.0")) == 0 || max.compareTo(new BigDecimal("1.01")) == 0)) {
             * // For dimmers with a max value of 1.01 or 1.0 the values must be corrected
             * min = MetadataUtils.createBigDecimal(0);
             * max = MetadataUtils.createBigDecimal(100);
             * step = MetadataUtils.createBigDecimal(1);
             * } else {
             * if (step == null) {
             * step = MetadataUtils
             * .createBigDecimal(dp.isFloatType() ? Float.valueOf(0.1f) : Long.valueOf(1L));
             * }
             * }
             */
            stateFragment.withMinimum(new BigDecimal(0)).withMaximum(new BigDecimal(20)).withStep(new BigDecimal(1))
                    .withReadOnly(false);

            ChannelTypeBuilder channelTypeBuilder;
            EventDescription eventDescription = null;
            channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                    .withStateDescriptionFragment(stateFragment.build());

            channelType = channelTypeBuilder.isAdvanced(false).withDescription(description).withCategory(category)
                    .build();
        }

        return channelType;
    }

    /**
     * Creates the ThingType for the given device.
     */
    private ThingType createThingType(String name, List<ChannelGroupType> groupTypes) {
        String label = "label" + name;
        String description = String.format("%s (%s)", label, "dType");

        List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(SiemensHvacBindingConstants.THING_TYPE_OZW672.toString());
        ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(name);

        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, SiemensHvacBindingConstants.PROPERTY_VENDOR_NAME);
        properties.put(Thing.PROPERTY_MODEL_ID, "dType");

        URI configDescriptionURI = getConfigDescriptionURI(name);
        if (configDescriptionProvider.getInternalConfigDescription(configDescriptionURI) == null) {
            generateConfigDescription(name, configDescriptionURI);
        }

        List<ChannelGroupDefinition> groupDefinitions = new ArrayList<>();
        for (ChannelGroupType groupType : groupTypes) {
            String id = groupType.getUID().getId();
            groupDefinitions.add(new ChannelGroupDefinition(id, groupType.getUID()));
        }

        return ThingTypeBuilder.instance(thingTypeUID, label).withSupportedBridgeTypeUIDs(supportedBridgeTypeUids)
                .withDescription(description).withChannelGroupDefinitions(groupDefinitions).withProperties(properties)
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withConfigDescriptionURI(configDescriptionURI)
                .build();
    }

    private URI getConfigDescriptionURI(String name) {
        try {
            return new URI(String.format("%s:%s", SiemensHvacBindingConstants.CONFIG_DESCRIPTION_URI_THING_PREFIX,
                    UidUtils.generateThingTypeUID(name)));
        } catch (URISyntaxException ex) {
            logger.warn("Can't create configDescriptionURI for device type {}", name);
            throw new RuntimeException("can't construct URI");
        }
    }

    private void generateConfigDescription(String name, URI configDescriptionURI) {
        List<ConfigDescriptionParameter> parms = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String groupName = "HMG_" + i;
            String groupLabel = "desc" + i;
            groups.add(ConfigDescriptionParameterGroupBuilder.create(groupName).withLabel(groupLabel).build());

            for (int j = 0; j < 3; j++) {

                ConfigDescriptionParameterBuilder builder = ConfigDescriptionParameterBuilder.create("pName" + j,
                        Type.INTEGER);

                builder.withLabel("label" + j);
                builder.withDefault("0");
                builder.withDescription("desc" + j);

                builder.withMinimum(new BigDecimal(0));
                builder.withMaximum(new BigDecimal(20));
                builder.withStepSize(new BigDecimal(1));
                builder.withUnitLabel("°C");

                builder.withGroupName(groupName);
                parms.add(builder.build());

            }
        }
        configDescriptionProvider.addConfigDescription(ConfigDescriptionBuilder.create(configDescriptionURI)
                .withParameters(parms).withParameterGroups(groups).build());

    }

    public void ReadMetaData(SiemensHvacMetadata parent, int id) {
        try {
            String request = "api/menutree/list.json?";
            if (id != -1) {
                request = request + "&Id=" + id;
            }

            SiemensHvacMetadata childNode;
            JsonObject resultObj = hvacConnector.DoRequest(request);
            /*
             * String html = cnx.DoBasicRequest("main.app?section=popcard&idtype=4&id=" + id);
             * Pattern pattern = Pattern.compile("td class=\"dp_linenumber\".?>(.?)</td>.?id=\"dp(.?)\"");
             * Matcher matcher = pattern.matcher(html);
             * Map<String, String> dptToIdMap = new Hashtable<String, String>();
             * while (matcher.find()) {
             * String all = matcher.group(0);
             * String idNum = matcher.group(1);
             * String dpNum = matcher.group(2);
             *
             * dptToIdMap.put(idNum, dpNum);
             * dptToIdMap.put(dpNum, idNum);
             * }
             */

            if (resultObj.has("MenuItems")) {
                JsonArray menuItems = resultObj.getAsJsonArray("MenuItems");

                for (JsonElement child : menuItems) {
                    JsonObject menuItem = child.getAsJsonObject();

                    if (menuItem == null) {
                        continue;
                    }

                    childNode = new SiemensHvacMetadataMenu();
                    childNode.setParent(parent);

                    int itemId = -1;
                    if (menuItem.has("Id")) {
                        itemId = menuItem.get("Id").getAsInt();
                    }

                    if (menuItem.has("Text")) {
                        JsonObject descObj = menuItem.getAsJsonObject("Text");

                        int catId = -1;
                        int groupId = -1;
                        int subItemId = -1;
                        String longDesc = "";
                        String shortDesc = "";

                        if (descObj.has("CatId")) {
                            catId = descObj.get("CatId").getAsInt();
                        }
                        if (descObj.has("GroupId")) {
                            groupId = descObj.get("GroupId").getAsInt();
                        }
                        if (descObj.has("Id")) {
                            subItemId = descObj.get("Id").getAsInt();
                        }
                        if (descObj.has("Long")) {
                            longDesc = descObj.get("Long").getAsString();
                        }
                        if (descObj.has("Short")) {
                            shortDesc = descObj.get("Short").getAsString();
                        }

                        childNode.setMenuId(subItemId);
                        childNode.setCatId(catId);
                        childNode.setGroupId(groupId);
                        childNode.setShortDesc(shortDesc);
                        childNode.setLongDesc(longDesc);
                        ((SiemensHvacMetadataMenu) parent).AddChild(childNode);

                    }
                }
            }
            if (resultObj.has("DatapointItems")) {
                // JSONArray dpItems = (JSONArray) result.get("DatapointItems");

                logger.debug("p1");

            }
            if (resultObj.has("WidgetItems")) {
                logger.debug("p2");
                // JSONArray wgItems = (JSONArray) result.get("WidgetItems");
            }

            logger.debug("p3");

        } catch (Exception e) {
            logger.error("siemensHvac:ResolveDpt:Error during dp reading: " + id + " ; " + e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        }

    }

    public void VerifyBindingConfig(/* siemensHvacBindingConfig bindingConfig */) {
        /*
         * String dptType = bindingConfig.getDptType();
         *
         * int dptId = bindingConfig.getDptId();
         * int dptMenuId = bindingConfig.getDptMenuId();
         * String dptName = bindingConfig.getDptName();
         *
         * siemensMetadata entry = null;
         *
         * if (entry == null && dptMenuId != -1) {
         * if (dptMap.containsKey("byMenu" + dptMenuId)) {
         * entry = dptMap.get("byMenu" + dptMenuId);
         * }
         * }
         *
         * if (entry == null && dptName != null) {
         * if (dptMap.containsKey("byName" + dptName)) {
         * entry = dptMap.get("byName" + dptName);
         * }
         * }
         *
         * if (entry == null && dptId != -1) {
         * if (dptMap.containsKey("byDptId" + dptId)) {
         * entry = dptMap.get("byDptId" + dptId);
         * }
         * }
         *
         * if (entry != null && entry.getClass() == siemensMetadataDataPoint.class) {
         * siemensMetadataDataPoint dpt = (siemensMetadataDataPoint) entry;
         *
         * resolveDptDetails(dpt);
         *
         * if (dptId == -1) {
         * bindingConfig.setDptId(dpt.getDptId());
         * }
         * if (dptName == null || dptName.equals("")) {
         * bindingConfig.setDptName(entry.getLongDesc());
         * }
         * if (dptMenuId == -1) {
         * bindingConfig.setDptMenuId(entry.getMenuId());
         * }
         *
         * if (dptType == null) {
         * bindingConfig.setDptType(dpt.getDptType());
         * }
         * }
         */
    }

    public void LoadMetaDataFromCache() {
        File file = null;

        try {
            file = new File(CONFIG_DIR + File.separator + "siemens.json");

            if (!file.exists()) {
                return;
            }

            FileInputStream is = new FileInputStream(file);
            String js = IOUtils.toString(is);

            root = hvacConnector.getGson().fromJson(js, SiemensHvacMetadataMenu.class);
        } catch (IOException ioe) {
            logger.error("Couldn't write WithingsAccount to file '{}'.", file.getAbsolutePath());

        }

    }

    public void SaveMetaDataToCache() {
        File file = null;

        try {
            file = new File(CONFIG_DIR + File.separator + "siemens.json");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream os = new FileOutputStream(file);

            String js = hvacConnector.getGson().toJson(root);

            IOUtils.write(js, os);
            IOUtils.closeQuietly(os);

        } catch (IOException ioe) {
            logger.error("Couldn't write WithingsAccount to file '{}'.", file.getAbsolutePath());

        }
    }

    /*
     * public void resolveDptDetails(siemensMetadataDataPoint dpt) {
     * if (dpt.getDetailsResolved()) {
     * return;
     * }
     *
     * String request = "api/menutree/datapoint_desc.json?Id=" + dpt.getDptId();
     * JSONObject result = cnx.DoRequest(request);
     * dpt.resolveDptDetails(result);
     * }
     */

}
