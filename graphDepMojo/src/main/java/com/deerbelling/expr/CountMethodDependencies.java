package com.deerbelling.expr;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class CountMethodDependencies extends ExprEditor {

	public CountMethodDependencies(Log mavenLog, CtMethod methodCalled, Map<String, Integer> countDepMap,
			Map<String, Integer> countExtLibMap, String projectOutputDirectory, String projectTestOutputDirectory) {
		log = mavenLog;
		method = methodCalled;
		countDependenciesMap = countDepMap;
		countExternalLibMap = countExtLibMap;
		outputDirectory = projectOutputDirectory;
		testOutputDirectory = projectTestOutputDirectory;
	}

	private Log log;
	private CtMethod method;

	// Voir pour du streaming la gestion du classloader
	private URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();

	// TODO voir pour du streaming la gestion de la réintégration des map &
	// gestionn nb thread
	private Map<String, Integer> countDependenciesMap;
	private Map<String, Integer> countExternalLibMap;

	private String outputDirectory;
	private String testOutputDirectory;

	private Log getLog() {
		return log;
	}

	@Override
	public void edit(MethodCall m) throws CannotCompileException {

		if (m != null && StringUtils.isNotBlank(m.getClassName())) {
			
			getLog().debug("** callee ClassName : " + m.getClassName());
			getLog().debug("** callee MethodName : " + m.getMethodName());
			getLog().debug("** callee Signature : " + m.getSignature());

			String className = m.getClassName();

			if (method.getDeclaringClass() != null && StringUtils.isNotBlank(method.getDeclaringClass().getName())
					&& !className.equals(method.getDeclaringClass().getName())) {
				className = method.getDeclaringClass().getName();
				getLog().debug("** callee DeclaringClass : " + className);
			}

			String classFilePath = m.getClassName().replace('.', '/') + ".class";
			URL url = urlClassLoader.getResource(classFilePath);

			getLog().debug("** find in : " + url);

			if (url != null) {
				String file = url.getFile();
				int endIndex = (file.contains("!")) ? file.lastIndexOf('!') : file.length();
				String lib = file.substring((file.lastIndexOf(':') + 1), endIndex);
				if (lib.endsWith(".class")) {
					if (lib.startsWith(outputDirectory)) {
						lib = outputDirectory;
						if (countDependenciesMap.containsKey(lib)) {
							int count = countDependenciesMap.get(lib).intValue() + 1;
							countDependenciesMap.replace(lib, count);
						} else {
							countDependenciesMap.put(lib, 1);
						}
					} else if (lib.startsWith(testOutputDirectory)) {
						lib = testOutputDirectory;
						if (countDependenciesMap.containsKey(lib)) {
							int count = countDependenciesMap.get(lib).intValue() + 1;
							countDependenciesMap.replace(lib, count);
						} else {
							countDependenciesMap.put(lib, 1);
						}
					} else {
						getLog().error(" * lib not found : " + lib);
					}
				} else {
					getLog().debug("** lib name : " + lib);

					if (countDependenciesMap.containsKey(lib)) {
						int count = countDependenciesMap.get(lib).intValue() + 1;
						countDependenciesMap.replace(lib, count);
					} else if (countExternalLibMap.containsKey(lib)) {
						int count = countExternalLibMap.get(lib).intValue() + 1;
						countExternalLibMap.replace(lib, count);
					} else {
						countExternalLibMap.put(lib, 1);
					}
				}
			}

		} else {
			getLog().error(" * 1 reference not found in : " + method.getLongName());
		}
		getLog().debug("********************************");
	}

}
