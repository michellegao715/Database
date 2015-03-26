import java.util.regex.Matcher;
import java.util.regex.Pattern;

//SELECT * FROM table_name WHERE Physician_Last_name="name"  
//SELECT * FROM table_name WHERE Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name="vendor_name"
public class SelectAndSearch{
	public static final String selectPattern = "(?i)(SELECT )([0-9a-zA-Z_]*|[\\*])( from )([0-9a-zA-Z_]*)( where )([0-9a-zA-Z_]*)(=)([a-zA-Z0-9][a-zA-Z0-9,. ]+);";
	//[tablename, searchkey, searchvalue]: [Physician, Physician_Last_Name, "Michelle"] 
	public static String[] get_search_keys(String cmd){
		Pattern r;
		Matcher m;
		String[] keys = new String[3];
		if(cmd.startsWith("SELECT")){
			r = Pattern.compile(selectPattern);
			m = r.matcher(cmd);
			if(m.find()) {
				String tablename = m.group(4);
				String searchkey = m.group(6);
				//System.out.println(searchkey);
				String searchvalue = m.group(8);	
				keys[0] = tablename; 
				keys[1]= searchkey;
				keys[2] = searchvalue;
			}
		}
		return keys;
	}	
	public static String get_filename(String tablename){
		String filename = "";
		switch(tablename) {
			case "Payments": filename = "OPPR_ALL_DTL_GNRL_12192014.csv";
			//case "Payments": filename = "10000_line_payments.csv";
							 break; 
			case "Physicians": filename = "OPPR_SPLMTL_PH_PRFL_12192014.csv";
			//case "Physicians": filename = "100000_line_physician.csv";
							 break; 
			case "Ownership": filename = "OPPR_ALL_DTL_OWNRSHP_12192014.csv";
							 break; 
			case "Research": filename = "OPPR_ALL_DTL_RSRCH_12192014.csv";
							 break; 
		}
		return filename;
	}
	
	public static void selectAndSearch(String cmd){
		String[] search_keys = get_search_keys(cmd);
		String tablename = search_keys[0];
		System.out.println(">>>Tablename of your searching is:"+tablename);
		String filename = get_filename(tablename);
		System.out.println(">>>Filename of your searching:"+filename);
		String key = search_keys[1];
		System.out.println(">>>Searching key is "+key);
		String value = search_keys[2];
		System.out.println(">>>Searching value is "+value);
		System.out.println("Start searching");
		HashIndex t = new HashIndex();
		long startTime = System.currentTimeMillis();
		t.hashIndex(key,filename,tablename);
		System.out.println("Finish indexing");
		System.out.println("Start searching");
		t.search_select(tablename, key, value);
		long endTime   = System.currentTimeMillis();
		long totalTime = (endTime-startTime)/1000; 
		System.out.println("Finish searching");
		System.out.println("Finish searching in "+totalTime+"seconds");
	}
	//java SelectAndSearch SELECT * FROM table_name WHERE Physician_Last_name=Michelle";
/* 	public static void main(String[] args){
		String cmd = args[0];
		String[] search_keys = get_search_keys(cmd);
		String tablename = search_keys[0];
		System.out.println("Tablename of your searching:"+tablename);
		String filename = get_filename(tablename);
		System.out.println("Filename of your searching:"+filename);
		String key = search_keys[1];
		System.out.println("Searching key is "+key);
		String value = search_keys[2];
		System.out.println("Searching value is "+value);
		System.out.println("Start searching");
		HashIndex t = new HashIndex();
		long startTime = System.currentTimeMillis();
		t.hashIndex(key,filename,tablename);
		System.out.println("Finish indexing");
		System.out.println("Start searching");
		t.search_select(tablename, key, value);
		long endTime   = System.currentTimeMillis();
		long totalTime = (endTime-startTime)/1000; 
		System.out.println("Finish searching");
		System.out.println("Finish searching in "+totalTime+"seconds");
	} */
}

