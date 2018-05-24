To use POSTagger in a Main.java:

public class Main {

    public static void main(String[] args){
        POSTagger posTagger = new POSTagger();
        System.out.println(posTagger.tagPOS("The food was very good. The service was good. The service was slow.").toString());
    }

}



To run in CMD:

javac -cp lib\stanford-postagger.jar POSTagger.java Main.java
java -cp .;lib\stanford-postagger.jar Main