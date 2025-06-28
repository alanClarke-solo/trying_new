package com.example.inventory.aop;

import com.example.inventory.model.tracking.FieldChangeTrackingStore;
import com.example.inventory.model.tracking.TrackChanges;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FieldChangeTrackingAspectTest {

    @Mock
    private FieldChangeTrackingStore trackingStore;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Logger logger;

    @InjectMocks
    private FieldChangeTrackingAspect fieldChangeTrackingAspect;

    private TestEntityWithClassAnnotation classAnnotatedEntity;
    private TestEntityWithFieldAnnotations fieldAnnotatedEntity;
    private TestEntityWithoutAnnotations nonAnnotatedEntity;

    @BeforeEach
    void setUp() {
        classAnnotatedEntity = new TestEntityWithClassAnnotation();
        fieldAnnotatedEntity = new TestEntityWithFieldAnnotations();
        nonAnnotatedEntity = new TestEntityWithoutAnnotations();

        // Inject mock logger using reflection to avoid logging in tests
        try {
            java.lang.reflect.Field logField = FieldChangeTrackingAspect.class.getDeclaredField("log");
            logField.setAccessible(true);
            logField.set(fieldChangeTrackingAspect, logger);
        } catch (Exception e) {
            // Ignore if field injection fails - tests will still work
        }
    }

    @Test
    void testTrackFieldChange_ClassAnnotated_ShouldTrackAllFields() throws Exception {
        // Arrange
        String newValue = "New Name";
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "name");
        verify(logger).debug(anyString(), eq("name"), eq("TestEntityWithClassAnnotation"));
    }

    @Test
    void testTrackFieldChange_FieldAnnotated_ShouldTrackOnlyAnnotatedFields() throws Exception {
        // Arrange
        String newValue = "New Description";
        Method setDescriptionMethod = TestEntityWithFieldAnnotations.class.getMethod("setDescription", String.class);

        when(joinPoint.getTarget()).thenReturn(fieldAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setDescriptionMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(fieldAnnotatedEntity, "description");
        verify(logger).debug(anyString(), eq("description"), eq("TestEntityWithFieldAnnotations"));
    }

    @Test
    void testTrackFieldChange_NonAnnotatedField_ShouldNotTrack() throws Exception {
        // Arrange
        String newValue = "New Name";
        Method setNameMethod = TestEntityWithFieldAnnotations.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(fieldAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger, never()).debug(anyString(), anyString(), anyString());
    }

    @Test
    void testTrackFieldChange_NoAnnotations_ShouldNotTrack() throws Exception {
        // Arrange
        String newValue = "New Name";
        Method setNameMethod = TestEntityWithoutAnnotations.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(nonAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger, never()).debug(anyString(), anyString(), anyString());
    }

    @Test
    void testTrackFieldChange_SameValue_ShouldNotTrack() throws Exception {
        // Arrange
        String existingValue = "Existing Name";
        classAnnotatedEntity.setName(existingValue); // Set the existing value properly
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, existingValue);

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger, never()).debug(anyString(), anyString(), anyString());
    }

    @Test
    void testTrackFieldChange_NullToValue_ShouldTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.setName(null); // Start with null
        String newValue = "New Name";
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "name");
        verify(logger).debug(anyString(), eq("name"), eq("TestEntityWithClassAnnotation"));
    }

    @Test
    void testTrackFieldChange_ValueToNull_ShouldTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.setName("Existing Name");
        String newValue = null;
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "name");
        verify(logger).debug(anyString(), eq("name"), eq("TestEntityWithClassAnnotation"));
    }

    @Test
    void testTrackFieldChange_BothNull_ShouldNotTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.setName(null);
        String newValue = null;
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger, never()).debug(anyString(), anyString(), anyString());
    }

    @Test
    void testTrackFieldChange_NonSetterMethod_ShouldNotTrack() throws Exception {
        // Arrange
        Method getNameMethod = TestEntityWithClassAnnotation.class.getMethod("getName");

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, "someValue");

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger, never()).debug(anyString(), anyString(), anyString());
    }

    @Test
    void testTrackFieldChange_ShortMethodName_ShouldNotTrack() throws Exception {
        // Arrange
        Method shortMethod = TestEntityWithClassAnnotation.class.getMethod("set");

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(shortMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, "someValue");

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger, never()).debug(anyString(), anyString(), anyString());
    }

    @Test
    void testTrackFieldChange_ExceptionHandling_ShouldNotThrow() throws Exception {
        // Arrange
        RuntimeException testException = new RuntimeException("Test exception");
        when(joinPoint.getTarget()).thenThrow(testException);

        // Act & Assert - Should not throw exception
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, "someValue");

        // Verify that markUpdated was never called due to exception
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger).error(eq("Error in field change tracking: {}"), eq("Test exception"), eq(testException));
    }

    @Test
    void testTrackFieldChange_BigDecimalValue_ShouldTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.setPrice(BigDecimal.valueOf(10.99));
        BigDecimal newValue = BigDecimal.valueOf(15.99);
        Method setPriceMethod = TestEntityWithClassAnnotation.class.getMethod("setPrice", BigDecimal.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setPriceMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "price");
        verify(logger).debug(anyString(), eq("price"), eq("TestEntityWithClassAnnotation"));
    }

    @Test
    void testTrackFieldChange_SameBigDecimalValue_ShouldNotTrack() throws Exception {
        // Arrange
        BigDecimal sameValue = BigDecimal.valueOf(10.99);
        classAnnotatedEntity.setPrice(sameValue);
        Method setPriceMethod = TestEntityWithClassAnnotation.class.getMethod("setPrice", BigDecimal.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setPriceMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, sameValue);

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger, never()).debug(anyString(), anyString(), anyString());
    }

    @Test
    void testTrackFieldChange_FieldReflectionError_ShouldHandleGracefully() throws Exception {
        // Arrange - use a method that references a non-existent field
        String newValue = "New Value";
        Method setNonExistentMethod = TestEntityWithClassAnnotation.class.getMethod("setNonExistent", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNonExistentMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert - should not track when field can't be found
        verify(trackingStore, never()).markUpdated(any(), any());
        verify(logger).warn(eq("Could not get current value for field '{}': {}"), eq("nonExistent"), anyString());
    }

    @Test
    void testTrackFieldChange_IntegerValue_ShouldTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.setQuantity(5);
        Integer newValue = 10;
        Method setQuantityMethod = TestEntityWithClassAnnotation.class.getMethod("setQuantity", Integer.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setQuantityMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "quantity");
        verify(logger).debug(anyString(), eq("quantity"), eq("TestEntityWithClassAnnotation"));
    }

    // Test entity classes
    @TrackChanges
    public static class TestEntityWithClassAnnotation {
        private String name;
        private BigDecimal price;
        private Integer quantity;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public void set() { /* Method with short name for testing */ }

        // Method for testing reflection error scenario
        public void setNonExistent(String value) { /* No corresponding field */ }
    }

    public static class TestEntityWithFieldAnnotations {
        private String name;

        @TrackChanges
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class TestEntityWithoutAnnotations {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}