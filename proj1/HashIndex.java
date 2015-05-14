import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

// TODO   Have to remove all indexed files any time before run the program. 
// TODO smaller file is inner join, bigger file is outer join. 
public class HashIndex {
	private static final String tab = "\t";
	private static int counter = 0;
	private static int hashSize = 128;
	private static final String seperator = "  ";

	/* 
	public static int hashcode(String name){
		char[] array = name.toCharArray();
		int sum = 0;
		for(char c : array){
			sum += c;
		}
		int prime = 31;
		return sum%prime; 
	} */
	public static int hashcode(String name){
		if (name.length() == 0)
			return 0;
		char c = name.charAt(0);
		return (int)c % hashSize;
	}

	@SuppressWarnings("resource")
	public static String getFirstLine(String filename){
		CSVReader reader;
		String attr_line = "";
		try {
			reader = new CSVReader(new FileReader(filename));
			String[] line = reader.readNext();
			for(int i = 0; i<line.length;i++){
				attr_line += (line[i]+tab);
			}
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException:"+e.toString());
		} catch (IOException e) {
			System.out.println("IOException:"+e.toString());
		}
		return attr_line;

	}
	public static int getIndexOfKey(String searchKey, String attr_line){
		String[] attr = attr_line.split(tab);
		return search(searchKey, attr);
	}

	// return the index of the key in the sorted array a[]; -1 if not found
	public static int search(String key, String[] a) {
		int i = 0;
		while(i < a.length){
			if(a[i].equals(key)){
				return i;
			}
			i++;
		}
		return -1;
	}

