
/**
 * @author chenhuaming
 * 2016-1-18
 * 
 */
public class TestFFF {
	public static void main(String[] args){
		f("A");
		f("A1");
		f("1A1");
		f("A1B");
		f("A1B");
		f("A1B2B");
		
	}
	public static void f(String imgName){
	
		if(!imgName.contains("A")){
			return;
		}
		String[] args = imgName.split("A");
		if(args.length<2){
			return;
		}
		if(!(args[1].contains("1B")||args[1].contains("2B"))){
			return;
		}
		System.out.println(imgName+":"+"reach");
	}
}
