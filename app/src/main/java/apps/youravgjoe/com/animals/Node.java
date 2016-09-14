package apps.youravgjoe.com.animals;

/**
 * Created by jcannon on 9/10/16.
 */
public class Node {

    private int id;
    private String question;
    private String animal;
    private int yesId;
    private int noId;


    Node (int id, String question, String animal) {
        this.id = id;
        this.question = question;
        this.animal = animal;
        yesId = -1;
        noId = -1;
    }

    Node (int id, String question, String animal, int yesId, int noId) {
        this.id = id;
        this.question = question;
        this.animal = animal;
        this.yesId = yesId;
        this.noId = noId;
    }

    // GETTERS:
    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnimal() {
        return animal;
    }

    public int getYesId() {
        return yesId;
    }

    public int getNoId() {
        return noId;
    }

    // SETTERS:
    public void setId(int id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnimal(String animal) {
        this.animal = animal;
    }

    public void setYesId(int yesId) {
        this.yesId = yesId;
    }

    public void setNoId(int noId) {
        this.noId = noId;
    }
}