	/* key: Physician_Last_Name,  filename: "/Users/MichelleGao/Documents/workspace/CS686/lab0/test.csv"
	 * read the csv line by line and write the line(after hashing the physician_last_name) into a file(indexed_file) into disk 
	 * */
	public static void hashIndex(String key, String filename, String tablename){
		String firstarray = getFirstLine(filename);
		int position = getIndexOfKey(key,firstarray);
		if(position < 0) {
			System.out.println(">>>Failed to find the attribute "+key+" in table "+tablename);
			System.exit(1);
		}
		String attr_line = getFirstLine(filename);
		//System.out.println("the index of Physician_Last_Name is:"+position);

		CSVReader reader;
		try {
			ArrayList<BufferedWriter> outputs = new ArrayList<BufferedWriter>();
			for (int i = 0;i < hashSize; ++i) {
				String index_file_name = tablename+"_"+key+"_"+i;
				File index_file = new File(index_file_name);
				index_file.createNewFile();
				FileWriter fw = new FileWriter(index_file.getAbsolutePath(),true);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(attr_line);
				out.newLine();
				outputs.add(out);
			}

			reader = new CSVReader(new FileReader(filename), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,1);
			String[] lines;  
			while ((lines = reader.readNext()) != null) {
				String line = "";
				for(int i = 0; i<lines.length;i++){
					if(lines[i].length() == 0)
						line += (","+tab);
					if(lines[i].length() > 0)
						line += (lines[i]+tab);
				}
				int index = 0; //every line will match a physician_last_name and hash the value to index which will be used as part of output_filename
				if(position < lines.length){ 
					index = Math.abs(hashcode(lines[position]));
					//System.out.println("the hashed index of physician_last_name "+lines[position]+" is:"+index);
				}
				outputs.get(index).write(line);
				outputs.get(index).newLine();
			}

			for (int i = 0;i < hashSize; ++i) {
				//outputs.get(i).write(builders.get(i).toString());
				outputs.get(i).close();
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException in hashIndex function:"+e.toString());
		} catch (IOException e) {
			System.out.println("IOException:"+e.toString());
		}
	}
	public static void old_join(String innerTable, String outerTablem, String innerFile, String outerFile, String innerKey, String outerKey){
		System.out.println("pass ");
	}
	// iterate through each line of outerTable(read from csv), hash the value of the attribute and search in index files of inner table
	public static void join(String innerTable, String outerTable,String innerFile, String outerFile, String innerKey, String outerKey){
		//System.out.println("In join() function of HashIndex.java");
		long joinTime = System.currentTimeMillis();
		String attributeLineOuter = getFirstLine(outerFile);
		String attributeLineInner = getFirstLine(innerFile);
		int posKeyOuter= getIndexOfKey(outerKey, attributeLineOuter);
		int posKeyInner = getIndexOfKey(innerKey, attributeLineInner);
		//System.out.println("pos of "+outerKey+" in "+outerFile+" is:"+posKeyOuter);
		//System.out.println("pos of "+innerKey+" in "+innerFile+" is:"+posKeyInner);
		if(posKeyOuter < 0) {
			System.err.println(">>>Failed to find the attribute "+outerKey+" in the table "+outerTable);
			System.exit(1);
		}
		CSVReader reader;
		boolean found = false;
		String innerJoinFile = "";
		BufferedReader innerReader= null;
		try {
			reader = new CSVReader(new FileReader(outerFile), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,1); //skip first line 
	 		String[] outerLine;
			while((outerLine = reader.readNext()) != null){
				StringBuilder matchingResults= new StringBuilder();
				/*Ignore if the value of the tuple outerLine[posKeyOuter] is empty or null.*/
				if(outerLine[posKeyOuter].length() == 0) continue;
				/*Add entry of outer table */
				for(int i = 0; i<outerLine.length;i++){
					if(i == posKeyOuter) continue;
					matchingResults.append(outerLine[i]+seperator);
				}
				//System.out.println(matchingResults.toString());
				int targetHash = Math.abs(hashcode(outerLine[posKeyOuter]));
				innerJoinFile = innerTable+"_"+innerKey+"_"+targetHash;
				String l = "";
				innerReader = new BufferedReader(new FileReader(innerJoinFile));
				while((l = innerReader.readLine()) != null ){
					String[] innerLine = l.split(tab);
					/*Ignore if the value is null or empty*/
					if(innerLine[posKeyInner].length() == 0) continue;
					//if(innerLine[posKeyInner].length() >0 && outerLine[posKeyOuter].length()>0 && innerLine[posKeyInner].equalsIgnoreCase(outerLine[posKeyOuter])){
					if(innerLine[posKeyInner].equalsIgnoreCase(outerLine[posKeyOuter])){
						found = true;
						System.out.println(">>>Matching join by :"+outerLine[posKeyOuter]+"........");
						System.out.print(matchingResults.toString());
						System.out.print(l+"\n");
						counter = counter+1;
						}
					}
				}
			long endTime   = System.currentTimeMillis();
			long join = (endTime-joinTime)/1000;
			System.out.println("There are "+counter+" matching results for joining "+outerTable+" and "+innerTable+" by key "+innerKey);
			System.out.println("Finish joining "+"Table "+innerTable+" and Table "+outerTable+" by key "+innerKey+" in "+join+"seconds( include time cost of printing out results)");
			//System.out.println("Number of matching entries is "+counter);
			}catch(FileNotFoundException e){
				System.out.println("Can't open:"+innerJoinFile);
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(found == false)
				System.out.println(">>>No matching join found");
				try {
					if (innerReader != null) {
					innerReader.close();
					}
				} catch (IOException e) {
					System.out.println("IOException when closing innerReader");
				}
			}	
		}
	/* public static void main(String[] args) throws IOException, InterruptedException{
		String innerT = "Ownership";
		String outerT = "Research";
		String innerK= "Physician_Last_Name"; 
		String outerK = "Physician_Last_Name";		
		//String innerK = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name";
		//String outerK = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name";
		String outerFile = "10lineOwnership.csv";
		String innerFile = "10lineResearch.csv";	
		 
		//String outerFile= "OPPR_ALL_DTL_RSRCH_12192014.csv";
		//String innerFile = "OPPR_ALL_DTL_OWNRSHP_12192014.csv";
		//String outerFile= "OPPR_ALL_DTL_GNRL_12192014.csv";
		//String outerFile = "OPPR_SPLMTL_PH_PRFL_12192014.csv";
		 
//String key = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name"; String value = "Safco Dental Supply Co."; 
		hashIndex(innerK,innerFile,innerT);
		long startTime = System.currentTimeMillis();
		join(innerT,outerT, innerFile, outerFile,innerK,outerK);		
		System.out.println("there are "+counter+" joining matches");
		long joinTime = System.currentTimeMillis();
		long join = (joinTime-startTime)/1000; 
		System.out.println("Finish joining in "+join+"seconds");

	} */ 
}
