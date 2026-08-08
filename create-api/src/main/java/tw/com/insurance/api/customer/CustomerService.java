package tw.com.insurance.api.customer;

import static tw.com.insurance.api.customer.CustomerDtos.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.insurance.api.common.BusinessException;

@Service
public class CustomerService {
    private final CustomerMapper mapper;
    private final byte[] key;
    public CustomerService(CustomerMapper mapper,@Value("${app.pii-encryption-key}") String keyText){
        this.mapper=mapper;
        if(keyText==null||keyText.length()<24) throw new IllegalStateException("PII_ENCRYPTION_KEY 長度至少需要 24 字元");
        this.key=sha256(keyText);
    }
    @Transactional
    public CustomerResult create(CreateCustomerRequest request,String requestId){
        String identity=normalize(request.identityNo());
        if("NATIONAL_ID".equals(request.identityTypeCode())&&!validTaiwanId(identity))
            throw new BusinessException("CUS-4001","身分證字號格式或檢核碼錯誤");
        String id=UUID.randomUUID().toString();
        String mobile=normalizeContact(request.mobilePhone()),email=request.email().trim().toLowerCase(Locale.ROOT);
        try{
            mapper.insertCustomer(id,request.customerName().trim(),request.genderCode(),request.birthDate(),
                request.nationalityCode().toUpperCase(Locale.ROOT),request.residencyCountryCode().toUpperCase(Locale.ROOT));
            mapper.insertIdentity(UUID.randomUUID().toString(),id,request.identityTypeCode(),
                HexFormat.of().formatHex(sha256(identity)),encrypt(identity),identity.substring(identity.length()-4),
                request.nationalityCode().toUpperCase(Locale.ROOT));
            mapper.insertContact(UUID.randomUUID().toString(),id,"MOBILE",encrypt(mobile),
                HexFormat.of().formatHex(sha256(mobile)),maskPhone(mobile));
            mapper.insertContact(UUID.randomUUID().toString(),id,"EMAIL",encrypt(email),
                HexFormat.of().formatHex(sha256(email)),maskEmail(email));
            mapper.insertAddress(UUID.randomUUID().toString(),id,request.postalCode(),encrypt(request.contactAddress().trim()),"地址已加密");
            mapper.insertNameHistory(UUID.randomUUID().toString(),id,request.customerName().trim());
            mapper.insertKyc(UUID.randomUUID().toString(),id,request.occupationCode(),request.sourceOfFundsCode(),request.insurancePurposeCode());
            mapper.insertConsent(UUID.randomUUID().toString(),id,request.consentVersion());
            mapper.insertAudit(UUID.randomUUID().toString(),id,requestId);
        }
        catch(DuplicateKeyException exception){throw new BusinessException("CUS-4091","此身分識別資料已建立客戶");}
        return new CustomerResult(id,request.identityTypeCode(),mask(identity),request.customerName().trim(),
            request.genderCode(),request.birthDate(),maskPhone(mobile),maskEmail(email),"ACTIVE",0);
    }
    private byte[] encrypt(String value){try{byte[] iv=new byte[12];new SecureRandom().nextBytes(iv);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));byte[] encrypted=cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));byte[] result=Arrays.copyOf(iv,iv.length+encrypted.length);System.arraycopy(encrypted,0,result,iv.length,encrypted.length);return result;}catch(Exception e){throw new IllegalStateException("個資加密失敗",e);}}
    private static byte[] sha256(String value){try{return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException(e);}}
    private static String normalize(String value){return value.replaceAll("[^A-Za-z0-9]","").toUpperCase(Locale.ROOT);}
    private static String normalizeContact(String value){return value.replaceAll("[^0-9+]","");}
    private static String mask(String value){return value.length()<4?"****":"******"+value.substring(value.length()-4);}
    private static String maskPhone(String value){return value.length()<4?"****":"******"+value.substring(value.length()-4);}
    private static String maskEmail(String value){int at=value.indexOf('@');return at<=1?"***":value.charAt(0)+"***"+value.substring(at);}
    private static boolean validTaiwanId(String value){if(!value.matches("[A-Z][12][0-9]{8}"))return false;String letters="ABCDEFGHJKLMNPQRSTUVXYWZIO";int code=letters.indexOf(value.charAt(0))+10;if(code<10)return false;int sum=code/10+(code%10)*9;for(int i=1;i<9;i++)sum+=(value.charAt(i)-'0')*(9-i);sum+=value.charAt(9)-'0';return sum%10==0;}
}
