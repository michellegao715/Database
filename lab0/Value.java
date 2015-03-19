
public enum Value {
	INT("INT"), TEXT("TEXT");
     String str;
	Value(String str){
		this.str = str;
	}
	public String showValue(){
		return str;
	}
}
