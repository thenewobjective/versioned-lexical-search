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

import edu.uwm.cs599.versionedlexicalsearch.util.*;

public class RebuildPromptWindow extends Dialog {

    protected Shell shlRepositoryPrompt;
	
    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
	public RebuildPromptWindow(Shell parent, int style) {
		super(parent, style);
        shlRepositoryPrompt = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shlRepositoryPrompt.setSize(483, 175);
    	shlRepositoryPrompt.setText("Rebuild Search Database");
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
        
    	btnSearch.setBounds(310, 115, 75, 25);
    	btnSearch.setText("Rebuild");
    	btnSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                	//http://lexical-search/?cmd=rebuild&repo=jpcsp&start=1&end=20
                	String firstRev = firstRevText.getText(),
                     	   lastRev = lastRevText.getText(),
                     	   repo = txtRepo.getText(),
             			   params = "cmd=rebuild&start="+firstRev+"&end="+lastRev+"&repo="+repo,
                    	   response = Http.httpPost(urlTextBox.getText(),params);
                    shlRepositoryPrompt.close();
                } catch (NumberFormatException e1) {
                    DialogUtils.displayErrorMsgWindow("The repository range is invalid", shlRepositoryPrompt);
                }
            }
        });
        
		 Button btnCancel = new Button(shlRepositoryPrompt, SWT.NONE);
		 btnCancel.setBounds(392, 115, 75, 25);
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
