package com.telus.credit.model.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * these metadata types will be used in metadata attributes in readDB
 */
public enum MetaDataType {
	DFLT("Default"), 
	PSP("PSP"), 
	PRV("PRV"), 
	SIN("SIN"), 
	CC("CC"), 
	DL("DL"),
	HC("HC"), 
	BIC("BIC"), 
	CRA("CRA"),
	NSJ("NSJ"), 
	QST("QST"), 
	// Below ones are Not encoded
	CUSTOMER_ID("customerId"),
	BIRTH_DATE("birthDate"), 
	POSTAL_CODE("postalCode"), 
	LAST_NAME("lastName");

	MetaDataType(final String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

	private final String desc;

	private static final Map<String, MetaDataType> lookup = new HashMap<>();

	private static final Set<String> encodedTypes = new HashSet<>();

	static {
		for (final MetaDataType type : MetaDataType.values()) {
			lookup.put(StringUtils.lowerCase(type.desc), type);
		}
		encodedTypes.add(MetaDataType.PSP.name());
		encodedTypes.add(MetaDataType.PRV.name());
		encodedTypes.add(MetaDataType.SIN.name());
		encodedTypes.add(MetaDataType.CC.name());
		encodedTypes.add(MetaDataType.DL.name());
		encodedTypes.add(MetaDataType.HC.name());
		encodedTypes.add(MetaDataType.BIC.name());
		encodedTypes.add(MetaDataType.CRA.name());
		encodedTypes.add(MetaDataType.NSJ.name());
		encodedTypes.add(MetaDataType.QST.name());
	}

	public static MetaDataType getIdentificationType(String descOrName) {
		return lookup.get(StringUtils.lowerCase(descOrName));

	}

	public static boolean isIdentificationTypeEncoded(String enumName) {
		return encodedTypes.contains(enumName);

	}
	
	@Override
	public String toString() {
		return desc;
	}
}
