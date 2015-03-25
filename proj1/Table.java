import java.util.ArrayList;

public class Table {
	String tablename;
	ArrayList<Attribute> attributes; //the attribute should be inserted and retrieved in same order.
	public Table(String tablename, ArrayList<Attribute> attributes){
		this.tablename = tablename;
		this.attributes = attributes;
	}
}
