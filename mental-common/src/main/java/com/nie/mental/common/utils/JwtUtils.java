package com.nie.mental.common.utils;

import com.nie.mental.common.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    //构造器注入
    //private  final JwtProperties jwtProperties;
    private final String secret;
    private final long expiration;

    public JwtUtils(JwtProperties jwtProperties){
        //this.jwtProperties = jwtProperties;
        this.secret = jwtProperties.getSecret();
        this.expiration = jwtProperties.getExpiration();
    }


    /**
     * 获取签名密钥 （将Base64 字符串解码为 Key 对象）
     */
    private Key getSigningKey(){
        byte[] keyBybes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBybes);
    }

    /**
     * 生成 JWT 令牌
     * @param claims 令牌中包含的信息
     * @return 生成的 JWT 令牌字符串
     */
    public String generateToken(Map<String,Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis())) //签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) //过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param token 要解析的 JWT 令牌字符串
     * @return 包含令牌信息的Claims对象
     * @throws JwtException 如果令牌无效或过期
     */
    public Claims parseToken(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch( ExpiredJwtException e ){
            //令牌过期异常
           throw new JwtException("JWT令牌已过期", e);
        } catch( MalformedJwtException e ){
            //令牌格式错误
            throw new JwtException("无效的JWT令牌格式",e);
        } catch ( SignatureException e){
            //签名验证失败
            throw new JwtException("JWT签名验证失败", e);
        } catch ( IllegalArgumentException e){
            //令牌为空
            throw new JwtException("JWT令牌不能为空" ,e);
        }
    }
}
