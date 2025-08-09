package kisthep.util;


import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class kisthelpJTable extends JTable {
	
	
	
	
	public static void alignRight(JTable table, int column) {
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    table.getColumnModel().getColumn(column).setCellRenderer(rightRenderer);
	}
	
	public static void alignLeft(JTable table, int column) {
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.LEFT);
	    table.getColumnModel().getColumn(column).setCellRenderer(rightRenderer);
	}

	
	public static void alignCenter(JTable table, int column) {
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.CENTER);
	    table.getColumnModel().getColumn(column).setCellRenderer(rightRenderer);
	}


}
