### Bugs

- Bug: Arbitrary method resolution is sometimes too loose.
  E.g. return type `@Provide ActionSequenceArbitrary<Integer>` would be accepted
  for parameter of type `@ForAll Arbitrary<ActionSequence<String>>` which will lead
  to class cast exception on property evaluation. See TODO in TypeUsage.canBeAssignedTo()
  
### Missing Tests

- Add tests for RandomGenerators
- Tests for TestRunDatabase
- Tests for TestRunData
- Tests for JqwikProperties and its use in JqwikTestEngine

### General

- Use TestDiscovery from JUnit platform 1.5:
https://junit.org/junit5/docs/5.5.0/api/org/junit/platform/engine/support/discovery/package-summary.html

- Class-based Property like this:
  
  ```
	@Property/Group/PropertyGroup?
	class NewBoard {

		private final Board board;

		public NewBoard(@ForAll Board board) {
			this.board = board;
		}

		@Property
		void all_holes_of_new_board_contain_pegs_except_center(
				@ForAll("validCoordinate")  int x,
				@ForAll("validCoordinate") int y
		) {
			Assume.that(x != board.center() || y != board.center());
			assertThat(board.hole(x, y)).isEqualTo(Hole.PEG);
		}

		@Provide
		Arbitrary<Integer> validCoordinate() {
			return Arbitraries.integers().between(1, board.size());
		}

	}
  ```

- Statistics.keyFigures(String label, Number variable)

- PackageDescriptor e.g.
  @Group
  @Label("mypackage")
  @AddHook(...)
  in package-info.java

- Classifiers as in John Hughes' talk: 
  https://www.youtube.com/watch?v=NcJOiQlzlXQ&list=PLvL2NEhYV4ZvCRCVlXTfB6-d09K3r0Sxa

  - Classifiers.label(boolean condition, String label)

  - Classifiers.checkCoverage(boolean condition, String label, double minCoverage)

  - Alternative: Case-based branching with statistical reporting:

    ```
    Cases.of("name")
      .match(condition1, "label1", () -> {})
      .match(condition2, "label2", () -> {})
      .noMatch();
    ``` 

    - specify minimum coverage for each case
  
- @AddExample[s] annotation like @FromData but additional to generated data

- Store regressions (samples once failed) in dedicated database
  https://hypothesis.readthedocs.io/en/latest/database.html

- Automatically generate nulls for types annotated as nullable
  See https://github.com/pholser/junit-quickcheck/pull/210

- Allow Fixture parameters to examples and properties

- Lifecycle Hooks
  - AroundAllHook
  - AroundTryHook
  - AroundContainerHook
  - Tests for AroundPropertyHook

- Parallel test execution:
  - Across single property with annotation @Parallel 
  - Across Properties: Does it make sense with non working IntelliJ support?
  - For ActionSequences

- Configuration:
  - Switch to JUnit platform configuration
  - Find a way to set config params through command line or env variable

    `request.getConfigurationParameters().get("jqwik.tries.default", Integer::valueOf)`

    plus:

    ```
    test {
        useJUnitPlatform {}
        systemProperties = [
            "jqwik.tries.default": 100
        ]
    }
    ```

    would do the trick.

### Reporting

- Reporting.ARBITRARIES|GENERATORS: report for each property which arbitraries are used.
  - Requires Arbitrary.describe() or something similar

- Reporter.report(key, value)

- Additional reporting data, e.g.
  - Typical runtimes: ~ 1m
  - Fraction of time spent in data generation: ~ 12%

- Record/report generated values by parameter name,
  plus allow label for generated parameter like that: @ForAll(label = "first“)


### Properties

- Stateless Properties:
  - Let action generation access the model state?
    E.g. to use a name that’s already been added to a store.
    - Will require involved version of ActionGenerator, e.g. to take
      Arbitrary<Function<T, Arbitrary<Action<T>>>>
    - Will shrinking work on that?
    - Are there other ways to reach the same goal?
  - Parallel execution of action sequences (see Proper book)
  - Special support for FSMs (finite state machines)

- Allow fully qualified name of static method in @From(method="") annotation
  as in Jupiter's `@MethodSource("example.StringsProviders#tinyStrings")`

- Arbitraries.series(n -> prime(n)[, maxN])

- Arbitraries.fromStream(aStream[, maxLength])

- Arbitraries.fromSize(Function<Integer, Arbitrary> f) : Arbitrary
  Use current size to influence arbitrary generation

- SizableArbitrary.averageSize(50)

- Arbitrary.share(Arbitrary, String key)
  [see here](https://hypothesis.readthedocs.io/en/latest/data.html#hypothesis.strategies.shared)

- Arbitraries.emails()

- @Property(timeout=500) msecs to timeout a property run

- Targeted data generation
  Simulated annealing with an additional target function in property
  (see Proper book)

- Shrinking targets
    - Provide multiple shrinking targets for number arbitraries,
    eg Arbitraries.integers().shrinkTowards(42, 110, 1000000)
    - @[Number]Range(shrinkingTarget=target)
  
- Reimplement String generation based on Unicode codepoints, not on characters
  Maybe consider this: https://github.com/quicktheories/QuickTheories/issues/54

- Check arbitrary providers for numbers that @Range annotations fit, e.g.
  `@IntRange @ForAll long aNumber` should result in a warning

- Data-driven properties: Warnings if method parameters have
  additional annotations to @ForAll

- Exhaustive Generators:
  - Better error messages when exhaustive generation not possible:
    Tell when the number of combinations is too high
    or which arbitrary does not provide exhaustive generation
  - Decimal generation with restricted scale

- Automatic Provider detection/loading
  - E.g. as in junit-quickcheck. If there is a
    mypackage.MyDomainArbitraryProvider for mypackage.MyDomain then load it

- Default Arbitraries, Generators and Shrinking for
  - Tuples.Tuple1/2/3/4/5/6/7/8
  - Dates and times (LocalDateTime, Date, Calendar, etc.)
  - Files, Paths etc.
  - Arrays of Arrays

- Arbitraries and Generators
  - Add Arbitrary.describe() to optionally describe elements in sample output
  - functions/methods (whose output parameter can be generated)
  - @Regex(RegularExpression value) or composable RegexStringArbitrary
  - Constrain charset for String and Char generation through @Charset(String charset) constraint

- Introduce recursive use of Arbitraries.forType(Class<T> targetType)
    - forType(Class<T> targetType, int depth)
    - @UseType(depth = 1)
    

### Contracts / Specifications / Domain objects

see example in package `net.jqwik.docs.contracts.eurocalc`

- Allow specification of consumer and provider contract in test class
- Allow spec annotations in domain classes a la clojure-spec
- Support domain object generation guided by spec annotations
  Have a look at https://github.com/benas/random-beans for inspiration 
