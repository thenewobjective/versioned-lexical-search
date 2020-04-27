package edu.uwm.cs599.versionedlexicalsearch.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.uwm.cs599.versionedlexicalsearch.util.*;

public class SearchPromptWindow extends Dialog {

    protected Shell shlRepositoryPrompt;
	
    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
	public SearchPromptWindow(Shell parent, int style) {
		super(parent, style);
        shlRepositoryPrompt = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shlRepositoryPrompt.setSize(483, 217);
    	shlRepositoryPrompt.setText("Search Repository History");
    	DialogUtils.centerScreen(shlRepositoryPrompt);
	}
	
    /**
     * Open the dialog.
     * @return the result
     */
	//Parameterize with prefilled contents
	public /*String[]*/ void open(/*String repoURL*/) {
		createContents();
	}
	
    /**
     * Create contents of the dialog.
     */
    private void createContents() {
    	final Font font12 = SWTResourceManager.getFont("Verdana", 12, SWT.NORMAL);
		final Font font10 = SWTResourceManager.getFont("Verdana", 10, SWT.NORMAL);

    	final Button btnSearch = new Button(shlRepositoryPrompt, SWT.NONE);
       
        Label lblUrl = new Label(shlRepositoryPrompt, SWT.NONE);
        lblUrl.setFont(font12);
        lblUrl.setBounds(10, 10, 457, 25);
        lblUrl.setText("VLS Server URL:");
        
        Label lblRepoName = new Label(shlRepositoryPrompt, SWT.NONE);
        lblRepoName.setFont(font12);
        lblRepoName.setBounds(10, 75, 125, 25);
        lblRepoName.setText("Repo. name:");

        final Text txtRepo = new Text(shlRepositoryPrompt, SWT.BORDER);
        txtRepo.setFont(font12);
        txtRepo.setText("MyRepo");
        txtRepo.setBounds(140, 75, 100, 27);
        
		Label lblRevision = new Label(shlRepositoryPrompt, SWT.NONE);
		lblRevision.setFont(font12);
		lblRevision.setBounds(255, 74, 80, 25);
		lblRevision.setText("Range:");

        final Text urlTextBox = new Text(shlRepositoryPrompt, SWT.BORDER);
        urlTextBox.setFont(font12);
        urlTextBox.setText("http://example.com/lexical-search");
        urlTextBox.setBounds(10, 41, 457, 27);
        /*urlTextBox.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.CR | e.keyCode == SWT.KEYPAD_CR){
                     btnOk.notifyListeners(SWT.Selection, new Event());
                 }
             }
         });*/
		
        final Text firstRevText = new Text(shlRepositoryPrompt, SWT.BORDER | SWT.CENTER);
        firstRevText.setFont(font10);
        firstRevText.setBounds(340, 75, 50, 21);
        firstRevText.setText("1");

        final Text lastRevText = new Text(shlRepositoryPrompt, SWT.BORDER | SWT.CENTER);
        lastRevText.setFont(font10);
        lastRevText.setBounds(395, 75, 50, 21);
        lastRevText.setText("20");
		
        Label lblQuery = new Label(shlRepositoryPrompt, SWT.NONE);
        lblQuery.setFont(font12);
        lblQuery.setBounds(10, 110, 117, 25);
        lblQuery.setText("Query:");
        
        final Text txtQuery = new Text(shlRepositoryPrompt, SWT.BORDER);
        txtQuery.setFont(font10);
        txtQuery.setBounds(120, 110, 300, 25);
        txtQuery.setText("");

    	btnSearch.setBounds(310, 154, 75, 25);
    	btnSearch.setText("Search");
    	btnSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                	//http://lexical-search/?cmd=query&repo=jpcsp&start=1&end=20&query='static'
                    String firstRev = firstRevText.getText(),
                    	   lastRev = lastRevText.getText(),
                    	   repo = txtRepo.getText(),
            			   search = txtQuery.getText(),
            			   baseUrl = urlTextBox.getText(),
            			   url = baseUrl + "?cmd=query&start="+firstRev+"&end="+lastRev+"&repo="+repo+"&query="+search,
                       	   response = Http.httpGet(url);
                   
                    shlRepositoryPrompt.close();
                    
                    QueryResults resultsView = (QueryResults)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        	.showView("edu.uwm.cs599.versionedlexicalsearch.views.QueryResults");
                    
                    //<LINE>,<REVISION>,<PATH>,<MATCH>
                    String[] rows = response.split("\r\n");
                    String[][] parsedData = new String[rows.length][4];
                    
    				for(int i = 0; i < rows.length; i++){
    					String[] rowData = rows[i].split(",");
    					for(int j = 0; j < 4; j++){
    						parsedData[i][j] = rowData[j];
    					}
    				}
                    
    				resultsView.data = parsedData;
    				resultsView.repo = repo;
    				resultsView.baseUrl = baseUrl;
    				resultsView.createTable();
                } catch (NumberFormatException e1) {
                	//TODO: message box
                	e1.printStackTrace();
                } catch (PartInitException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        
		 Button btnCancel = new Button(shlRepositoryPrompt, SWT.NONE);
		 btnCancel.setBounds(392, 154, 75, 25);
		 btnCancel.setText("Cancel");
		 btnCancel.addSelectionListener(new SelectionAdapter() {
	         @Override
	         public void widgetSelected(SelectionEvent e) {
                 shlRepositoryPrompt.close();
	         }
		 });

         shlRepositoryPrompt.open();
         shlRepositoryPrompt.layout();

         Display display = getParent().getDisplay();
         while (!shlRepositoryPrompt.isDisposed()) {
             if (!display.readAndDispatch()) {
                 display.sleep();
             }
         }

    }
}
