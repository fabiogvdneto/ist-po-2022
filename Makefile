all:
	(cd po-uilib; make $(MFLAGS) all)
	(cd prr-core; make $(MFLAGS) all)
	(cd prr-app; make $(MFLAGS) all)

clean:
	(cd po-uilib; make $(MFLAGS) clean)
	(cd prr-core; make $(MFLAGS) clean)
	(cd prr-app; make $(MFLAGS) clean)

install:
	(cd po-uilib; make $(MFLAGS) install)
	(cd prr-core; make $(MFLAGS) install)
	(cd prr-app; make $(MFLAGS) install)
