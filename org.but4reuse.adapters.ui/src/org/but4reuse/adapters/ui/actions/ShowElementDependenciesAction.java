package org.but4reuse.adapters.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.eclipse.EclipseAdapter;
import org.but4reuse.adapters.helper.AdaptersHelper;
import org.but4reuse.adapters.ui.AdaptersSelectionDialog;
import org.but4reuse.adapters.ui.views.PluginContentProvider;
import org.but4reuse.adapters.ui.views.PluginLabelProvider;
import org.but4reuse.artefactmodel.Artefact;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

/** 
 * ShowElementDependenciesAction
 * @author Selma Sadouk
 * @author Julia Wisniewski
 */
public class ShowElementDependenciesAction implements IObjectActionDelegate {

	Artefact artefact = null;
	List<IAdapter> adap;
    List<IElement> elements;
	
	@Override
	public void run(IAction action) {
		artefact = null;
		if (selection instanceof IStructuredSelection) {
			for (Object art : ((IStructuredSelection) selection).toArray()) {
				if (art instanceof Artefact) {
					artefact = ((Artefact) art);

					// Adapter selection by user
					adap = AdaptersSelectionDialog.show("Show Element Dependencies", artefact);

					if (!adap.isEmpty()) {


						// Launch Progress dialog
						ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent()
								.getActiveShell());

						try {
							progressDialog.run(true, true, new IRunnableWithProgress() {
								@Override
								public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {

									int totalWork = 1;
									monitor.beginTask("Calculating dependencies of " + artefact.getArtefactURI(), totalWork);

									for (IAdapter adapter : adap) {
										if(adapter instanceof EclipseAdapter) {
											elements = AdaptersHelper.getElements(artefact, adapter);
										}
									}
									monitor.worked(1);
									monitor.done();
								}
							});

							String name = artefact.getName();
							if (name == null || name.length() == 0) {
								name = artefact.getArtefactURI();
							}

							Display.getDefault().syncExec(
									new Runnable(){
										public void run(){

											Display d = Display.getCurrent();
											Shell shell = new Shell();
											shell.setText("Visualization");
											shell.setLayout(new FillLayout(SWT.VERTICAL));
											shell.setSize(700,700);
										
											GraphViewer viewer = new GraphViewer(shell,SWT.NONE);
											
											PluginContentProvider contentProvider = new PluginContentProvider();
											PluginLabelProvider labelProvider = new PluginLabelProvider();
											viewer.setContentProvider(contentProvider);
											viewer.setLabelProvider(labelProvider);
											
											 // Definition of the layout 
									        viewer.setLayoutAlgorithm(new 
									                SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING)); 
									        viewer.applyLayout(); 
											
											viewer.setInput(elements); 
											

											shell.open();
											while (!shell.isDisposed()) {
												while (!d.readAndDispatch()) {
													d.sleep();
												}
											}
										} 
									}
									);
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	ISelection selection;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

}
