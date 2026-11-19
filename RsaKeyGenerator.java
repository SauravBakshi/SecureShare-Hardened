import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Set;

public class RsaKeyGenerator {
    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? Path.of("./keys") : Path.of(args[0]);
        Files.createDirectories(output);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3072);
        KeyPair pair = generator.generateKeyPair();
        Path publicKey = output.resolve("secure-share-public.der.b64");
        Path privateKey = output.resolve("secure-share-private.pk8.b64");
        Files.writeString(publicKey, Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        Files.writeString(privateKey, Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        try { Files.setPosixFilePermissions(privateKey, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)); }
        catch (UnsupportedOperationException ignored) { }
        System.out.println("Generated 3072-bit RSA keys in " + output.toAbsolutePath());
        System.out.println("Keep the private key restricted and provide both values through your secret manager.");
    }
}
