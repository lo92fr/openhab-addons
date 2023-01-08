package org.openhab.binding.mbus.internal.old;

// import org.openhab.core.binding.BindingConfig;
// import main.java.org.openmuc.jmbus.Bcd;
// import main.java.org.openmuc.jmbus.DataRecord;
// import main.java.org.openmuc.jmbus.MBusSap;
// import main.java.org.openmuc.jmbus.VariableDataStructure;

// public class mbusConnector extends Thread {
//
// private static final Logger logger = LoggerFactory.getLogger(mbusBinding.class);
//
// private String host = null;
// private int port = 2460;
// private Date lastUpdate;
//
// private Map<String, Type> updateCommand;
//
// private boolean interrupted = false;
//
// private mbusBinding binding;
// private MBusSap sap;
//
// public mbusConnector(mbusBinding binding) {
// this.binding = binding;
// this.updateCommand = new Hashtable<String, Type>();
// start();
// }
//
// public void setHost(String host) {
// this.host = host;
// }
//
// public void setPort(int port) {
// this.port = port;
// }
//
// private void _doConnect() {
// if (sap != null) {
// return;
// }
//
// try {
// sap = MBusSap.getTCPSAP(host, port);
// sap.open();
// logger.debug("mbus:: open Sap Connection");
// } catch (Exception ex) {
// logger.debug("exception:" + ex.getMessage());
// }
//
// }
//
// public void ReadAllDp() {
//
// //
// _doConnect();
//
// mbusGenericBindingProvider provider = binding.getProvider();
// logger.info("mbus:readAllDP():begin");
//
// Iterator<BindingConfig> it = provider.getBindingConfigs().values().iterator();
//
// Map<String, VariableDataStructure> readCache = new Hashtable<String, VariableDataStructure>();
//
// while (it.hasNext()) {
// mbusBindingConfig config = (mbusBindingConfig) it.next();
// String name = config.getName();
// int mbusId = config.getmbusId();
// int dib = config.getDib();
// try {
//
// logger.info("try cache for " + name + " / " + mbusId + " / " + dib);
// VariableDataStructure dt = null;
// if (readCache.containsKey("" + mbusId)) {
// dt = readCache.get("" + mbusId);
// } else {
// dt = null;
//
// // give multiple try in case of error or disconnection
// int nbtry = 0;
// while (dt == null && nbtry < 3) {
// try {
// nbtry++;
// logger.info("do read " + name + " / " + mbusId + " / " + dib);
// dt = sap.read(mbusId);
// if (dt != null) {
// readCache.put("" + mbusId, dt);
// }
// logger.info("end read " + name + " / " + mbusId + " / " + dib);
//
// } catch (Exception ex) {
// logger.debug("exception during reads2 !:" + ex.getMessage());
// sap = null;
// logger.info("reconnect");
// _doConnect();
// }
// }
// }
//
// if (dt == null) {
// continue;
// }
//
// logger.info("get record " + name + " / " + mbusId + " / " + dib);
// List<DataRecord> values = dt.getDataRecords();
// DataRecord dr = values.get(dib);
//
// Object dr_value = dr.getDataValue();
// int dr_multiplier = dr.getMultiplierExponent();
// String dr_desc = "";
// String dr_type = "";
// String dr_func = "";
// String dr_unit = "";
//
// logger.info("decode1 " + name + " / " + mbusId + " / " + dib);
//
// if (dr.getDescription() != null) {
// dr_desc = dr.getDescription().name();
// }
//
// if (dr.getDataValueType() != null) {
// dr_type = dr.getDataValueType().name();
// }
//
// if (dr.getUnit() != null) {
// dr_unit = dr.getUnit().name();
// }
//
// if (dr.getFunctionField() != null) {
// dr_func = dr.getFunctionField().name();
// }
//
// if (dr_value == null || dr_value.equals("----") || dr_value.equals("")) {
// continue;
// }
//
// logger.info("decode2 " + name + " / " + mbusId + " / " + dib);
// if (dr_type.equals("LONG")) {
// logger.info("decode long1 " + name + " / " + mbusId + " / " + dib);
// double dr_value_cv = (long) dr_value;
// if (dr_multiplier != 0) {
// dr_value_cv = (dr_value_cv * Math.pow(10, dr_multiplier));
// }
// if (dr_unit.equals("WATT_HOUR")) {
// dr_value_cv = dr_value_cv / 1000.00;
// }
// dr_value_cv = Math.round(dr_value_cv * 100.00) / 100.00;
//
// BigDecimal dr_value_bd = BigDecimal.valueOf(dr_value_cv);
// dr_value_bd = dr_value_bd.setScale(2, RoundingMode.CEILING);
// logger.info("decode long2 " + name + " / " + mbusId + " / " + dib + " / " + dr_value_bd);
// binding.getEventPublisher().postUpdate(name, new DecimalType(dr_value_bd));
// } else if (dr_type.equals("BCD")) {
// logger.info("decode bcd1 " + name + " / " + mbusId + " / " + dib);
// Bcd bcd = (Bcd) dr_value;
// binding.getEventPublisher().postUpdate(name, new DecimalType(bcd.longValue()));
// logger.info("decode bcd2 " + name + " / " + mbusId + " / " + dib);
// logger.debug("..");
// } else {
// logger.debug("..");
// }
// } catch (Exception ex) {
// logger.info("exception during read1 " + ex.getMessage() + " / " + name + " / " + mbusId + " / " + dib);
// }
// logger.debug("..");
// }
// logger.debug("mbus:readAllDP():end");
// }
//
// public void AddDpUpdate(String itemName, Type dp) {
// synchronized (updateCommand) {
// updateCommand.put(itemName, dp);
// lastUpdate = new java.util.Date();
// }
// }
//
// @Override
// public void run() {
// logger.debug("mbus:sender thread start");
//
// // as long as no interrupt is requested, continue running
// while (!interrupted) {
// try {
// Thread.sleep(2000);
// Date currentDate = new java.util.Date();
//
// logger.debug("mbus:sender thread alive:" + currentDate);
//
// if (lastUpdate == null) {
// continue;
// }
//
// long ms = currentDate.getTime() - lastUpdate.getTime();
// if (ms < 3000) {
// continue;
// }
//
// synchronized (updateCommand) {
// if (updateCommand.isEmpty()) {
// continue;
// }
//
// logger.debug("mbus:sender thread updateCommand");
//
// for (String key : updateCommand.keySet()) {
// Type dp = updateCommand.get(key);
// }
//
// updateCommand.clear();
// }
//
// } catch (Exception e) {
// logger.error("mbus:Error occured will sending update values", e);
// }
// }
// }
// }
