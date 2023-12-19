package kr.re.keti.sc.datamanager.common.configuration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import kr.re.keti.sc.datamanager.common.vo.AASUserVO;
import org.springframework.util.Base64Utils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class AASTokenUtil {

    /**
     * parsing token
     * @param pbKey public key
     * @param token jwt token
     * @return AASUserVO parsing result
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static AASUserVO getAllClaimsFromToken(String pbKey, String token) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Jws<Claims> jws = Jwts.parserBuilder()  // (1)
                .setSigningKey(getPublickey(pbKey))         // (2)
                .build()                    // (3)
                .parseClaimsJws(token); // (4)
        AASUserVO aasUserVO = claimsToAASUserVO(jws);
        return aasUserVO;

    }

    private static AASUserVO claimsToAASUserVO(Jws<Claims> jwsClaims) {

        Claims claims = jwsClaims.getBody();
        /**
         * "type": "userSystem",
         * "userId": "cityhub08",
         * "nickname": "cityhub08",
         * "email": "skycross@n2m.co.kr",
         * "role": "Marketplace_User",
         * "iat": 1625316127,
         * "exp": 1625319727,
         * "aud": "Ud6WGtFacxrAbbWLHMLO",
         * "iss": "urn:datahub:cityhub:security"
         */

        AASUserVO aasUserVO = new AASUserVO();
        claims.forEach((key, value) -> {
            if (key.equalsIgnoreCase("type")) {
                aasUserVO.setType((String) value);

            } else if (key.equalsIgnoreCase("userId")) {
                aasUserVO.setUserId((String) value);

            } else if (key.equalsIgnoreCase("nickname")) {
                aasUserVO.setNickname((String) value);

            } else if (key.equalsIgnoreCase("email")) {
                aasUserVO.setEmail((String) value);

            } else if (key.equalsIgnoreCase("role")) {
                aasUserVO.setRole((String) value);

            } else if (key.equalsIgnoreCase("iss")) {
                aasUserVO.setIssuer((String) value);

            } else if (key.equalsIgnoreCase("aud")) {
                aasUserVO.setClientId((String) value);

            } else if (key.equalsIgnoreCase("exp")) {
                aasUserVO.setExpiration(Date.from(
                        Instant.ofEpochSecond(Long.parseLong(value.toString()))));
            } else if (key.equalsIgnoreCase("iat")) {
                aasUserVO.setIssuedAt(Date.from(
                        Instant.ofEpochSecond(Long.parseLong(value.toString()))
                ));
            }
        });

        return aasUserVO;

    }

    /**
     * Extract SSO public key
     * @param pbKey public key
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey getPublickey(String pbKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory kf = KeyFactory.getInstance("RSA");

        String publicKeyContent = pbKey.replace("\n", "").replace("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
                .replace("\"", "");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64Utils.decodeFromString(publicKeyContent));
        PublicKey publicKey = kf.generatePublic(keySpecX509);

        return publicKey;
    }


    public static String createUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


}

