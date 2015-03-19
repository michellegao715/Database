import java.util.Arrays;

public class sort{
	public 
	public static void sortArray(){
		String[] a = new String[5];
		a[0] = "b";
		a[1] ="a";
		a[2]="d";
		a[3]="e";
		a[4]="c";
		Arrays.sort(a);
		for(String s:a){
			System.out.println(s);
		}
	}
	public static void main(String[] args){
		sortArray();
	}
}

