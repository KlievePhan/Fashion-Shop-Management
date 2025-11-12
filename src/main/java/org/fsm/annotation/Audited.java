package org.fsm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for auditing method executions
 *
 * Usage:
 * @Audited(entity = "Product", action = "CREATE")
 * public Product createProduct(Product product) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /**
     * The entity type being audited (e.g., "Product", "Brand", "User")
     */
    String entity();

    /**
     * The action being performed (e.g., "CREATE", "UPDATE", "DELETE", "VIEW")
     */
    String action();
}