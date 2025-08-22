package com.transformer;

import com.transformer.api.TransformerServiceImpl;
import java.util.List;

/**
 * Main class for testing the standalone JAR functionality.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Transformer Standalone JAR Test ===");
        
        try {
            TransformerServiceImpl service = new TransformerServiceImpl();
            
            System.out.println("Service instantiated successfully!");
            
            List<String> transforms = service.getAvailableTransforms();
            System.out.println("Available transforms: " + transforms.size());
            
            for (String transform : transforms) {
                System.out.println("  - " + transform);
            }
            
            boolean configValid = service.validateConfiguration();
            System.out.println("Configuration valid: " + configValid);
            
            System.out.println("JAR dependencies loaded successfully!");
            System.out.println("=== Test Completed Successfully ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}