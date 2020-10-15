/*import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;*/
import java.util.Scanner;
public class Example
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Scanner kbd = new Scanner(System.in);
        System.out.println("Enter your text: ");

        String input = "Hello";
        char [] charArray = stringToChar(input);

        System.out.println("The elements of the char array are ");
        for (int i = 0; i < charArray.length; i++) {
            System.out.print(charArray[i]);
            System.out.println();
        }

    }

    public static char [] stringToChar(String string) {

        char [] tempArray = new char [string.length()];

        for (int i = 0; i < tempArray.length; ++i) {
            tempArray[i] = string.charAt(i);
        }
        return tempArray;
    }


}