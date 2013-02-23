import com.google.common.base.Joiner;

public class Test {
    public static void main(String[] args) {
        System.out.println(Joiner.on(",").join(new String[]{"Hello", "world"}));
    }
}
