package com.deerbelling;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @requiresDependencyResolution test
 * 
 */
public class MyMojo extends AbstractMojo {

	// private static final String[] BASEDIR_EXPRESSIONS = { "${basedir}",
	// "${pom.basedir}", "${project.basedir}" };

	private Map<String, List<File>> classMap = new HashMap<String, List<File>>();

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	public void execute() throws MojoExecutionException {

		try {

			List compileCpElem = project.getCompileClasspathElements();

			Reflections reflections = new Reflections("my.package.prefix");
			

			for (Object src : compileCpElem) {
				getLog().info("getClass : " + src.getClass());
				getLog().info("toString : " + src.toString());

				if (src instanceof String) {

					String dirname = (String) src;

					List<File> files = new ArrayList<File>();

					findFile(new File(dirname), files);

					if (!files.isEmpty()) {
						classMap.put(dirname, files);
					}
				}
			}

			for (String dirname : classMap.keySet()) {

				for (File file : classMap.get(dirname)) {

					getLog().info("   dirname : " + dirname);
					getLog().info("   file : " + file.getPath());
					getLog().info("   file name : " + file.getName());
					getLog().info("   file.getAbsolutePath() : " + file.getAbsolutePath());
					getLog().info("   file.getCanonicalPath() : " + file.getCanonicalPath());

					// String classname = file.getName().substring(0,
					// file.getName().indexOf('.'));
					// getLog().info(" class name : " + classname);

					String classname = file.getPath().substring(dirname.length(), file.getPath().lastIndexOf('.'));

					classname = (classname.startsWith(File.separator))
							? classname.substring(1).replace(File.separatorChar, '.')
							: classname.replace(File.separatorChar, '.');

					getLog().info("         class name : " + classname);

					Class clazz = getClassLoader().loadClass(classname);

					getLog().info("         haaaaaaaaa : " + clazz.getName());
					
					getLog().info("         declared classes : " + Arrays.asList(clazz.getDeclaredClasses()));
					
					getLog().info("         classes : "+Arrays.asList(clazz.getClasses()));
					getLog().info("         getTypeParameters : "+Arrays.asList(clazz.getTypeParameters()));
					
					
					getLog().info("res2 : "+getClassLoader().getResource("java/io/IOException.class"));
					getLog().info("res2 : "+getClassLoader().getResource("java/io"));
					
				}
			}

		} catch (DependencyResolutionRequiredException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	private void findFile(File file, List<File> classList) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child != null)
					findFile(child, classList);
			}

		} else if (file.isFile() && file.getPath().endsWith(".class")) {
			classList.add(file);
		}
	}

	private URLClassLoader getClassLoader() throws MojoExecutionException {
		try {
			List<String> classpathElements = project.getCompileClasspathElements();
			classpathElements.add(project.getBuild().getOutputDirectory());
			classpathElements.add(project.getBuild().getTestOutputDirectory());
			URL urls[] = new URL[classpathElements.size()];

			for (int i = 0; i < classpathElements.size(); ++i) {
				urls[i] = new File((String) classpathElements.get(i)).toURI().toURL();
			}
			return new URLClassLoader(urls, getClass().getClassLoader());
		} catch (Exception e)// gotta catch em all
		{
			throw new MojoExecutionException("Couldn't create a classloader.", e);
		}
	}
}
