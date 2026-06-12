package com.wonkglorg.utilitylib.config.provider;

import java.io.InputStream;

@FunctionalInterface
public interface ResourceProvider{
	
	InputStream getResource();
	
}