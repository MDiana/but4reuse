package org.but4reuse.feature.constraints.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.BlockElement;
import org.but4reuse.adaptedmodel.ElementWrapper;
import org.but4reuse.adapters.IDependencyObject;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.eclipse.PluginElement;
import org.but4reuse.feature.constraints.IConstraintsDiscovery;
import org.but4reuse.featurelist.FeatureList;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Binary relations constraints discovery A structural constraints discovery
 * between pairs of blocks
 * 
 * @author jabier.martinez
 * 
 */
public class BinaryRelationConstraintsDiscovery implements IConstraintsDiscovery {

	@Override
	public String discover(FeatureList featureList, AdaptedModel adaptedModel, Object extra, IProgressMonitor monitor) {

		String result = "";

		// for binary relations we explore the matrix n*n where n is the number
		// of blocks. We ignore the matrix diagonal so it is n*n - n for
		// requires and (n*n-n)/2 for mutual exclusion
		int n = adaptedModel.getOwnedBlocks().size();
		monitor.beginTask("Binary Relation Constraints discovery", (n * n - n) + ((n * n - n) / 2));

		// Block Level
		// TODO feature level
		for (int y = 0; y < n; y++) {
			for (int x = 0; x < n; x++) {
				if (x != y) {
					Block b1 = adaptedModel.getOwnedBlocks().get(y);
					Block b2 = adaptedModel.getOwnedBlocks().get(x);
					monitor.subTask("Checking Requires relations of " + b1.getName() + " with " + b2.getName());
					// check monitor
					if (monitor.isCanceled()) {
						return result;
					}
					// here we have all binary combinations A and B, B and A
					// etc.
					// requires b1 -> b2
					if (blockRequiresAnotherBlockB(b1, b2)) {
						// TODO provide more info about the origin of the
						// constraint, the involved elements for example
						result = result + b1.getName() + " requires " + b2.getName() + "\n";
					}
					monitor.worked(1);
				}
			}
		}

		for (int y = 0; y < n; y++) {
			for (int x = 0; x < n; x++) {
				// mutual exclusion, not(b1 and b2), as it is mutual we do
				// not need to check the opposite
				if (x != y && y < x) {
					Block b1 = adaptedModel.getOwnedBlocks().get(y);
					Block b2 = adaptedModel.getOwnedBlocks().get(x);
					monitor.subTask("Checking Mutual Exclusion relations of " + b1.getName() + " with " + b2.getName());
					// check monitor
					if (monitor.isCanceled()) {
						return result;
					}
					// mutual exclusion
					if (blockExcludesAnotherBlock(b1, b2)) {
						result = result + b1.getName() + " mutually excludes " + b2.getName() + "\n";
					}
					monitor.worked(1);
				}
			}
		}
		monitor.done();
		return result;
	}

