package org.but4reuse.adapters.eclipse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
//import org.apache.commons.io.FileUtils;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.eclipse.plugin_infos_extractor.utils.DependenciesBuilder;
import org.but4reuse.adapters.eclipse.plugin_infos_extractor.utils.PluginInfosExtractor;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Eclipse adapter
 * @author Fjorilda Gjermizi
 * @author Krista Drushku
 * @author Jason CHUMMUN
 * @author Diana MALABARD
 */
public class EclipseAdapter implements IAdapter {

	/**
	 * Cette m�thode permet de d�finir si l'artefact est adaptable par le
	 * EclipseAdapter
	 */

	@Override
	public boolean isAdaptable(URI uri, IProgressMonitor monitor) {
		File file = FileUtils.getFile(uri);
		if (file.isDirectory()) {
			File eclipse = new File(file.getAbsolutePath() + "/eclipse.exe");
			if (eclipse.exists()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * Provides the atoms (plugins) this distribution is made of
	 * 
	 * @param uri
	 *            URI of the distribution
	 * @param monitor
	 */
	@Override
	public List<IElement> adapt(URI uri, IProgressMonitor monitor) {
		List<IElement> elements = new ArrayList<IElement>();
		File file = FileUtils.getFile(uri);
		if (file != null && file.exists() && file.isDirectory()) {
			elements.addAll(adaptFolder(file.getAbsolutePath() + "/dropins",
					monitor));
			elements.addAll(adaptFolder(file.getAbsolutePath() + "/plugins",
					monitor));
			// For each element, build the dependencies map, depending
			// on the plugins installed in the considered distribution
			// and the values retrieved in its RequiredBundle field
			for (IElement elem : elements) {
				DependenciesBuilder builder = new DependenciesBuilder(
						(PluginElement) elem, elements);
				builder.run();
			}
			// Test
			//			for (IElement elem : elements) {
			//				PluginElement plugin = (PluginElement) elem;
			//				if (plugin.getDependencies().get(
			//						AbstractElement.MAIN_DEPENDENCY_ID) == null) {
			//					System.out.println("Le plugin "
			//							+ plugin.getPluginSymbName()
			//							+ " n'a aucune d�pendance.");
			//				} else {
			//					System.out.println("Le plugin "
			//							+ plugin.getPluginSymbName()
			//							+ " a "
			//							+ plugin.getDependencies()
			//									.get(AbstractElement.MAIN_DEPENDENCY_ID)
			//									.size()+" d�pendances.");
			//				}
			//			}
		}
		return elements;
	}

	/**
	 * Searches for plugins in the given folder
	 * 
	 * @param uri
	 *            URI of an Eclipse folder
	 */
	private List<IElement> adaptFolder(String uri, IProgressMonitor monitor) {
		List<IElement> elements = new ArrayList<IElement>();
		File file = new File(uri);
		File[] fichiers = file.listFiles();

		for (int i = 0; i < fichiers.length; i++) {

			// System.out.println("analyse de l'�l�ment "+fichiers[i].getName());

			if (fichiers[i].isDirectory()) {

				// System.out.println("plugin sous forme de dossier : "+fichiers[i].getAbsolutePath());

				try {

					elements.add(PluginInfosExtractor
							.getPluginInfosFromManifest(fichiers[i]
									.getAbsolutePath()
									+ "/META-INF/MANIFEST.MF"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (fichiers[i].getPath().endsWith(".jar")) {
				try {
					elements.add(PluginInfosExtractor
							.getPluginInfosFromJar(fichiers[i]
									.getAbsolutePath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return elements;
	}

	@Override
	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {
		System.out.println("Enter construct");
//adresse en dur donc � changer d�s qu'on veut tester la m�thode construct
		File dest= new File("C:/Users/getter/Documents/runtime-EclipseApplication/test/plugins/");
		deleteFolder(dest);
		// File dest = new File("C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test/plugins/");
		// System.out.println(dest.mkdirs());
		for (IElement element : elements) {
			System.out.println("************* ENTRE element");
			URI uri2 = uri.resolve(uri);
			System.out.println(uri2);

			if (!monitor.isCanceled()) {
				monitor.subTask(element.getText());
				if (element instanceof PluginElement) {
					System.out.println("*********    PluginELEMENT     **********");

					PluginElement fileElement = (PluginElement) element;
					File file = new File(fileElement.getAbsolutePath());
					if(file.isDirectory()){
						 new File("C:/Users/getter/Documents/runtime-EclipseApplication/test/plugins/"+file.getName()).mkdir();
						File dossierTarget = new File("C:/Users/getter/Documents/runtime-EclipseApplication/test/plugins/"+file.getName());
						System.out.println("Directory :"+file.getAbsolutePath());
						try{
						    org.apache.commons.io.FileUtils.copyDirectory(file, dossierTarget);
						}catch(IOException e){
							e.printStackTrace();
						}
						//new File("C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/" + file.getName()).mkdir();
//						File dossierPlugin = new File("C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/" + file.getName());
//						for(File plugins : dossierPlugin.listFiles()){
//							String addr = plugins.getAbsolutePath();
//							String nomPlugin = tokenize(addr);
//							System.out.println("plugin : " +  addr);
//							try{
//								FileUtils.downloadFileFromURL(new URL("file:///"+addr), new File("C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/"+ dossierPlugin.getName()+"/" +nomPlugin));
//							}catch(IOException e){
//								e.printStackTrace();
//							}
//						}
						
					}
					
					else{
						try {

							String pluginAddr = fileElement.getAbsolutePath();
							//System.out.println("plugin : " +  pluginAddr);

							String pluginName = tokenize(pluginAddr);
							//	URI newDirectoryURI = uri.resolve(pluginAddr);
							//	System.out.println("uri ==========="+ newDirectoryURI.toString());
							//System.out.println("Eclipse i ri ------------------------"+newDirectoryURI+"plugins/"+pluginName);
							FileUtils.downloadFileFromURL(new URL("file:///"+pluginAddr), new File("C:/Users/getter/Documents/runtime-EclipseApplication/test/plugins/S"+pluginName));
							//System.out.println("pass");

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}
			monitor.worked(1);
		}

	}

	
	private void construct_rec(File dossier, IProgressMonitor monitor){
		System.out.println("Directory :"+dossier.getAbsolutePath());
		String directoryName="";
		if(tokenize(dossier.getParent()).equalsIgnoreCase("plugins")){
		
			new File("C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/" + dossier.getName()).mkdir();
			directoryName ="C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/" + dossier.getName();
		}
		else{
			new File(dossier.getName() ).mkdir();
			directoryName = "C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/" + dossier.getName();
		}
		
		File dossierPlugin = new File("C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/" + dossier.getName());
		for(File plugins : dossierPlugin.listFiles()){
			String addr = plugins.getAbsolutePath();
			String nomPlugin = tokenize(addr);
			System.out.println("plugin : " +  addr);
			try{
				FileUtils.downloadFileFromURL(new URL("file:///"+addr), new File("C:/UPMC/M2/GPSTL/fevrier/runtime-EclipseApplication/test1/plugins/"+ dossierPlugin.getName()+"/" +nomPlugin));
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
	}

	private String tokenize (String addresse){
		String resultat =null ;
		StringTokenizer st = new StringTokenizer(addresse, "\\");
		while (st.hasMoreTokens()){
			resultat = st.nextToken().toString();
		}
		return resultat;
	}
	private void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}

	}
}
