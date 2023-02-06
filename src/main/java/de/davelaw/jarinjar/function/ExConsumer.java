package de.davelaw.jarinjar.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ExConsumer<T, E extends Throwable> {

	@SuppressWarnings("rawtypes")
	ExConsumer NOP = Function.identity()::apply;

	@SuppressWarnings("unchecked")
	static <T, E extends Throwable> ExConsumer<T, E> nop() {
		return NOP;
	}

	void accept(T t) throws E;

	default ExConsumer<T, E> andThen(ExConsumer<? super T, E> after) {
		Objects.requireNonNull(after);

		return          (t) -> {
			/**/  accept(t);
			after.accept(t);
		};
	}
}
