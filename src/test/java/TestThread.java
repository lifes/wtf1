/**
 * Created by chenhuaming on 16/1/16.
 */
public class TestThread {
    public  static void main(String[] args){
       Thread t =  new Thread(new Runnable() {

            public void run() {
                System.out.println("111111");
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("2222222");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

