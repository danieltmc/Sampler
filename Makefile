all: FORCE
	javac *.java

clean:
	rm *.class

run: FORCE
	java SampleTest
