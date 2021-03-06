package net.jqwik.api.lifecycle;

import java.util.*;

import org.apiguardian.api.*;

import static org.apiguardian.api.API.Status.*;

/**
 * Experimental feature. Not ready for public usage yet.
 */
@API(status = EXPERIMENTAL, since = "1.0")
@FunctionalInterface
public interface SkipExecutionHook extends LifecycleHook<SkipExecutionHook> {

	SkipResult shouldBeSkipped(LifecycleContext context);

	@Override
	default int compareTo(SkipExecutionHook other) {
		return Integer.compare(this.order(), other.order());
	}

	default int order() {
		return 0;
	}

	default SkipExecutionHook then(SkipExecutionHook rest) {
		return descriptor -> {
			SkipResult result = this.shouldBeSkipped(descriptor);
			if (result.isSkipped()) {
				return result;
			} else {
				return rest.shouldBeSkipped(descriptor);
			}
		};
	}

	static SkipExecutionHook combine(List<SkipExecutionHook> skipExecutionHooks) {
		if (skipExecutionHooks.isEmpty()) {
			return descriptor -> SkipResult.doNotSkip();
		}
		SkipExecutionHook first = skipExecutionHooks.remove(0);
		return first.then(combine(skipExecutionHooks));
	}


	class SkipResult {

		public static SkipResult skip(String reason) {
			return new SkipResult(true, reason);
		}

		public static SkipResult doNotSkip() {
			return new SkipResult(false, null);
		}

		private final boolean skipped;
		private final String reason;

		private SkipResult(boolean skipped, String reason) {
			this.skipped = skipped;
			this.reason = reason == null || reason.isEmpty() ? null : reason;
		}

		/**
		 * Whether execution of the context should be skipped.
		 *
		 * @return {@code true} if the execution should be skipped
		 */
		public boolean isSkipped() {
			return this.skipped;
		}

		/**
		 * Get the reason that execution of the context should be skipped,
		 * if available.
		 */
		public Optional<String> reason() {
			return Optional.ofNullable(reason);
		}

		@Override
		public String toString() {
			String skipString = skipped ? "skip" : "do not skip";
			String reasonString = reason().map(reason -> ": " + reason).orElse("");
			return String.format("SkipResult(%s%s)", skipString, reasonString);
		}

	}
}
