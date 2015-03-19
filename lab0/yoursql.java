import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class yoursql {
	String tableName = "";
	public static final String createPattern = "(?i)(CREATE TABLE )([0-9a-zA-Z_]*)(.*)";
	public static final String insertPattern = "(?i)(INSERT INTO )([0-9a-zA-Z_]*)( VALUES )(.*)";
	public static final String selectPattern = "(?i)(SELECT )([0-9a-zA-Z_]*|[\\*])( from )([0-9a-zA-Z_]*);";

	private static ArrayList<String> getInsertData(String cmd) {
		ArrayList<String> res = new ArrayList<String>();
		if(!checkFormat(cmd)) return res;
		Pattern r = Pattern.compile(insertPattern);
		Matcher m = r.matcher(cmd);
		if(!m.find()){ 
			return res;
		}		
		String result = m.group(4);
		result = result.substring(1, result.length()-2); //delete the last ; mark.
		for(String s : result.split(",")){
			res.add(s.trim());
		}
		return res;
	}
	//regular expression to parse anything following "CREATE TABLE ". check if table existed, then exit. 
	public static void createTable(String cmd) throws IOException{
		String tableName = getTableName(cmd);
		String tablefilename = tableName+".txt";
		File f = new File(yoursql.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		if(findFile(tablefilename,f) != null) {
			System.err.println("Can't create the table:"+tableName+" ,since it is already existed.");
			System.exit(0);
		}
		ArrayList<Attribute> attributes = getAttributes(cmd);
		int size = attributes.size();
		ArrayList<String> attributeStr = new ArrayList<String>();
		//TODO decide if write type of attribute to file
		ArrayList<String> attributeType = new ArrayList<String>();
		for(int i = 0; i<size;i++){
			attributeStr.add(attributes.get(i).name);
			attributeType.add(attributes.get(i).type.showValue());
		}
		writeToFile(tableName,attributeType,attributeStr);
	}
	//create the file if not existed, or continue write to the file .
	public static void writeToFile(String file,ArrayList<String> attrTypes, ArrayList<String> contents) throws IOException {
		boolean flag = false; // the flag is true if file existed so append contents to the file.
		String filename = file+".txt";
		File f = new File(yoursql.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		File fileToWrite = null; 
		if(findFile(filename,f) == null){
			fileToWrite = new File(f+"/"+filename); //CREATE TABLE 
		}else {
			flag = true; //file existed, ready for INSERT INTO
			fileToWrite = new File(findFile(filename,f));
		}
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileToWrite,true)));
			int i = 0;
			if(flag) writer.println();
			else if((!flag) && (attrTypes !=null)) {
				while( i<attrTypes.size()-1){
					String type = attrTypes.get(i++);
					writer.print(type+"|");
				}
				writer.print(attrTypes.get(i));
				writer.println();
			}
			i = 0;
			while(i<contents.size()-1){ 
				String attrname = contents.get(i++);
				writer.print(attrname+"|");
			}
			writer.print(contents.get(i));
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not found exception in writeToFile");
		} catch (UnsupportedEncodingException e) {
			System.err.println("UnsupportedEncodingException when writeToFile");
		}
	}

	public static String getTableName(String cmd){
		if((!cmd.startsWith("CREATE TABLE ")) &&  (!cmd.startsWith("INSERT INTO ")) && (!cmd.startsWith("SELECT "))|| (!cmd.endsWith(";"))){
			System.err.println("wrong input of yousql");
		}
		Pattern r; 
		Matcher m;
		String tableName = null;
		if(cmd.startsWith("CREATE TABLE ")){
			r = Pattern.compile(createPattern);
			m = r.matcher(cmd);
			if(m.find()) tableName = m.group(2);
		}
		if(cmd.startsWith("INSERT INTO ")){
			r = Pattern.compile(insertPattern);
			m = r.matcher(cmd);
			if(m.find()) tableName = m.group(2);
		}
		if(cmd.startsWith("SELECT")){
			r = Pattern.compile(selectPattern);
			m = r.matcher(cmd);
			if(m.find()) {
				tableName = m.group(4);	
			}
		}
		return tableName;
	}
	//return attributes < name, type>   
	public static ArrayList<Attribute> getAttributes(String cmd){
		ArrayList<Attribute> res = new ArrayList<Attribute>();
		if(!checkFormat(cmd)) return res;
		Pattern r;
		Matcher m;
		String contents = null;
		if(cmd.startsWith("CREATE TABLE ")){ 
			r = Pattern.compile(createPattern);
			m = r.matcher(cmd);
			if(!m.find()) {
				return res;
			}
			contents = m.group(3).trim();
		}else if (cmd.startsWith("INSERT INTO ")){
			r = Pattern.compile(insertPattern);
			m = r.matcher(cmd);
			if(!m.find()) {
				return res;
			}
			contents = m.group(4).trim();
		}
		//remove ( ); from contents to get real "content" 
		String[] attributes = contents.substring(1, contents.length()-2).split(",");
		for(String c : attributes){
			String attribute;
			String typestr;
			Value type;
			Attribute attr = null;
			if(cmd.startsWith("CREATE TABLE")){
				attribute = c.trim().split(" ")[0];
				typestr = c.trim().split(" ")[1];
				type= Value.valueOf(typestr);
				attr = new Attribute(attribute,type);
			}else if (cmd.startsWith("INSERT INTO")){
				attribute = c.trim();
				//if attribute is "123" then the type is integer/
				try{
					int x =Integer.parseInt(attribute);
					typestr = "INT";
				}catch(NumberFormatException e){
					typestr = "TEXT";
				}
				type = Value.valueOf(typestr);
				attr = new Attribute(attribute,type);
			}
			res.add(attr);
		} 
		return res;
	} 
	public static String getAttribute(String cmd){
		Pattern r = Pattern.compile(selectPattern);
		Matcher m = r.matcher(cmd);
		if(!m.find()){
			return null; 
		}else {
			return m.group(2).trim();
		}
	}
	/* Check the right format of parenthesis and semicolon  in cmd */
	public static boolean checkFormat(String cmd){
		if (!cmd.endsWith(";") || !cmd.contains("(") || !cmd.contains(")") || cmd.indexOf("(") > cmd.indexOf(")")) return false;
		return true;
	}
	//insert row into table, if table not existed, return error; else insert contents into existed table; Do type checking when insert new rows
	public static void insertIntoTable(String cmd){
		File f = new File(yoursql.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String tableName = getTableName(cmd);
		File tableFile = new File(tableName+".txt");
		ArrayList<String> res = getInsertData(cmd);
		ArrayList<Attribute> types = getAttributes(cmd);
		String file = findFile(tableFile.toString(),f);
		if(file == null) {
			System.err.println("Table "+file.toString()+" not existed.");
		}
		// TODO don't do typecheck at this time 
		//if(typeCheck(new File(file),types)) {
			try {
				ArrayList<String> attrType = null;
				writeToFile(tableName,attrType,res); //pass a null list of types if write content into files.
			} catch (IOException e) {
				System.err.println("exception when insertIntoTable");
			}
	//TODO don't do typecheck at this time 	
	 /*	}else {
			System.err.println("wrong type to insert into table");
			System.exit(0);
		} */ 
	}
	
	//check if types of contents are same to attributes' types in tableFile(store in first line of tableFile) 
	private static boolean typeCheck(File tableFile, ArrayList<Attribute> types) {
		BufferedReader br;
		String firstline;
		try {
			br = new BufferedReader(new FileReader(tableFile));
			firstline = br.readLine();
			String[] validTypes = firstline.split("\\|");
			for(int i = 0; i<validTypes.length;i++){
				if(!(types.get(i).type.showValue().equals(validTypes[i]))){
					br.close();
					return false;
				}
			}
			br.close();
		} catch (Exception e) {
			System.err.println("exception in typeCheck()");
		}
		return true;
	}
	//given the current directory of application, find the file which save the table.
	public static String findFile(String tablename,File curPath)
	{
		File[] list = curPath.listFiles();
		if(list!=null)
			for (File f : list){
				if (f.isDirectory()){
					findFile(tablename,f);
				}else if (tablename.equalsIgnoreCase(f.getName())){
					return f.getPath();
				}
			}
		return null;
	}
	//print out all contents in table if table existed, or return error. 
	public static void selectFromTable(String cmd){
		File f = new File(yoursql.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String tableName = getTableName(cmd);
		String attribute = getAttribute(cmd).trim();
		File file = new File(tableName+".txt");
		if(findFile(file.toString(),f) == null) {
			System.err.println("Table not existed.");
			System.exit(0);
		}
		String filename = findFile(file.toString(),f);
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(filename));
			String firstLine = r.readLine();//this line saves type of attributes 
			String secondLine = r.readLine();
			String line;
			int index=0;  //the index of selecting attribute in firstline 
			if(!attribute.equals("*")){
				String[] attrs = secondLine.split("\\|");  
				for(int i = 0;i<attrs.length;i++){
					if(attrs[i].equals(attribute)){
						index = i;
					}
				}
				System.out.println(attrs[index]);
			}else {
				System.out.println(secondLine);
			}
			while((line = r.readLine()) !=null){
				if(attribute.equals("*")) System.out.println(line);
				else System.out.println(line.split("\\|")[index]);
			}
		}catch(Exception e){
			System.err.println("exception in selectFromTable");
		}

	}
	public static void main(String[] args){
		try {
			InputStreamReader in = new InputStreamReader(System.in);
			BufferedReader input = new BufferedReader(in);
			String cmd = "";
			int count = args.length;
			String tableName = args[1];
			if(count == 2){
				while(true){
					System.out.print(">>>");
					if((cmd = input.readLine())!=null){
						cmd = cmd.trim(); //remove leading and trailing space.
						if(cmd.startsWith("CREATE TABLE")){
							createTable(cmd);
						}
						else if (cmd.startsWith("INSERT INTO")){
							insertIntoTable(cmd);
						}else if (cmd.startsWith("SELECT")){
							selectFromTable(cmd);
						}else if (cmd.equals("exit()")){
							System.exit(0);
						}else {
							System.out.println("Error: near "+cmd+" :syntax error");
							System.exit(0);
						}
					}
				}
			}else if(count == 3) { //file input
				String init = args[0]; 
				String file = args[1];
				File f = new File(yoursql.class.getProtectionDomain().getCodeSource().getLocation().getPath());
				if(!init.equals("-init") || !file.equals("part_physician_script.sql")) {
					System.err.println("wrong format of file input");	
					System.exit(0);
				}
				String table = args[2];
				BufferedReader fileinput = null;
				try{
					String currentLine;
					file = f+"/"+file;
					fileinput = new BufferedReader(new FileReader(file));
					while((currentLine = fileinput.readLine())!=null){
						currentLine= currentLine.trim(); //remove leading and trailing space.
						if(currentLine.startsWith("CREATE TABLE")) {
							createTable(currentLine);
						}	
						else if (currentLine.startsWith("INSERT INTO")) insertIntoTable(currentLine);
						else if (currentLine.startsWith("SELECT"))
							selectFromTable(currentLine);
						else {System.out.println("Error: near "+currentLine+" :syntax error");
						System.exit(0);}
					}
				}catch(Exception e){
					System.err.println("Exception in read file input");
				}
			}
		}catch(IOException id){
			System.err.println("exception in reading sql");
		}
	}
}

