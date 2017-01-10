package com.deerbelling.expr;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class CountMethodDependencies extends ExprEditor {

	public CountMethodDependencies(Log mavenLog, Map<String, Integer> countDepMap, Map<String, Integer> countExtLibMap,
			String projectOutputDirectory, String projectTestOutputDirectory) {
		log = mavenLog;
		countDependenciesMap = countDepMap;
		countExternalLibMap = countExtLibMap;
		outputDirectory = projectOutputDirectory;
		testOutputDirectory = projectTestOutputDirectory;
	}

	private Log log;

	private CtBehavior behavior;

	// Voir pour du streaming la gestion du classloader
	private URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();

	// TODO voir pour du streaming la gestion de la réintégration des map &
	// gestionn nb thread
	private Map<String, Integer> countDependenciesMap;
	private Map<String, Integer> countExternalLibMap;

	private String outputDirectory;
	private String testOutputDirectory;

	public void setBehavior(CtBehavior behavior) {
		this.behavior = behavior;
	}

	private Log getLog() {
		return log;
	}

	@Override
	public void edit(MethodCall m) throws CannotCompileException {

		if (m != null && StringUtils.isNotBlank(m.getClassName())) {

			getLog().debug("** callee ClassName : " + m.getClassName());
			getLog().debug("** callee MethodName : " + m.getMethodName());
			getLog().debug("** callee Signature : " + m.getSignature());

			count(m.getClassName());

		} else {
			getLog().error(" * 1 reference not found in : " + behavior.getLongName());
		}
		getLog().debug("********************************");
	}

	public void countAnno(CtBehavior ctBehavior) throws CannotCompileException {
		MethodInfo mi = ctBehavior.getMethodInfo2();
		AnnotationsAttribute annoInvisibleTag = (AnnotationsAttribute) mi
				.getAttribute(AnnotationsAttribute.invisibleTag);
		if (annoInvisibleTag != null) {
			for (Annotation annotation : annoInvisibleTag.getAnnotations()) {
				count(annotation.getTypeName());
			}
		}
		AnnotationsAttribute annoVisibleTag = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
		if (annoVisibleTag != null) {
			for (Annotation annotation : annoVisibleTag.getAnnotations()) {
				count(annotation.getTypeName());
			}
		}
	}

	public void countAnno(CtClass ctClass) throws CannotCompileException {
		AnnotationsAttribute annoInvisibleTag = (AnnotationsAttribute) ctClass.getClassFile()
				.getAttribute(AnnotationsAttribute.invisibleTag);
		if (annoInvisibleTag != null) {
			for (Annotation annotation : annoInvisibleTag.getAnnotations()) {
				count(annotation.getTypeName());
			}
		}
		AnnotationsAttribute annoVisibleTag = (AnnotationsAttribute) ctClass.getClassFile()
				.getAttribute(AnnotationsAttribute.visibleTag);
		if (annoVisibleTag != null) {
			for (Annotation annotation : annoVisibleTag.getAnnotations()) {
				count(annotation.getTypeName());
			}
		}
	}

	public void countAnno(CtField ctField) throws CannotCompileException {
		FieldInfo fieldInfo = ctField.getFieldInfo2();
		AnnotationsAttribute annoInvisibleTag = (AnnotationsAttribute) fieldInfo
				.getAttribute(AnnotationsAttribute.invisibleTag);
		if (annoInvisibleTag != null) {
			for (Annotation annotation : annoInvisibleTag.getAnnotations()) {
				count(annotation.getTypeName());
			}
		}
		AnnotationsAttribute annoVisibleTag = (AnnotationsAttribute) fieldInfo
				.getAttribute(AnnotationsAttribute.visibleTag);
		if (annoVisibleTag != null) {
			for (Annotation annotation : annoVisibleTag.getAnnotations()) {
				count(annotation.getTypeName());
			}
		}

	}

	public void count(String className) throws CannotCompileException {

		if (className != null && !className.startsWith("java.") && !className.startsWith("javax.")
				&& !className.startsWith("jdk.") && !className.startsWith("org.jcp.")
				&& !className.startsWith("org.omg.") && !className.startsWith("org.w3c.")
				&& !className.startsWith("com.sun.") && !className.startsWith("com.oracle.")
				&& !className.startsWith("org.xml.sax")) {

			if (behavior != null && behavior.getDeclaringClass() != null
					&& StringUtils.isNotBlank(behavior.getDeclaringClass().getName())
					&& !className.equals(behavior.getDeclaringClass().getName())) {
				getLog().debug("** callee DeclaringClass : " + behavior.getDeclaringClass().getName());
			}

			String classFilePath = className.replace('.', '/') + ".class";
			URL url = urlClassLoader.getResource(classFilePath);

			getLog().debug("** find in : " + url);

			if (url != null) {
				String file = url.getFile();
				int endIndex = (file.contains("!")) ? file.lastIndexOf('!') : file.length();
				String lib = file.substring((file.lastIndexOf(':') + 1), endIndex);

				if (lib != null && lib.endsWith("rt.jar")) {
					System.out.println("rt.jar : " + className);
				}

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

			getLog().debug("********************************");
		}
	}

}
