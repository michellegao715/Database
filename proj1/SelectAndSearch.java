import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.util.HashMap; 
import java.io.*;

//SELECT * FROM Payments p, Physicians r WHERE p.Physician_Last_Name = r.Physician_Last_Name 
public class SelectAndSearch{
	//public static final String selectPattern = "(?i)(SELECT )([0-9a-zA-Z_]*|[\\*])( from )([0-9a-zA-Z_]*)( where )([0-9a-zA-Z_]*)(=)([a-zA-Z0-9][a-zA-Z0-9,_. ]+);";
	//public static final String selectPattern = "(?i)(SELECT )[\\*]( from )([0-9a-zA-Z_]*) ([a-zA-Z]+), ([a-zA-Z]+) [a-zA-Z]+( where )[a-zA-Z]*.([0-9a-zA-Z_]*)( = )[a-zA-Z]*.([a-zA-Z_]*)";
	public static final String selectPattern = "(?i)SELECT [\\*] from ([0-9a-zA-Z_]*) ([a-zA-Z]+), ([a-zA-Z]+) ([a-zA-Z]+) where ([a-zA-Z]+).([0-9a-zA-Z_]*) = ([a-zA-Z]+).([a-zA-Z_]*)";
	public static String[] old_get_search_keys(String cmd){
		Pattern r;
		Matcher m;
		String[] keys = new String[4];  // [table1, table2, key1, key2]  key1 should == key2. 
		if(cmd.startsWith("SELECT")){
			r = Pattern.compile(selectPattern);
			m = r.matcher(cmd);
			if(m.find()) {  //TODO handle wrong select such as SELECT * FROM Payments r, Physician m WHERE p.Physician_Name = r.Physician;
				String table1= m.group(3);
				String table2= m.group(5);
				String searchKey1 = m.group(7);
				String searchKey2= m.group(9);
				keys[0] = table1;
				keys[1]= table2;
				keys[2] = searchKey1;
				keys[3] = searchKey2;
			}
		}
		return keys;
	}	
/*Convert: SELECT * FROM Physicians p, Ownership o WHERE p.Physician_Profile_Last_Name = o.Physician_Last_Name
	 * to: {Physicians:Physician_Profile_Last_Name, Payments:Physician_Last_Name} */
	public static HashMap<String, String> get_search_keys(String cmd){
		Pattern r;
		Matcher m;
		HashMap<String, String> map = new HashMap<>();
		//String[] keys = new String[4];  // [table1, table2, key1, key2]  key1 should == key2. 
		if(cmd.startsWith("SELECT")){
			r = Pattern.compile(selectPattern);
			m = r.matcher(cmd);
			if(m.find()) {  				
				String table1 = m.group(1);
				String tablename1 = m.group(2);
				String table2= m.group(3);
				String tablename2 = m.group(4);
				String temptablename1 = m.group(5);
				String temptablename2 = m.group(7);
				String key1 = m.group(6);
				String key2= m.group(8);
				// Handle wrong select such as SELECT * FROM Payments r, Physician m WHERE p.Physician_Name = r.Physician;
				if((!temptablename1.equals(tablename1)&& !temptablename1.equals(tablename2))||(!temptablename2.equals(tablename1) && !temptablename2.equals(tablename2))){
				   System.out.println("Invalid input of select queries");
				   System.exit(0);
				} 
				if(temptablename1.equals(tablename1)){
					map.put(table1,key1);
				}else if(temptablename2.equals(tablename1)){
					map.put(table1,key2);
				}
				if(temptablename1.equals(tablename2)){
					map.put(table2,key1);
				}else if (temptablename2.equals(tablename2)){
					map.put(table2,key2);
				}
			}
		}
		/* {Physicians:Physician_Profile_Last_Name, Payments:Physician_Last_Name} */
		return map;
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
	
	public static void searchJoin(String cmd) throws InterruptedException{
		//String[] search_keys = get_search_keys(cmd);
		//{Physicians:Physician_Profile_Last_Name, Payments:Physician_Last_Name} 
		HashMap<String, String> map = get_search_keys(cmd);
		//[table1, table2]: tables  
		String[] tables = new String[2];
		int i = 0;
		for(String s:map.keySet()){
			tables[i++] = s;
		}
		System.out.println(">>>You want to join two tables:"+tables[0]+","+tables[1]+" by "+map.get(tables[0]));
		File f1= new File(get_filename(tables[0]));
		File f2 = new File(get_filename(tables[1]));	
		System.out.println("f1 is "+get_filename(tables[0])+", length is "+f1.length());
		System.out.println("f2 is "+get_filename(tables[1])+", length is "+f2.length());
		String innerTable = f1.length() < f2.length() ? tables[0] : tables[1]; 
		System.out.println("smaller table is the inner table:"+innerTable);
		//Thread.sleep(8000);
		String outerTable = f1.length() < f2.length() ? tables[1] : tables[0];
		// innerTable(smaller table) --> innerFile --> innerKey   
		// outerTable --> outerFile --> outerKey 
		String innerKey= map.get(innerTable);
		String outerKey = map.get(outerTable);
        String innerFile = f1.length() < f2.length() ? get_filename(tables[0]) : get_filename(tables[1]);
        String outerFile = f1.length() < f2.length() ? get_filename(tables[1]) : get_filename(tables[0]);
		// Build index files for bigger files(as inner join). 
		System.out.println("Start building index files for table(smaller table is inner join table):"+innerTable);
		HashIndex t = new HashIndex();
		/*Check if there are related indexed files exists in workspace, if not call hashIndex to generate indexed files*/
		String f = innerTable+"_"+innerKey+"_0";
		File indexedFile = new File(f);
		if(!indexedFile.exists()){
			long startTime = System.currentTimeMillis();
			System.out.println("Build index files by key: "+innerKey+" from csv files:"+innerFile+", which is table: "+innerTable);
			t.hashIndex(innerKey,innerFile,innerTable);
			long indexTime = System.currentTimeMillis() - startTime;
			System.out.println("Finish indexing in:"+indexTime/1000.0+"seconds");
		}
		
		System.out.println("Start joining........");
        t.join(innerTable, outerTable, innerFile, outerFile, innerKey, outerKey);
		//System.out.println("(Time of indexing is:"+indexTime/1000.0+"seconds)");
	}
	
	//java SelectAndSearch SELECT * FROM table_name WHERE Physician_Last_name=Michelle";
	public static void main(String[] args) throws InterruptedException{
		File infile = new File(args[0]);
		BufferedReader input = null;
		try{
		    input = new BufferedReader(new FileReader(infile));
		}catch(FileNotFoundException e){
			System.err.println("input file not found");
			System.exit(0);
		}
		String cmd = "";
		try{
			while(true){
				System.out.println(">>>");
					if((cmd = input.readLine()) != null){
					// cmd = "SELECT * FROM Research p, Ownership o WHERE p.Physician_Last_Name = o.Physician_Last_Name";
					cmd = cmd.trim();
					/*Check if query (ignore case) starts with SELECT * FROM*/
					if(cmd.toLowerCase().startsWith("select * from")){
						SelectAndSearch s = new SelectAndSearch();
						HashMap<String, String> map = s.get_search_keys(cmd);
						s.searchJoin(cmd);
						System.out.println(".......Finish one query.........");
						Thread.sleep(8000);
					}else{
						System.err.println("______________________________");
						System.out.println("Exit yourSQL");
						System.exit(0);
					}
				}else{
						System.err.println("______________________________");
						System.out.println("Finish all queries, exit yourSQL");
						System.exit(0);
				}
			}
		}catch(IOException e){
			System.err.println("Failed to read script file");
		}
	}
}


	
		/* String cmd = "SELECT * FROM Research p, Ownership o WHERE p.Physician_Last_Name = o.Physician_Last_Name";
		HashMap<String, String> map = get_search_keys(cmd);
		for(String s: map.keySet()){
			System.out.println(s+":"+map.get(s));
		}
		searchJoin(cmd); */ 
		/* String[] search_keys = get_search_keys(cmd);
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
		System.out.println("Finish searching in "+totalTime+"seconds"); */

