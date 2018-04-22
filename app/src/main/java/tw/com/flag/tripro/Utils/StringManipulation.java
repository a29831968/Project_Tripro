package tw.com.flag.tripro.Utils;

/**
 * Created by Tony on 2018/2/11.
 */

public class StringManipulation {


    // replace dot with space
    public static String expandUsername(String username){
        return username.replace(".", " ");
    }

    // replace space with dot
    public static String condenseUsername(String username){
        return username.replace(" " , ".");
    }

    public static String getTags(String string){
        if(string.indexOf("#") > 0){
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;
            for( char c : charArray){
                if(c == '#'){
                    foundWord = true;
                    sb.append(c);
                }else{
                    if(foundWord){
                        sb.append(c);
                    }
                }
                if(c == ' ' ){
                    foundWord = false;
                }
            }
            String s = sb.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1, s.length());
        }
        return string;
    }
}