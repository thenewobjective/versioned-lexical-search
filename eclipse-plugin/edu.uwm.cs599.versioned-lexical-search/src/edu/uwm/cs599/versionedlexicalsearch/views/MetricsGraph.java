package edu.uwm.cs599.versionedlexicalsearch.views;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import edu.uwm.cs599.versionedlexicalsearch.util.SWTResourceManager;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class MetricsGraph extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.uwm.cs599.versionedlexicalsearch.views.ResultGraph";

	public String[][] data; 
	
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	
	private 

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return new String[] { "One", "Two", "Three" };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public MetricsGraph() {}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	//TODO: Need a scrollable canvas
	// http://stackoverflow.com/questions/7791696/how-do-i-make-a-view-scrollable-in-an-eclipse-rcp-application
	// http://www.coderanch.com/t/330746/GUI/java/scroll-image
	public void createPartControl(final Composite parent) {
		//ScrolledComposite scParent = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		final Canvas canvas = new Canvas(parent, SWT.NONE);

		/*canvas.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
        });*/
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if(data == null || data.length == 0)
					return;

				int xOffset = 130;
				int cw = (data.length+1) * xOffset;
				int ch = 450; //TODO: determine height of canvas
				Color red = new Color(e.display,255,0,0),
					  tan = new Color(e.display,128,128,0),
					  pink = new Color(e.display,255,0,255),
					  black = new Color(e.display,0,0,0),
					  green = new Color(e.display,0,128,0),
					  cyan = new Color(e.display,0,128,128),
					  blue = new Color(e.display,0,0,255),
					  white = new Color(e.display,255,255,255),
				  	  gray = new Color(e.display,240,240,240);

				canvas.setBounds(0, 0, cw, ch);

				//REVISION,PATH,TOTAL_LINES,CODE_LINES,COMMENT_LINES,BLANK_LINES,COMMENTS,CYCLO

				GC gc = e.gc;
				gc.drawText(data[0][1], 10, 10);
				
				gc.setForeground(red);
				gc.drawText("Total Lines", 10, 30);
				gc.setForeground(tan);
				gc.drawText("Lines of Code", 10, 50);
				
				gc.setForeground(green);
				gc.drawText("Comment Lines", 120, 30);
				gc.setForeground(pink);
				gc.drawText("Blank Lines", 120, 50);
				
				gc.setForeground(cyan);
				gc.drawText("Total Comments", 230, 30);
				gc.setForeground(blue);
				gc.drawText("Cyclomatic Complexity", 230, 50);
				
				//draw the plot line
				gc.setForeground(black);
				gc.drawLine(100, 70, 100, ch-30);
				gc.drawLine(100, ch-30, cw-10, ch-30);
				
				//draw the y-axis
				for(int h = ch-40; h > 70; h -= 20){
					gc.setForeground(black); gc.setBackground(gray);
					gc.drawText(Math.abs(ch-40-h)+"", 20,h);
				}
				
				for(int i = 0; i < data.length; i++) {
					String[] row = data[i];
					String[] lastRow = data[Math.max(i-1, 0)];
					String rev = row[0];
					int totalLines = Integer.parseInt(row[2]);
					int totalLinesLast = Integer.parseInt(lastRow[2]);
					int codeLines = Integer.parseInt(row[3]);
					int codeLinesLast = Integer.parseInt(lastRow[3]);
					int commentLines = Integer.parseInt(row[4]);
					int commentLinesLast = Integer.parseInt(lastRow[4]);
					int blankLines = Integer.parseInt(row[5]);
					int blankLinesLast = Integer.parseInt(lastRow[5]);
					int comments = Integer.parseInt(row[6]);
					int commentsLast = Integer.parseInt(lastRow[6]);
					int cyclo = Integer.parseInt(row[7]);
					int cycloLast = Integer.parseInt(lastRow[7]);
					
					int xPos = xOffset + i*60;
					int xPosPrev = xOffset + Math.max(i-1, 0) * 60;
					//draw tick + revision number
					gc.setForeground(black); gc.setBackground(gray);
					gc.drawLine(xPos, ch-30, xPos, ch-20);
					gc.drawText(rev, xPos, ch-15);
					
					//plot Total Lines
					gc.setBackground(red); gc.setForeground(red);
					gc.fillRectangle(xPos - 5, ch - 30 - totalLines - 5, 10, 10);
					gc.drawLine(xPosPrev,  ch - 30 - totalLinesLast, xPos,  ch - 30 - totalLines);
					
					//plot Code Lines
					gc.setBackground(tan); gc.setForeground(tan);
					gc.fillRectangle(xPos - 5, ch - 30 - codeLines - 5, 10, 10);
					gc.drawLine(xPosPrev,  ch - 30 - codeLinesLast, xPos,  ch - 30 - codeLines);
					
					//plot comment lines
					gc.setBackground(green); gc.setForeground(green);
					gc.fillRectangle(xPos - 5, ch - 30 - commentLines - 5, 10, 10);
					gc.drawLine(xPosPrev,  ch - 30 - commentLinesLast, xPos,  ch - 30 - commentLines);

					//plot blank lines
					gc.setBackground(pink); gc.setForeground(pink);
					gc.fillRectangle(xPos - 5, ch - 30 - blankLines - 5, 10, 10);
					gc.drawLine(xPosPrev,  ch - 30 - blankLinesLast, xPos,  ch - 30 - blankLines);
					
					//plot comments
					gc.setBackground(cyan); gc.setForeground(cyan);
					gc.fillRectangle(xPos - 5, ch - 30 - comments - 5, 10, 10);
					gc.drawLine(xPosPrev,  ch - 30 - commentsLast, xPos,  ch - 30 - comments);
					
					//plot cyclo
					gc.setBackground(blue); gc.setForeground(blue);
					gc.fillRectangle(xPos - 5, ch - 30 - cyclo - 5, 10, 10);
					gc.drawLine(xPosPrev,  ch - 30 - cycloLast, xPos,  ch - 30 - cyclo);
				}
			}
		});
		
		
/**************************************/
		/*
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "edu.uwm.cs599.versioned-lexical-search.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		*/
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MetricsGraph.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Metrics Graph",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//viewer.getControl().setFocus();
	}

}