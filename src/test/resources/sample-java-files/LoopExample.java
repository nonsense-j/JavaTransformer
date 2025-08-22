public class LoopExample {
    public void processArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
    }
    
    public void processNumbers() {
        for (int i = 0; i < 10; i++) {
            int value = i * 2;
            System.out.println(value);
        }
    }
    
    public void enhancedForExample(int[] numbers) {
        for (int num : numbers) {
            System.out.println(num);
        }
    }
}