package AST.parser;

class ClassA {
    private int a;
    private int b;
    public int c;
    static final ClassB classB = null;

    public enum ENUM {A, B, C}

    public String getName(String name, int age) {
        return name;
    }

    class ClassB {
        int b;
        int c;
    }
}
