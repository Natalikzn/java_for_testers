import java.io.File;

public class Hello {
    public static void main(String[] args) {
        System.out.println("Моя первая программа!");

        var configFie = new File("sandbox/build.gradle");
        System.out.println(configFie.getAbsolutePath());
        System.out.println(configFie.exists());
    }
}
