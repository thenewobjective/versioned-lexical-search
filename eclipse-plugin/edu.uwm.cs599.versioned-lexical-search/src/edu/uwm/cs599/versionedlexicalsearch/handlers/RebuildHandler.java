package edu.uwm.cs599.versionedlexicalsearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.uwm.cs599.versionedlexicalsearch.views.RebuildPromptWindow;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RebuildHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public RebuildHandler() {}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		RebuildPromptWindow win = new RebuildPromptWindow(shell,0);
		win.open();

		return null;
	}
}
