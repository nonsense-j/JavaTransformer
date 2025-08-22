public class SimpleClass {
    private int value;
    
    public void setValue(int newValue) {
        value = newValue;
    }
    
    public int getValue() {
        return value;
    }
    
    public void calculate() {
        int result = 10 + 20;
        value = result * 2;
    }
}