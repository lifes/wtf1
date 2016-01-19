import java.util.Arrays;

/**
 * Created by chenhuaming on 16/1/17.
 */
public class TestFile {
    public static void main(String[] args) {
        int[] array = new int[99];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        if (array.length > 0) {
            int total = array.length;
            int base = 10; //线程数
            int size = (total - total % base) / base;
            if (size > 0) {
                for (int i = 0; i < base; i++) {
                    int[] temp = Arrays.copyOfRange(array, size * i, size * (i + 1));
                    System.out.println(i + " ::: " + temp.length + ":::" + (size * (i + 1)));
                    System.out.println(Arrays.toString(temp));
                }
            }
            if (total % base != 0) {
                int[] temp = Arrays.copyOfRange(array, total - total % base, total);
                System.out.println(Arrays.toString(temp));
            }

        }
    }
}
