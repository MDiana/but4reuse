package org.but4reuse.adapters.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.helper.AdaptersHelper;
import org.but4reuse.artefactmodel.Artefact;
import org.but4reuse.artefactmodel.ArtefactModel;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * Adapters Selection Dialog
 * 
 * @author jabier.martinez
 */
public class AdaptersSelectionDialog {

	static List<IAdapter> correctAdapters = null;
	
	public static List<IAdapter> show(String title, final ArtefactModel input) {
		// Calculate adapters selected by default
		
		// Launch Progress dialog
		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		try {
			progressDialog.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					String name = input.getName();
					if(name == null){
						name = "this artefact model";
					}
					monitor.beginTask("Calculating the Adapters that can be used for " + name, 1);
					correctAdapters = AdaptersHelper.getAdapters(input, monitor);
					monitor.done();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return show(title, correctAdapters);
	}

	public static List<IAdapter> show(String title, final Artefact input) {
		// Calculate adapters selected by default
		
		// Launch Progress dialog
		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		try {
			progressDialog.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					String name = input.getName();
					if(name == null){
						name = "this artefact";
					}
					monitor.beginTask("Calculating the Adapters that can be used for " + name, 1);
					correctAdapters = AdaptersHelper.getAdapters(input, monitor);
					monitor.done();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return show(title, correctAdapters);
	}

	/**
	 * Show the dialog to the user and return the selected ones TODO this is
	 * showing everything, even the not isApplicable ones...
	 * 
	 * @param title
	 * @param valid
	 *            adapters or null (it will be used for pre-selection)
	 */
	public static List<IAdapter> show(String title, List<IAdapter> correctAdapters) {

		// No adapters
		List<IAdapter> result = new ArrayList<IAdapter>();
		if (correctAdapters==null || correctAdapters.isEmpty()) {
			MessageDialog
					.openError(Display.getCurrent().getActiveShell(), "Adapters selection",
							"Sorry, no adapter is available for your artefact types or some problems occurred. Please, check the Problems view.");
			return result;
		}

		// Prepare Adapters selection dialog
		List<IAdapter> allAdapters = AdaptersHelper.getAllAdapters();

		List<IAdapter> correctAdapters2 = new ArrayList<IAdapter>();
		for (IAdapter ca : correctAdapters) {
			for (IAdapter ea : allAdapters) {
				if (ca.getClass().equals(ea.getClass())) {
					correctAdapters2.add(ea);
				}
			}
		}

		// Dialog
		ListSelectionDialog lsd = new ListSelectionDialog(Display.getDefault().getActiveShell(), allAdapters,
				new ArrayContentProvider(), new LabelProvider() {
					public String getText(Object element) {
						return element == null ? "" : AdaptersHelper.getAdapterName((IAdapter) element);
					}

					public Image getImage(Object element) {
						ImageDescriptor img = AdaptersHelper.getAdapterIcon((IAdapter) element);
						if (img != null) {
							return img.createImage();
						}
						return null;
					}
				}, "Select the Adapter");
		lsd.setInitialSelections(correctAdapters2.toArray());
		lsd.setTitle(title);

		// Open and process
		if (lsd.open() == Window.OK && lsd.getResult() != null && lsd.getResult().length > 0) {
			for (Object a : lsd.getResult()) {
				result.add((IAdapter) a);
			}
		}
		return result;
	}

}
