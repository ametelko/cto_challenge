public class ArrayCopyDemo {

    public static void main(String[] args) {

        char[] Src = { 'd', 'e', 'c', 'a', 'f', 'f', 'e',
       'i', 'n', 'a', 't', 'e', 'd' };
        char[] dst = new char[7];
        System.arraycopy(Src, 2, dst, 0, 7);
        System.out.println(new String(dst));

    }
}