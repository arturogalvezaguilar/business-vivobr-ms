package com.lamark.business.vivobr.core.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.lamark.business.vivobr.core.request.MORequestJboss;
import com.lamark.business.vivobr.core.service.InitService;
import com.lamark.business.vivobr.core.service.MOService;

@Singleton
public class MOServiceImpl implements MOService {

	@Inject
	InitService initConfiguration;



	@Override
	public void process(MORequestJboss requestJBoss) {
		// TODO Auto-generated method stub
		String siteName = this.initConfiguration.getSiteFromNumber(requestJBoss.getShortCode());
		
	}

}
