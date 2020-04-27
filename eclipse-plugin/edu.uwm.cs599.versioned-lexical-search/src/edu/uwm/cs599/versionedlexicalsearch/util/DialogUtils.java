package edu.uwm.cs599.versionedlexicalsearch.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DialogUtils {

        public static void centerScreen(Shell shell) {
                Rectangle bds = shell.getDisplay().getPrimaryMonitor().getBounds();
                Point p = shell.getSize();

                int nLeft = (bds.width - p.x) / 2;
                int nTop = (bds.height - p.y) / 2;

                shell.setBounds(nLeft, nTop, p.x, p.y);
        }

        public static void displayErrorMsgWindow(String msg, Shell shl) {
                MessageBox msgBox = new MessageBox(shl);
                msgBox.setText("Error Window");
                msgBox.setMessage(msg);
                msgBox.open();
        }
}
