package object;

public class Behavior {
    private Type type;
    private int beginA;
    private int endA;
    private int beginB;
    private int endB;
    public static enum Type {
        INSERT,
        DELETE,
        REPLACE,
        EMPTY;

        private Type() {
        }
    }

    public Behavior() {
    }

    public Behavior(Type type, int beginA, int endA, int beginB, int endB) {
        this.type = type;
        this.beginA = beginA;
        this.endA = endA;
        this.beginB = beginB;
        this.endB = endB;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getBeginA() {
        return beginA;
    }

    public void setBeginA(int beginA) {
        this.beginA = beginA;
    }

    public int getEndA() {
        return endA;
    }

    public void setEndA(int endA) {
        this.endA = endA;
    }

    public int getBeginB() {
        return beginB;
    }

    public void setBeginB(int beginB) {
        this.beginB = beginB;
    }

    public int getEndB() {
        return endB;
    }

    public void setEndB(int endB) {
        this.endB = endB;
    }
}
