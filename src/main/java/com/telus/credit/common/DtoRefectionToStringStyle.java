package com.telus.credit.common;

import org.apache.commons.lang3.builder.ToStringStyle;

public class DtoRefectionToStringStyle extends ToStringStyle {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DtoRefectionToStringStyle() {
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
    }

    @Override
    public void appendFieldStart(StringBuffer buffer, String fieldName) {
        if (shouldAppend(fieldName)) {
            super.appendFieldStart(buffer, fieldName);
        }
    }

    @Override
    public void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (shouldAppend(fieldName)) {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    private boolean shouldAppend(String fieldName) {
        return !fieldName.endsWith("Dirty");
    }
}
