package net.jqwik.api.constraints;

import java.lang.annotation.*;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FloatRange {
	float min() default 0.0f;

	float max();
}