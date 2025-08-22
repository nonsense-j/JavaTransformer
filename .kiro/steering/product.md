# Product Overview

## Transformer Library

The Transformer library is a standalone Java code transformation tool that applies semantic-preserving transformations to Java source files. It's designed for metamorphic testing of static analysis tools and bug localization research.

## Key Capabilities

- **Code Transformation**: Applies 30+ different transformations (AddBrackets, LoopConversion, etc.) to Java source code
- **Bug-Guided Strategy**: Uses external static analysis results to focus transformations on problematic code areas
- **Multiple Strategies**: Supports specific transformations, random location selection, and bug-guided location selection
- **Static Analysis Integration**: Works with PMD, SpotBugs, and other static analyzers to guide transformation decisions
- **Metamorphic Testing**: Generates semantically equivalent code variants for testing static analysis tool consistency

## Primary Use Cases

1. **Metamorphic Testing**: Generate multiple versions of code to test static analysis tools for consistency
2. **Bug Localization Research**: Focus transformations on areas where bugs were detected
3. **Code Mutation Testing**: Apply systematic transformations for testing purposes
4. **Static Analysis Tool Validation**: Verify that analysis tools produce consistent results across equivalent code variants

## Target Users

- Researchers in software testing and static analysis
- Developers testing static analysis tools
- Quality assurance teams validating code analysis pipelines
- Academic researchers studying bug localization techniques