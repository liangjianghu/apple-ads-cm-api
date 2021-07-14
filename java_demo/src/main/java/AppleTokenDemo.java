import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * apple ads demo
 */
public class TokenUtils {

    private static String sub = "SEARCHADS.27478e71-3bb0-4588-998c-182e2b405577";
    private static String iss = "SEARCHADS.27478e71-3bb0-4588-998c-182e2b405577";
    private static String key_id = "bacaebda-e219-41ee-a907-e2c25b24d1b2";
    private static String aud = "https://appleid.apple.com";
    private static String alg = "ES256";

    public static void main(String[] args) {
        String token = createAccessJwtToken("/Users/admin/private-key.pem");
        System.err.println(token);
    }
    /**
     * 生成token字符串的方法
     *
     * @return
     */
    public static String createAccessJwtToken(String privateKeyPath) {
        PrivateKey privateKey = getECPrivateKey(privateKeyPath);
        String accessToken = Jwts.builder().setHeader(new HashMap() {{
                    put("alg",alg);
                    put("kid",key_id);
                }})
                .setIssuer(iss)
                .setIssuedAt(new Date())
                .setSubject(sub)
                .setAudience(aud)
                .setExpiration(new Date(System.currentTimeMillis() + 86400*180*1000L))
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();

        return accessToken;
    }


    /**
     * 获取PrivateKey对象
     *
     * @return
     */
    private static PrivateKey getECPrivateKey(String privateKeyPath) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
            String outPath = privateKeyPath.substring(0, privateKeyPath.lastIndexOf(File.separator) + 1) + "ec_private_pkcs8";
            run( new String[]{"openssl", "pkcs8",
                    "-topk8",
                    "-inform", "pem",
                    "-outform", "der",
                    "-in",privateKeyPath,
                    "-nocrypt","-out",outPath});
            File file = new File(outPath);
            InputStream inputStream =  new FileInputStream(file);
            byte[] devicePriKeybytes = IOUtils.toByteArray(inputStream);
            PKCS8EncodedKeySpec devicePriKeySpec = new PKCS8EncodedKeySpec(devicePriKeybytes);
            file.delete();
            return keyFactory.generatePrivate(devicePriKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以阻塞模式执行命令
     *
     * @param commands
     *            其他后续命令，如果有设置，会使用管道来关联前后命令的标准输出流和标准输入流
     */
    public static void run(String[] commands) throws Exception{
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(commands));
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();
        InputStream is = p.getInputStream();
        BufferedReader bs = new BufferedReader(new InputStreamReader(is));

        p.waitFor();
        if (p.exitValue() != 0) {
            //说明命令执行失败
            //可以进入到错误处理步骤中
            System.out.println("执行出错");
        }
        String line = null;
        while ((line = bs.readLine()) != null) {
            System.out.println(line);
        }
    }
}