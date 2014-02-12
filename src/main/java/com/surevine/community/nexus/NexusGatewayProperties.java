package com.surevine.community.nexus;

import java.util.ResourceBundle;

public enum NexusGatewayProperties {

	NEXUS_REPOSITORIES,
	GATEWAY_HOST,
	GATEWAY_PORT,
	GATEWAY_CONTEXT;
	
	private static final ResourceBundle BUNDLE;
	
	static {
		BUNDLE = ResourceBundle.getBundle("nexus-gateway");
	}
	
	public static String get(final NexusGatewayProperties property) {
		return BUNDLE.getString(String.format("nexus-gateway.%s",
				property.toString().toLowerCase().replaceAll("_", ".")));
	}
}
