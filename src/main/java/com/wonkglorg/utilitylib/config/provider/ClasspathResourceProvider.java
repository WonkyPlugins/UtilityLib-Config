package com.wonkglorg.utilitylib.config.provider;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class ClasspathResourceProvider implements ResourceProvider{
	
	private final ClassLoader classLoader;
	private final Path path;
	
	public ClasspathResourceProvider(ClassLoader classLoader, Path path) {
		this.classLoader = classLoader;
		this.path = path;
	}
	
	@Override
	public InputStream getResource() {
		return classLoader.getResourceAsStream(path.toString().replace(File.separatorChar, '/'));
	}
}