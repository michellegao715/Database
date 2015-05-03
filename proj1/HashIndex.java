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


public class HashIndex {
	private static final String tab = "\t";

	private static int hashSize = 128;

	private static int bufferSize = 100;

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
		/* for(String s : attr){
			System.out.print(s+"$$$");
		}*/ 
		return search(searchKey, attr);
	}

	// return the index of the key in the sorted array a[]; -1 if not found
	public static int search(String key, String[] a) {
		int i = 0;
		while(i < a.length){
			if(a[i].equals(key)){
				//System.out.println("the index of "+key+" in attr_line is:"+i);
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
			ArrayList<StringBuilder> builders = new ArrayList<StringBuilder>();
			int[] counters = new int[hashSize];
			for (int i = 0;i < hashSize; ++i) {
				String index_file_name = tablename+"_"+i;
				File index_file = new File(index_file_name);
				index_file.createNewFile();
				FileWriter fw = new FileWriter(index_file.getAbsolutePath(),true);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(attr_line);
				out.newLine();
				outputs.add(out);
				builders.add(new StringBuilder());
				counters[i] = 0;
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

				//write one line into file: tablename_index 
				// String index_file_name = tablename+"_"+index;
				// File index_file = new File(index_file_name);
				// if(!index_file.exists()) {
				// 	index_file.createNewFile();
				// 	FileWriter fw = new FileWriter(index_file.getAbsolutePath(),true);
				// 	BufferedWriter out = new BufferedWriter(fw);
				// 	out.write(attr_line);
				// 	out.newLine();
				// 	out.close();
				// } 
				// FileWriter fw = new FileWriter(index_file.getAbsolutePath(),true);
				// BufferedWriter out = new BufferedWriter(fw);
				// out.write(line);
				// out.newLine();
				// out.close();

				//builders.get(index).append(line);
				//builders.get(index).append("\n");
				//if (counters[index]++ == bufferSize) {
				//	outputs.get(index).write(builders.get(index).toString());
				//	builders.get(index).setLength(0);
				//	counters[index] = 0;
				//}
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
	//SELECT * FROM table_name WHERE Physician_Last_name="name"
	// search(Physician, Physician_Last_Name, Adlow)   should 
	public static void search_select(String tablename, String key, String value){
		boolean found = false;//set this value to true when find any matching result
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
				String[] lines = line.split(tab);
				if(lines[index].equals(value)) {
					found = true;
					System.out.println(">>>Found matching physician:");
					System.out.println(line);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Can't open:"+file_to_search);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(found == false) {
				System.out.println(">>>No matching datas");
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}
}

	public static void main(String[] args) throws IOException, InterruptedException{
		String tablename = "Physician";
		String filename = "oppr_ownership.csv";
		String key = "Physician_Last_Name"; 
		String value = "Yakubov"; 
		//filename = OPPR_ALL_DTL_OWNRSHP_12192014.csv; String key = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name"; String value = "Safco Dental Supply Co."; 
		//filename = oppr_research.csv;  String key = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name"; string value= "ELI LILLY AND COMPANY" 
		//String key = "Applicable_Manufacturer_or_Applicable_GPO_Making_Payment_Name"; String value = "ELI LILLY AND COMPANY"; String filename = "/Users/MichelleGao/Documents/workspace/CS686/lab0/oppr_research.csv";
		hashIndex(key,filename,tablename);
		search_select(tablename,key,value);
		
	}
}
