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

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
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
    }

    @Test
    void testTrackFieldChange_SameValue_ShouldNotTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.name = "Existing Name";
        String sameValue = "Existing Name";
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, sameValue);

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
    }

    @Test
    void testTrackFieldChange_NullToValue_ShouldTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.name = null;
        String newValue = "New Name";
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "name");
    }

    @Test
    void testTrackFieldChange_ValueToNull_ShouldTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.name = "Existing Name";
        String newValue = null;
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "name");
    }

    @Test
    void testTrackFieldChange_BothNull_ShouldNotTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.name = null;
        String newValue = null;
        Method setNameMethod = TestEntityWithClassAnnotation.class.getMethod("setName", String.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setNameMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore, never()).markUpdated(any(), any());
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
    }

    @Test
    void testTrackFieldChange_ExceptionHandling_ShouldNotThrow() throws Exception {
        // Arrange
        when(joinPoint.getTarget()).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert - Should not throw exception
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, "someValue");

        // Verify that markUpdated was never called due to exception
        verify(trackingStore, never()).markUpdated(any(), any());
    }

    @Test
    void testTrackFieldChange_BigDecimalValue_ShouldTrack() throws Exception {
        // Arrange
        classAnnotatedEntity.price = BigDecimal.valueOf(10.99);
        BigDecimal newValue = BigDecimal.valueOf(15.99);
        Method setPriceMethod = TestEntityWithClassAnnotation.class.getMethod("setPrice", BigDecimal.class);

        when(joinPoint.getTarget()).thenReturn(classAnnotatedEntity);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(setPriceMethod);

        // Act
        fieldChangeTrackingAspect.trackFieldChange(joinPoint, newValue);

        // Assert
        verify(trackingStore).markUpdated(classAnnotatedEntity, "price");
    }

    // Test entity classes

    @TrackChanges
    public static class TestEntityWithClassAnnotation {
        public String name;
        public BigDecimal price;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public void set() { /* Method with short name for testing */ }
    }

    public static class TestEntityWithFieldAnnotations {
        public String name;

        @TrackChanges
        public String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class TestEntityWithoutAnnotations {
        public String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}