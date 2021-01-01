package org.openhab.binding.siemenshvac.internal.Metadata;

import java.util.List;

public class SiemensHvacMetadataDataPoint extends SiemensHvacMetadata {
    private int dptId = -1;
    private String dptType = null;
    private String dptUnit = null;
    private String min = null;
    private String max = null;
    private String resolution = null;
    private String fieldWitdh = null;
    private String decimalDigits = null;
    private Boolean detailsResolved = false;
    private String dialogType = null;
    private String maxLength = null;
    private List<SiemensHvacMetadataPointChild> child = null;

    public String getDptType() {
        return dptType;
    }

    public void setDptType(String dptType) {
        this.dptType = dptType;
    }

    public List<SiemensHvacMetadataPointChild> getChild() {
        return child;
    }

    public void setChild(List<SiemensHvacMetadataPointChild> child) {
        this.child = child;
    }

    public int getDptId() {
        return dptId;
    }

    public void setDptId(int dptId) {
        this.dptId = dptId;
    }

    public String getDptUnit() {
        return dptUnit;
    }

    public void setDptUnit(String dptUnit) {
        this.dptUnit = dptUnit;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public String getDialogType() {
        return dialogType;
    }

    public void setDialogType(String dialogType) {
        this.dialogType = dialogType;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getFieldWitdh() {
        return fieldWitdh;
    }

    public void setFieldWitdh(String fieldWitdh) {
        this.fieldWitdh = fieldWitdh;
    }

    public String getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(String decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public Boolean getDetailsResolved() {
        return detailsResolved;
    }

    public void setDetailsResolved(Boolean detailsResolved) {
        this.detailsResolved = detailsResolved;
    }

    /*
     * public void resolveDptDetails(JSONObject result) {
     * if (result == null) {
     * return;
     * }
     */
    /*
     * JSONObject desc = (JSONObject) result.get("Description");
     *
     * if (desc != null) {
     * this.dptType = (String) desc.get("Type");
     *
     * if (dptType.equals("Enumeration")) {
     * JSONArray enums = (JSONArray) desc.get("Enums");
     * child = new ArrayList<siemensMetadataPointChild>();
     *
     * for (Object obj : enums) {
     * JSONObject entry = (JSONObject) obj;
     *
     * siemensMetadataPointChild ch = new siemensMetadataPointChild();
     * ch.setText((String) entry.get("Text"));
     * ch.setValue((String) entry.get("Value"));
     * ch.setIsActive((String) entry.get("IsCurrentValue"));
     * child.add(ch);
     * }
     * } else if (dptType.equals("Numeric")) {
     * this.dptUnit = (String) desc.get("Unit");
     * this.min = (String) desc.get("Min");
     * this.max = (String) desc.get("Max");
     * this.resolution = (String) desc.get("Resolution");
     * this.fieldWitdh = (String) desc.get("FieldWitdh");
     * this.decimalDigits = (String) desc.get("DecimalDigits");
     * } else if (dptType.equals("String")) {
     *
     * this.dialogType = (String) desc.get("DialogType");
     * this.maxLength = (String) desc.get("MaxLength");
     * } else if (dptType.equals("RadioButton")) {
     * JSONArray buttons = (JSONArray) desc.get("Buttons");
     *
     * child = new ArrayList<siemensMetadataPointChild>();
     *
     * for (Object obj : buttons) {
     * JSONObject button = (JSONObject) obj;
     *
     * siemensMetadataPointChild ch = new siemensMetadataPointChild();
     * ch.setOpt0((String) button.get("TextOpt0"));
     * ch.setOpt1((String) button.get("TextOpt1"));
     * ch.setIsActive((String) button.get("IsActive"));
     * child.add(ch);
     *
     * String signifiance = (String) button.get("Significance");
     * }
     * } else if (dptType.equals("DateTime")) {
     * System.out.println("");
     * } else if (dptType.equals("TimeOfDay")) {
     * System.out.println("");
     * } else if (dptType.equals("Scheduler")) {
     * System.out.println("");
     * } else if (dptType.equals("Calendar")) {
     * System.out.println("");
     * } else {
     * System.out.println("");
     * }
     * detailsResolved = true;
     * }
     */
    // }

}
