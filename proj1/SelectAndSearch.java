import java.util.regex.Matcher;
import java.util.regex.Pattern;

//SELECT * FROM Payments p, Physicians r WHERE p.Physician_Last_Name = r.Physician_Last_Name 
public class SelectAndSearch{
	//public static final String selectPattern = "(?i)(SELECT )([0-9a-zA-Z_]*|[\\*])( from )([0-9a-zA-Z_]*)( where )([0-9a-zA-Z_]*)(=)([a-zA-Z0-9][a-zA-Z0-9,_. ]+);";
	
	public static final String selectPattern = "(?i)(SELECT )[\\*]( from )([0-9a-zA-Z_]*) ([a-zA-Z]+), ([a-zA-Z]+) [a-zA-Z]+( where )([a-zA-Z]*.[0-9a-zA-Z_]*)( = )([a-zA-Z]*.[a-zA-Z_]*)";
	public static String[] get_search_keys(String cmd){
		Pattern r;
		Matcher m;
		String[] keys = new String[4];  // [table1, table2, key1, key2]  key1 should == key2. 
		if(cmd.startsWith("SELECT")){
			r = Pattern.compile(selectPattern);
			m = r.matcher(cmd);
			if(m.find()) {  //TODO handle wrong select such as SELECT * FROM Payments r, Physician m WHERE p.Physician_Name = r.Physician;
				String table1= m.group(3);
				String table2= m.group(5);
				String searchKey1 = m.group(8);
				String searchKey2= m.group(10);
				keys[0] = table1;
				keys[1]= table2;
				keys[2] = searchKey1;
				keys[3] = searchKey2;
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
	
	public static void searchJoin(String cmd){
		String[] keys = get_search_keys(cmd);
		String t1=search_keys[0];
		String t2 =search_keys[1];
		System.out.println(">>>You want to join two tables:"+t1+","+t2+" by "+search_keys[2]);
		File f1= new File(get_filename(t1));
		File f2 = new File(get_filename(t2));	
		String innerTable = f1.length() < f2.length() ? t1 : t2; 
		String outerTable = f1.length() < f2.length() ? t2 : t1;
		String innerKey= search_keys[2];
		String outerKey = search_keys[3];
        String innerFile = f1.length() < f2.length() ? get_filename(t1) : get_filename(t2);
        String outerFile = f1.length() < f2.length() ? get_filename(t2) : get_filename(t1);
		// Build index files for bigger files(as inner join). 
		System.out.println("Start building index files for tables:"+innerTable);
		HashIndex t = new HashIndex();
		long startTime = System.currentTimeMillis();
		String key = f1.length() < f2.length() ? innerKey : outerKey;
		t.hashIndex(key,get_filename(innerTable),innerTable);
		long indexTime = System.currentTimeMillis() - startTime;
		System.out.println("Finish indexing in:"+indexTime/1000+"seconds");
		
		System.out.println("Start joining");
		long joinTime = System.currentTimeMillis();
		// join "innerTable" and "outerTable" by attribute "key"
        t.join(innerTable, outerTable, innerFile, outerFile, innerKey, outerKey);
		long endTime   = System.currentTimeMillis();
		long joinTime = (endTime-joinTime)/1000; 
		System.out.println("Finish joining in "+joinTime+"seconds");
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

