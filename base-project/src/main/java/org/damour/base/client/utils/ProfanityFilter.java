package org.damour.base.client.utils;

import java.util.ArrayList;
import java.util.List;

public class ProfanityFilter {
  private static List<String> words = new ArrayList<String>();

  static {
    words.add("titsucker");
    words.add("tit sucker");
    words.add("cunt");
    words.add("slut");
    words.add("whore");
    words.add("homo");
    words.add("fuck");
    words.add("shit");
    words.add("titty");
    words.add("bitch");
    words.add("asshole");
    words.add("asswipe");
    words.add("asskisser");
    words.add("ass hole");
    words.add("ass wipe");
    words.add("ass kisser");
    words.add("blowjob");
    words.add("blow job");
    words.add("cum");
    words.add("cumshot");
    words.add("cum shot");
    words.add("dick nose");
    words.add("dick face");
    words.add("dick head");
    words.add("dick sucker");
    words.add("dick licker");
    words.add("dick stroker");
    words.add("dicknose");
    words.add("dickface");
    words.add("dickhead");
    words.add("dicksucker");
    words.add("dicklicker");
    words.add("dickstroker");
    words.add("penis");
    words.add("motherfucker");
    words.add("mother fucker");
    words.add("suck me");
    words.add("sucking me");
    words.add("suck my");
    words.add("sucking my");
    words.add("dick");
    words.add("piss");
  }

  public static String filterProfanity(String str) {

    if (str == null || "".equals(str)) {
      return str;
    }

    for (String word : words) {
      String replacement = "";
      for (int i = 0; i < word.length(); i++) {
        if (word.charAt(i) == ' ') {
          replacement += " ";
        } else {
          replacement += "*";
        }
      }
      str = str.replaceAll("(?i)" + word, replacement);
    }

    return str;
  }

  public static void main(String args[]) {
    System.out.println(filterProfanity("hello you bullshitter"));
  }

}
