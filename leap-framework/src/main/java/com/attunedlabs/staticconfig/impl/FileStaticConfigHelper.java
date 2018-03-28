package com.attunedlabs.staticconfig.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.staticconfig.StaticConfigDuplicateNameofFileException;

public class FileStaticConfigHelper {

	/**
	 * method to fetch the fields from RequestContext and create a namespace out
	 * of it
	 * 
	 * @return
	 * 
	 */
	public String createNamespaceFromRequestContext(RequestContext reqCtx) {
		StringBuffer reqCtxNamespace;
		reqCtxNamespace = new StringBuffer();
		reqCtxNamespace.append("::" + reqCtx.getTenantId() + "::");
		reqCtxNamespace.append(reqCtx.getSiteId() + "::");
		reqCtxNamespace.append(reqCtx.getFeatureGroup() + "::");
		reqCtxNamespace.append(reqCtx.getFeatureName() + "::");
		reqCtxNamespace.append(reqCtx.getImplementationName() + "::");
		reqCtxNamespace.append(reqCtx.getVendor() + "::");
		reqCtxNamespace.append(reqCtx.getVersion());
		return reqCtxNamespace.toString();
	}

	/**
	 * method to fetch the fields from configurationContext and create
	 * namespaces out of it.
	 * 
	 * @param ctx
	 *            - Configuration context Passed as a parameter
	 */
	public String createNamespaceFromConfigurationContext(ConfigurationContext ctx) {
		StringBuffer ctxNamespace;
		ctxNamespace = new StringBuffer();
		ctxNamespace.append("::" + ctx.getTenantId() + "::");
		ctxNamespace.append(ctx.getSiteId() + "::");
		if (ctx.getFeatureGroup() != null) {
			ctxNamespace.append(ctx.getFeatureGroup() + "::");
			ctxNamespace.append("::");
		}
		if (ctx.getFeatureName() != null) {
			ctxNamespace.append(ctx.getFeatureName() + "::");
			ctxNamespace.append("::");
		}
		if (ctx.getVendorName() != null) {
			ctxNamespace.append(ctx.getVendorName());
			ctxNamespace.append("::");
		}
		if (ctx.getVersion() != null) {
			ctxNamespace.append(ctx.getVersion());
		}
		return ctxNamespace.toString();// ..
										// 23::site::sacGroup::feature::vendor::version
	}

	/**
	 * check on which OS the code is running, based on which the delimeter ":"
	 * will change into "/" OR "\"
	 * 
	 * @param namespace
	 */
	public String changeNamespaceintoPath(String namespace) {
		return namespace.replace("::", "/");
	}

	/**
	 * writing file into the path using channels
	 * 
	 * @throws IOException
	 * @throws StaticConfigDuplicateNameofFileException
	 */
	public void write2File(String staticConfigName, String configContent, Path baseDirectory)
			throws IOException, StaticConfigDuplicateNameofFileException {
		byte[] byteArray = configContent.getBytes();
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
		// byteChannelWrite(byteBuffer,staticConfigName);
		// fileChannelRead();
		fileChannelWrite(byteBuffer, staticConfigName, baseDirectory.toString());
	}

	/**
	 * method to write into file using channel
	 * 
	 * @param byteBuffer
	 * @throws StaticConfigDuplicateNameofFileException
	 */
	public void fileChannelWrite(ByteBuffer byteBuffer, String staticConfigName, String directoryTowrite)
			throws IOException, StaticConfigDuplicateNameofFileException {

		Set<StandardOpenOption> options = new HashSet<StandardOpenOption>();
		options.add(StandardOpenOption.CREATE);
		options.add(StandardOpenOption.APPEND);

		Path path = Paths.get(directoryTowrite + "/" + staticConfigName);
		try {
			Files.createFile(path);
			FileChannel fileChannel = FileChannel.open(path, options);
			fileChannel.write(byteBuffer);
			fileChannel.close();
		} catch (FileAlreadyExistsException e) {
			throw new StaticConfigDuplicateNameofFileException("file already exists");
		}
	}
}