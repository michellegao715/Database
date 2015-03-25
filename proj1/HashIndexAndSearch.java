import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;


public class ReadFromCSV {
	private static final String tab = "\t";
	public static int hashcode(String name){
		char[] array = name.toCharArray();
		int sum = 0;
		for(char c : array){
			sum += c;
		}
		int prime = 31;
		return sum%prime; 
	}

	// "/Users/MichelleGao/Documents/workspace/CS686/lab0/test.csv"
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
		System.out.println("print out everything ");
		return search(searchKey, attr);
	}

	// return the index of the key in the sorted array a[]; -1 if not found
	public static int search(String key, String[] a) {
		int i = 0;
		while(i < a.length){
			if(a[i].equals(key)){
				System.out.println("search for the index of Physician_Last_Name in first line:"+i);
				System.out.println(a[i]);
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
		String attr_line = getFirstLine(filename);
		//System.out.println("the index of Physician_Last_Name is:"+position);
		CSVReader reader;
		try {
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
				//write one line into file: tablename_index 
				String index_file_name = tablename+"_"+index;
				File index_file = new File(index_file_name);
				if(!index_file.exists()) {
					index_file.createNewFile();
					FileWriter fw = new FileWriter(index_file.getAbsolutePath(),true);
					BufferedWriter out = new BufferedWriter(fw);
					out.write(attr_line);
					out.newLine();
					out.close();
				} 
				FileWriter fw = new FileWriter(index_file.getAbsolutePath(),true);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(line);
				out.newLine();
				out.close();
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException in hashIndex function:"+e.toString());
		} catch (IOException e) {
			System.out.println("IOException:"+e.toString());
		}
	}
	//SELECT * FROM table_name WHERE Physician_Last_name="name"
	// search(Physician, Physician_Last_Name, Adlow)   should 
	public static void search_select(String tablename, String key, String value){
		int target_hash_index= hashcode(value);
		String file_to_search = tablename+"_"+target_hash_index;
		File f = new File(file_to_search);
		BufferedReader reader = null;
		String attr_line = "";
		String line = "";
		int index=-1; //index of Physician_Last_Name in attr_line
		try {
			reader = new BufferedReader(new FileReader(f));
			attr_line = reader.readLine();
			index = getIndexOfKey(key,attr_line);
			//int i = 0;
			while ((line = reader.readLine()) != null) {
			/*	if ( i == 0) {
					String[] temp = line.split(tab);
					for ( String s : temp) {
						System.out.print(s+"$$$");
					}
				}
				i++;*/
//				System.out.println("print out line in table:");
//				System.out.println(line);
				String[] lines = line.split(tab);
				if(lines[index].equals(value)) {
					System.out.println("Found matching physician>>>");
					System.out.println(line);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Can't open:"+file_to_search);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException{
		String tablename = "Physician";
		//  String key = "Physician_Last_Name"; String value = "Yakubov"; filename = oppr_ownership; 
		//filename = OPPR_ALL_DTL_OWNRSHP_12192014.csv; String key = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name"; String value = "Safco Dental Supply Co."; 
		String key = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name";
		String value = "Cardiosolutions, Inc.";
		String filename = "/Users/MichelleGao/Documents/workspace/CS686/lab0/oppr_ownership.csv";
		hashIndex(key,filename,tablename);
		search_select(tablename,key,value);
		
	}
}
