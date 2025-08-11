import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class jTableRendererInert extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			
			if ( (row % 2) == 0)

			{
				cell.setBackground(new Color(248,205,216));
			}

			else

			{
				cell.setBackground(Color.WHITE);;
			}
			if ( ( row  == 2) && ( (column==1) || (column==2) || (column==3) ) )					
		    {cell.setBackground(Color.GRAY);}	
			
			if ( ( row  == 3) && ( (column==1) || (column==3) || (column==4) ) )					
		    {cell.setBackground(Color.GRAY);}	
			
			if ( ( row  == 4) && ( (column==1) || (column==3) ) )					
		    {cell.setBackground(Color.GRAY);}	

			
			
			return cell;
		}
	}	
