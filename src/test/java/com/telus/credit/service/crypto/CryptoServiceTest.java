
/*
package com.telus.credit.service.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.telus.credit.config.CryptoConfig;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.crypto.service.HashService;

@SpringBootTest(classes = {CryptoConfig.class, CryptoService.class, HashService.class})
public class CryptoServiceTest {

  @Autowired CryptoService cryptoService;
  @Autowired HashService hashService;

  @Test
  public void cryptoTest() {
    try {
      String data = "300";
      String encrypted = cryptoService.encrypt(data);
      String decrypted = cryptoService.decrypt(encrypted);
      System.out.println("data="+data +", encrypted="+encrypted+", decrypted="+decrypted);
      Assertions.assertEquals(data, decrypted);

    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("Encrypt/Decrypt test failed with error=" + e.getMessage());
    }
  }

  @Test
  void getDecryptedDataTest() throws Exception {
	  String encrypted ="rQYHHbm1UhdSZCHpwroEzlD4eIWaZMCPNl2GQHHyGJA=";
	  System.out.println("Decrypted Data:"+cryptoService.decrypt(encrypted)); 
  }
  
  @Test
  public void cryptoTestNegative() {
    try {
      String data = " ";
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> {
            String encrypted = cryptoService.encrypt(data);
          });
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> {
            String encrypted = cryptoService.decrypt(data);
          });
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("Encrypt/Decrypt test failed with error=" + e.getMessage());
    }
  }

  @Test
  public void hmacMd5Test() {
    String result = "a4fec3061d409fe1ad0cdef42e7a86ae";
    Assertions.assertEquals(hashService.hmacMd5("secret", "qwertyuiop"), result);
  }

  @Test
  public void hmacSha512Test() {
    String result =
        "cdb2d59b327562299a1e0f079f9552f3be0eb368a825df9f0afb8baeb6b30a7f0dddabed6815e1f62c786c3990c183d6a2af5dae024940469c763bafacc9f78f";
    Assertions.assertEquals(hashService.hmacSha512("secret", "qwertyuiop"), result);
  }

  @Test
  public void sha512Test() {
    String result =
        "6308d8f6a7ccc9f77e41be5331a52c71c0bb28ecbd4669b960d60dd505dfde9ddd7a30cd26bb308010b3819699daba7caeb791bf6a4153605fe56d1fd3d5df41";
    Assertions.assertEquals(hashService.sha512("qwertyuiop"), result);
  }
}

*/