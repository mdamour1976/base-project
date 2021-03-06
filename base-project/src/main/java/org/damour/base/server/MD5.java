package org.damour.base.server;

import java.security.MessageDigest;
import org.apache.commons.codec.binary.Base64;

public class MD5 {

  private MessageDigest digest;
  
  public MD5() {
    try {
      digest = java.security.MessageDigest.getInstance("MD5");
    } catch (Throwable t) {
      Logger.log(t);
    }
  }

  public void update(String input) {
    digest.update(input.getBytes());
  }
    
  public String digest() {
    String hash = new String(Base64.encodeBase64(digest.digest()));
    return hash;
  }
  
  public static String md5(String input) {
    MD5 md5 = new MD5();
    md5.update(input);
    return md5.digest();
  }

}
