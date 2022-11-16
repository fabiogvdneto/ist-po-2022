JARNAME=prr-app

all:
	(cd src; javac -cp ../../po-uilib/po-uilib.jar:../../prr-core/prr-core.jar -encoding UTF-8 `find . -name \*.java`)
	(cd src; jar cf ../$(JARNAME).jar `find . -name \*.class -o -name \*.java` )

clean:
	$(RM) $(JARNAME).jar `find . -name \*.class`
