package com.ibagroup.wf.intelia.compatibility;

import com.amazonaws.services.s3.AmazonS3;
import com.freedomoss.crowdcontrol.webharvest.plugin.security.service.ISecureStoreService;
import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.workfusion.utils.client.AmazonUtils;

public interface MachineVersionAdaptations {

	public static <T> T wrap(SecureEntryDTO dto, ISecureEntryDtoWrapper<T> wrapper) {
		if (dto != null) {
			return wrapper.wrap(dto.getAlias(), dto.getKey(), dto.getValue(), dto.getLastUpdateDate());
		}
		return null;
	}

	public static Class<ISecureStoreService> getISecureStoreServiceClass() {
		return ISecureStoreService.class;
	}

	public static AmazonS3 getS3ClientConnection(String s3AccessKey, String s3SecretKey, String s3EndpointUrl) {
		return AmazonUtils.createS3Client(s3AccessKey, s3SecretKey, s3EndpointUrl, null);
	}

}