	/**
	 * exists e in b1 : exists de in e.dependencies : de containedIn b2
	 * 
	 * @param b1
	 * @param b2
	 * @return
	 */
	public boolean blockRequiresAnotherBlockB(Block b1, Block b2) {
		for (BlockElement e : b1.getOwnedBlockElements()) {
			List<IDependencyObject> de = getAllDependencies(e);
			for (BlockElement b2e : b2.getOwnedBlockElements()) {
				for (ElementWrapper elementW2 : b2e.getElementWrappers()) {
					if (de.contains(elementW2.getElement())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * exists e1 in b1, exists e2 in b2 : exists de in (e1.dependencies
	 * intersection e2.dependencies) and de.maxDependencies <=1
	 * 
	 * @param b1
	 * @param b2
	 * @return
	 */
	private boolean blockExcludesAnotherBlock(Block b1, Block b2) {
		// Create the global maps of dependency ids and dependency objects
		Map<String, List<IDependencyObject>> map1 = new HashMap<String, List<IDependencyObject>>();
		Map<String, List<IDependencyObject>> map2 = new HashMap<String, List<IDependencyObject>>();
		for (BlockElement e1 : b1.getOwnedBlockElements()) {
			map1 = getDepedencyTypesAndPointedObjects(map1, e1);
		}
		for (BlockElement e2 : b2.getOwnedBlockElements()) {
			map2 = getDepedencyTypesAndPointedObjects(map2, e2);
		}
		for (String key : map1.keySet()) {
			List<IDependencyObject> pointed1 = map1.get(key);
			List<IDependencyObject> pointed2 = map2.get(key);
			if (pointed2 == null) {
				break;
			}
			for (IDependencyObject o : pointed1) {
				if (pointed2.contains(o)) {
					if (o.getMaxDependencies(key) < Collections.frequency(pointed1, o)
							+ Collections.frequency(pointed2, o)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private Map<String, List<IDependencyObject>> getDepedencyTypesAndPointedObjects(
			Map<String, List<IDependencyObject>> result, BlockElement blockElement) {
		// we allow duplicates in result but not those that belong to the same
		// BlockElement
		List<IDependencyObject> visitedForThisBlockElement = new ArrayList<IDependencyObject>();
		for (ElementWrapper elementW1 : blockElement.getElementWrappers()) {
			IElement element = (IElement) elementW1.getElement();
			Map<String, List<IDependencyObject>> map = element.getDependencies();
			for (String key : map.keySet()) {
				List<IDependencyObject> res = result.get(key);
				if (res == null) {
					res = new ArrayList<IDependencyObject>();
				}
				List<IDependencyObject> dependencies = map.get(key);
				for (IDependencyObject o : dependencies) {
					if (!visitedForThisBlockElement.contains(o)) {
						res.add(o);
						visitedForThisBlockElement.add(o);
					}
				}
				result.put(key, res);
			}
		}
		return result;
	}

	public static List<IDependencyObject> getAllDependencies(BlockElement blockElement) {
		List<IDependencyObject> result = new ArrayList<IDependencyObject>();
		for (ElementWrapper elementW1 : blockElement.getElementWrappers()) {
			IElement element = (IElement) elementW1.getElement();
			Map<String, List<IDependencyObject>> map = element.getDependencies();
			for (String key : map.keySet()) {
				List<IDependencyObject> dependencies = map.get(key);
				for (IDependencyObject o : dependencies) {
					if (!result.contains(o)) {
						result.add(o);
					}
				}
			}
		}
		return result;
	}

	// Non functional yet
	public static List<Block> getAllDependencies(Block block, List<Block> list) {


		List<BlockElement> blockElementList = block.getOwnedBlockElements();
		List<Block> result = new ArrayList<Block>();
		boolean blockdepend;
		for(Block current : list) {
			blockdepend = false;


			for (BlockElement blockElement : blockElementList) {
				for (BlockElement currentBlock : current.getOwnedBlockElements()) {

					if((currentBlock instanceof PluginElement) &&
							(blockElement instanceof PluginElement)) {

						String blockElementStr = ((PluginElement)blockElement).getPluginSymbName();
						String currentBlockStr = ((PluginElement)currentBlock).getPluginSymbName();
						if(blockElementStr.equals(currentBlockStr)) {
							result.add(current);
							blockdepend = true;
							break;
						}
					}
				}
				if(blockdepend){ break; }
			}
			/*
				for (BlockElement blockElement : block.getOwnedBlockElements()){
					IElement element = (IElement)blockElement.getElementWrappers().get(0).getElement();
					sText = sText + element.getText() + "\n";

				}*/
		}


		/*List<IDependencyObject> result = new ArrayList<IDependencyObject>();
			for (ElementWrapper elementW1 : blockElement.getElementWrappers()) {
				IElement element = (IElement) elementW1.getElement();
				Map<String, List<IDependencyObject>> map = element.getDependencies();
				for (String key : map.keySet()) {
					List<IDependencyObject> dependencies = map.get(key);
					for (IDependencyObject o : dependencies) {
						if (!result.contains(o)) {
							result.add(o);
						}
					}
				}
			}*/
		return result;

	}

}
