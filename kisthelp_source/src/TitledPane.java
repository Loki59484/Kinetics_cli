import javax.swing.JComponent;


public class TitledPane {

	String  title;
	JComponent jComp;
	
	/* C O N S T R U C T O R 1*/
	public TitledPane(String title, JComponent aJComp) {
		
		this.title = title;
		this.jComp = aJComp;
	}// end of constructor1
	
	/* C O N S T R U C T O R 2*/
	public TitledPane() {
		
		this.title = "Tab";
		this.jComp = null;
	}// end of constructor2

	
	public String getTitle() {return title;}
    public JComponent getJComp(){return jComp;}	
	
}
