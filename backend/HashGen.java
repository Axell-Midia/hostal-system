import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println("centro123: " + enc.encode("centro123"));
        System.out.println("norte123: " + enc.encode("norte123"));
    }
}
